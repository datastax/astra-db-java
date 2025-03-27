package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindAndRerankCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertOneOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
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
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.KEYWORD;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.LETTER;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.WHITESPACE;


@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
public class Local_12_CollectionFindAndRerankITTest extends AbstractDataAPITest {

    @Test
    @Order(1)
    public void should_find_rerank_providers() {
        Database db = getDatabase();
        FindRerankingProvidersResult res = db.getDatabaseAdmin().findRerankingProviders();
        res.getRerankingProviders().get("nvidia").getModels().forEach(model -> {
            System.out.println("Model: " + model.getName() + " - " + model.getUrl());
        });
    }

    @Test
    @Order(2)
    public void should_create_collection_lexical() {
        // Delete existing collections
        Database db = getDatabase();
        db.listCollectionNames().forEach(db::dropCollection);

        // c_simple
        getDatabase().createCollection("c_simple");

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
                .lexical(new LexicalOptions().analyzer(new Analyzer(LETTER))));
    }

    @Test
    @Order(3)
    public void should_create_collection_reranking() {
        Database db = getDatabase();
        db.listCollectionNames().forEach(db::dropCollection);

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

        // Ingest the CSV
        EmbeddingHeadersProvider authEmbedding =
                new EmbeddingAPIKeyHeaderProvider(System.getenv("OPENAI_API_KEY"));
        CollectionInsertOneOptions options =
                new CollectionInsertOneOptions().embeddingAuthProvider(authEmbedding);
        Files.readAllLines(Paths.get("src/test/resources/philosopher-quotes.csv"))
                .forEach(line -> {
                    String[] chunks = line.split(",");
                    String quote = chunks[1].replace("\"", "");
                    myCol.insertOne(new Document()
                            .append("author", chunks[0])
                            .append("quote", quote)
                            // no insert why hybrid
                            .lexical(quote)
                            .vectorize(quote), options);
                });
    }

    @Test
    public void should_query_collection() {
        Collection<Document> myCol = getDatabase().getCollection("c_farr");
        EmbeddingHeadersProvider authEmbedding =
                new EmbeddingAPIKeyHeaderProvider(System.getenv("OPENAI_API_KEY"));
        myCol.find(new CollectionFindOptions()
                        .embeddingAuthProvider(authEmbedding)
                        .sort(Sort.vectorize("We struggle all in life")))
                        .toList()
                .stream()
                .forEach(doc -> doc.get("author"));
    }

    @Test
    public void should_query_collection_farr() throws IOException {
        Collection<Document> myCol = getDatabase().getCollection("c_farr");

        // HEADERS
        EmbeddingHeadersProvider authEmbedding =
                new EmbeddingAPIKeyHeaderProvider(System.getenv("OPENAI_API_KEY"));
        RerankingHeadersProvider rerankingHeadersProvider =
                new RerankingAPIKeyHeaderProvider(System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV"));

        CollectionFindAndRerankOptions farrOptions = new CollectionFindAndRerankOptions()
                .embeddingAuthProvider(authEmbedding)
                .rerankingAuthProvider(rerankingHeadersProvider)

                .sort(Sort.hybrid(new Hybrid("We struggle all in life")))
                .projection(Projection.include("$vectorize"))
                .limit(10)
                .hybridLimits(10)
                //.hybridLimits(Map.of("$vector", 100, "$lexical", 10))
                .includeSimilarity(true)
                .includeScores(true);
                //.rerankOn("author");

        //Filter filter = Filters.eq("author", "aristotle");
        CollectionFindAndRerankCursor<Document, Document> cursor = myCol.findAndRerank(null, farrOptions);
        for (RerankResult<Document> doc : cursor) {
            System.out.println("---- result ----");
            System.out.println(doc.getDocument());
            System.out.println(doc.getScores());
        }
    }


}
