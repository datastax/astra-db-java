package com.datastax.astra.test.integration.astra;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.RerankingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.CollectionRerankOptions;
import com.datastax.astra.client.core.rerank.RerankedResult;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.sdk.utils.JsonUtils;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.datastax.astra.client.DataAPIDestination.ASTRA_DEV;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;
import static com.dtsx.astra.sdk.db.domain.CloudProviderType.GCP;

@Slf4j
public class DemoAstraDevFindAndRerank {

    public static final String ASTRA_DB_TOKEN     = System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV");
    public static final String DATABASE_DEMO_NAME = "demo_charter";

    public static final String ASTRA_DB_ENDPOINT = "https://9e0ff165-666d-4a69-b5b7-727d6cd77092-us-central1.apps.astra-dev.datastax.com";
    public static final String OPEN_API_KEY      = System.getenv("OPENAI_API_KEY");

    /**
     * Create a DataAPIClient for Astra DEV.
     */
    private DataAPIClient getDataApiClient() {
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(ASTRA_DEV)
                .rerankAPIKey(ASTRA_DB_TOKEN)
                .embeddingAPIKey(OPEN_API_KEY)
                .logRequests();
        return new DataAPIClient(ASTRA_DB_TOKEN, options);
    }

    /**
     * Access DB from its endpoints (regular way)
     */
    private Database getDatabase() {
        // Alternatively, you can use the following code to get the database:
        return getDataApiClient().getAdmin()
          .createDatabase(DATABASE_DEMO_NAME, GCP, "us-central1")
          .getDatabase();
        //return getDataApiClient().getDatabase(ASTRA_DB_ENDPOINT);
    }

    @Test
    public void should_list_db() {
        getDataApiClient()
            .getAdmin()
            .listDatabases()
            .forEach(db -> {
                log.info("DB: name={}, status={}, cloud={}, region={}",
                    db.getName(),
                    db.getRawDevopsResponse().getStatus(),
                    db.getCloudProvider(),
                    db.getRegion());
        });
    }

    @Test
    public void should_find_rerank_providers() {
        getDatabase()
                .getDatabaseAdmin()
                .findRerankingProviders()
                .getRerankingProviders()
                .get("nvidia")
                .getModels().forEach(model -> {
                    System.out.println("Model: " + model.getName() + " - " + model.getUrl());
        });
    }

    @Test
    public void should_find_embedding_providers() {
        getDatabase().getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .get("nvidia")
                .getModels()
                .forEach(model -> {
                    System.out.println("Model: " + model.getName() + " - " + model.getVectorDimension());
        });
    }

    @Test
    public void should_create_collection_default() {
        // Create Collection
        getDatabase().createCollection("c_first");

        CollectionDefinition def = getDatabase()
                .getCollection("c_first")
                .getDefinition();
        System.out.println("--------------------------------");
        System.out.println(JsonUtils.marshall(def));
    }

    @Test
    public void should_create_collection_reranking() {

        CollectionDefinition def = new CollectionDefinition();

        // Vector
        VectorServiceOptions vectorService = new VectorServiceOptions()
                .provider("nvidia")
                .modelName("NV-Embed-QA");
        VectorOptions vectorOptions = new VectorOptions()
                .dimension(1024)
                .metric(SimilarityMetric.COSINE.getValue())
                .service(vectorService);
        def.vector(vectorOptions);

        // Lexical
        LexicalOptions lexicalOptions = new LexicalOptions()
                .enabled(true)
                .analyzer(new Analyzer(STANDARD));
        def.lexical(lexicalOptions);

        // Rerank
        RerankServiceOptions rerankService = new RerankServiceOptions()
                .modelName("nvidia/llama-3.2-nv-rerankqa-1b-v2")
                .provider("nvidia");
        CollectionRerankOptions rerankOptions = new CollectionRerankOptions()
                .enabled(true)
                .service(rerankService);
        def.rerank(rerankOptions);

        getDatabase().createCollection("c_find_rerank", def);
    }

    @Test
    public void should_populate_collection() throws IOException {

        Collection<Document> myCol = getDatabase()
                .getCollection("c_find_rerank");

        myCol.deleteAll();

        List<Document> docs = Files
                .readAllLines(Paths.get("src/test/resources/philosopher-quotes.csv"))
                .stream().map(line -> {
                    String[] chunks = line.split(",");
                    String quote = chunks[1].replace("\"", "");
                    return new Document()
                            .append("author", chunks[0])
                            .append("quote", quote)
                            .append("tags", chunks.length > 2 ? chunks[2].split(";") : new String[0])
                            //.hybrid(new Hybrid(quote))
                            //.hybrid(new Hybrid().lexical(quote).vectorize(quote))
                            .lexical(quote).vectorize(quote);
                }).toList();

        CollectionInsertManyOptions beGentleWihAstraDev = new CollectionInsertManyOptions()
                .concurrency(3).chunkSize(20);
        myCol.insertMany(docs, beGentleWihAstraDev);
    }

    @Test
    public void should_run_find_and_rerank() throws IOException {
        CollectionFindAndRerankOptions farrOptions = new CollectionFindAndRerankOptions()
                .projection(Projection.include("$vectorize", "_id", "quote", "author"))


                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))



                .rerankingAuthProvider(new RerankingAPIKeyHeaderProvider(ASTRA_DB_TOKEN))
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(ASTRA_DB_TOKEN))
                .includeScores(true)
                .limit(10)
                .hybridLimits(10);

        getDatabase().getCollection("c_find_rerank")
                .findAndRerank(null, farrOptions)
                .forEach(res -> {
                    System.out.println(res.getDocument());
                    System.out.println(res.getScores());
                });
    }

    @Test
    public void should_test_with_openai() throws IOException {
        Collection<Document> openAiCollection = getDatabase()
            .createCollection("c_openai", new CollectionDefinition()
                .vector(new VectorOptions()
                        .dimension(1536)
                        .metric(SimilarityMetric.COSINE.getValue())
                        .service(new VectorServiceOptions()
                                .provider("openai")
                                .modelName("text-embedding-3-small")))
                .lexical(STANDARD)
                .rerank(new CollectionRerankOptions()
                        .service(new RerankServiceOptions()
                                .modelName("nvidia/llama-3.2-nv-rerankqa-1b-v2")
                                .provider("nvidia"))));

        // INGEST
        List<Document> docs = Files.readAllLines(Paths.get("src/test/resources/philosopher-quotes.csv"))
                .stream().map(line -> {
                    String[] chunks = line.split(",");
                    String quote = chunks[1].replace("\"", "");
                    return new Document()
                            .append("author", chunks[0])
                            .append("quote", quote)
                            // no insert why hybrid
                            .lexical(quote)
                            .vectorize(quote);
                }).toList();
        EmbeddingHeadersProvider authEmbedding =
                new EmbeddingAPIKeyHeaderProvider(System.getenv("OPENAI_API_KEY"));
        openAiCollection.insertMany(docs, new CollectionInsertManyOptions()
                .concurrency(3)
                .chunkSize(10)
                .embeddingAuthProvider(authEmbedding));

        // SEARCh
        // Build Query
        CollectionFindAndRerankOptions farrOptions = new CollectionFindAndRerankOptions()
                .projection(Projection.include("$vectorize", "_id", "quote", "author"))
                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .rerankingAuthProvider(new RerankingAPIKeyHeaderProvider(ASTRA_DB_TOKEN))
                .embeddingAuthProvider(authEmbedding)
                .includeScores(true)
                .limit(10)
                .hybridLimits(10);

        // Execute the command
        openAiCollection.findAndRerank(null, farrOptions)
                .stream()
                .forEach(res -> {
                    System.out.println(res.getDocument());
                    System.out.println(res.getScores());
                });
    }

    @Test
    public void should_query_collection_bring_my_own_vector() {
        Collection<Document> myCol = getDatabase().getCollection("c_openai");

        // Bring your own vector
        Hybrid hybridSort = new Hybrid().vector(
                OpenAiEmbeddingModel
                    .builder().apiKey(OPEN_API_KEY)
                    .modelName("text-embedding-3-small")
                    .build().embed("We struggle all in life")
                    .content().vector())
                .lexical("struggle life");

        // Build Query
        CollectionFindAndRerankOptions farrOptions = new CollectionFindAndRerankOptions()
                .projection(Projection.include("$vectorize", "_id", "quote", "author"))
                .rerankingAuthProvider(new RerankingAPIKeyHeaderProvider(ASTRA_DB_TOKEN))
                .sort(Sort.hybrid(hybridSort))
                // This is the default
                .rerankOn(DataAPIKeywords.VECTORIZE.getKeyword())
                // Rerank query
                .rerankQuery("We struggle all in life")
                .includeScores(true)
                .limit(10)
                .hybridLimits(20);

        // Run the query
        List<RerankedResult<Document>> result = myCol.findAndRerank(farrOptions).toList();
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        System.out.println("Result: " + result.size());
    }
}