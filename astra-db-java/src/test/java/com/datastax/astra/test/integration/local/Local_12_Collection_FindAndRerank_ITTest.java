package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.RerankingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.RerankingHeadersProvider;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.CollectionRerankOptions;
import com.datastax.astra.client.core.rerank.RerankResult;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import com.datastax.astra.test.integration.AbstractDataAPITest;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.KEYWORD;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.LETTER;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.WHITESPACE;


@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
public class Local_12_Collection_FindAndRerank_ITTest extends AbstractDataAPITest {

    @Test
    @Order(1)
    public void should_find_rerank_providers() {
        dropAllCollections();
        FindRerankingProvidersResult res = getDatabase().getDatabaseAdmin().findRerankingProviders();
        res.getRerankingProviders().get("nvidia").getModels().forEach(model -> {
            System.out.println("Model: " + model.getName() + " - " + model.getUrl());
        });
    }

    @Test
    @Order(2)
    public void should_create_collection_lexical() {
        dropAllCollections();
        // Analyzer String
        Analyzer             standardAnalyzer = new Analyzer(STANDARD);
        LexicalOptions       lexicalOptions   = new LexicalOptions().enabled(true).analyzer(standardAnalyzer);
        CollectionDefinition def = new CollectionDefinition().lexical(lexicalOptions);
        getDatabase().createCollection("c_lexical_standard",def);

        // Analyzer Tokenizer
        Analyzer analyzer = new Analyzer()
                .tokenizer(WHITESPACE.getValue());
        getDatabase().createCollection("c_lexical_custom", new CollectionDefinition()
                .lexical(new LexicalOptions().analyzer(analyzer)));

        // Analyzer Keyword
        Analyzer analyzer2 = new Analyzer()
                .tokenizer(KEYWORD.getValue())
                .addFilter("synonym", Map.of("synonyms", "Alex, alex, Alexander, alexander => Alex"));
        getDatabase().createCollection("c_lexical_custom2", new CollectionDefinition()
                .lexical(new LexicalOptions().analyzer(analyzer2)));

        getDatabase().createCollection("c_lexical_false", new CollectionDefinition()
                .lexical(new LexicalOptions().analyzer(new Analyzer()
                        .tokenizer(LETTER.getValue()))));
    }

    @Test
    @Order(3)
    public void should_create_collection_reranking() {
        dropAllCollections();

        // Vector
        VectorServiceOptions vectorService = new VectorServiceOptions()
                .provider( "openai")
                .modelName("text-embedding-3-small");
        VectorOptions vectorOptions = new VectorOptions()
                .dimension(1536)
                .metric(SimilarityMetric.COSINE.getValue())
                .service(vectorService);

        // Lexical
        LexicalOptions lexicalOptions = new LexicalOptions()
                .enabled(true)
                .analyzer(new Analyzer(STANDARD));

        // Rerank
        RerankServiceOptions rerankService = new RerankServiceOptions()
                .modelName("nvidia/llama-3.2-nv-rerankqa-1b-v2")
                .provider("nvidia");
        CollectionRerankOptions rerankOptions = new CollectionRerankOptions()
                 .enabled(true)
                 .service(rerankService);
        CollectionDefinition def = new CollectionDefinition()
                .vector(vectorOptions)
                .lexical(lexicalOptions)
                .rerank(rerankOptions);

        getDatabase().createCollection("c_find_rerank",def);
    }

    @Test
    @Order(4)
    public void should_populate_collection_farr() throws IOException {
        Database db = getDatabase();
        Collection<Document> myCol = db.getCollection("c_find_rerank");
        myCol.deleteAll();

        // Ingest the CSV
        EmbeddingHeadersProvider authEmbedding =
                new EmbeddingAPIKeyHeaderProvider(System.getenv("OPENAI_API_KEY"));
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

        myCol.insertMany(docs, new CollectionInsertManyOptions()
                .concurrency(3)
                .chunkSize(10)
                .embeddingAuthProvider(authEmbedding));
    }

    private CollectionFindAndRerankOptions initiateFindAndRerankQuery() {
        EmbeddingHeadersProvider authEmbedding =
                new EmbeddingAPIKeyHeaderProvider(System.getenv("OPENAI_API_KEY"));
        RerankingHeadersProvider rerankingHeadersProvider =
                new RerankingAPIKeyHeaderProvider(System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV"));
        return new CollectionFindAndRerankOptions()
                .embeddingAuthProvider(authEmbedding)
                .rerankingAuthProvider(rerankingHeadersProvider)
                .includeScores(true)
                .limit(10);
    }

    @Test
    @Order(5)
    public void should_query_collection_farr() throws IOException {

        // Build Query
        CollectionFindAndRerankOptions farrOptions = initiateFindAndRerankQuery()
                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .projection(Projection.include("$vectorize"))
                .hybridLimits(10);

        // Execute the command
        getDatabase()
                .getCollection("c_find_rerank")
                .findAndRerank(null, farrOptions)
                .stream()
                .forEach(res -> {
                    System.out.println(res.getDocument());
                    System.out.println(res.getScores());
                });
    }

    @Test
    @Order(6)
    public void should_query_collection_byov() {
        Collection<Document> myCol = getDatabase().getCollection("c_find_rerank");

        // Bring your own vector
        String openAiApiKey = System.getenv("OPENAI_API_KEY");
        float[] myVector = OpenAiEmbeddingModel
                .builder()
                .apiKey(openAiApiKey)
                .modelName("text-embedding-3-small")
                .build()
                .embed("We struggle all in life").content().vector();

        // Build Query
        CollectionFindAndRerankOptions farrOptions = initiateFindAndRerankQuery()
            .sort(Sort.hybrid(new Hybrid().vector(myVector).lexical("struggle life")))
            .rerankOn(DataAPIKeywords.VECTORIZE.getKeyword())
            .rerankQuery("We struggle all in life")
            .hybridLimits(20);

        // Run the query
        List<RerankResult<Document>> result = myCol.findAndRerank(farrOptions).toList();
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        System.out.println("Result3: " + result.size());
    }


}
