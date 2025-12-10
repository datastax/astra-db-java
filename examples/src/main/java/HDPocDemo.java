import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.RerankingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.RerankingHeadersProvider;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.Main;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static com.datastax.astra.client.core.lexical.AnalyzerTypes.PORTER_STEM;
import static com.datastax.astra.client.core.lexical.AnalyzerTypes.STANDARD;

public class HDPocDemo {

    public static final String ASTRA_TOKEN    = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    public static final String ASTRA_ENDPOINT = "https://844c28ee-7824-40a5-9b48-f4ca160c2b7b-us-east-2.apps.astra.datastax.com";
    public static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    public static final String COL_OPENAI     = "c_product_openai";
    public static final String COL_OPENAI_KMS = "c_product_openai_kms";

    public static void main(String[] args)
    throws Exception {

        // connect
        Database db = new DataAPIClient(ASTRA_TOKEN,
          new DataAPIClientOptions().logRequests()).getDatabase(ASTRA_ENDPOINT);
        System.out.println("Connected to Astra");

        // create schema
        createSchema(db);
        System.out.println("Tables and Collections created");

        // truncate collections if needed
        db.getCollection(COL_OPENAI).deleteAll();
        db.getCollection(COL_OPENAI_KMS).deleteAll();

        // Insert sample records with LLM embeddings (use key in header)
        ingestJsonCollection(db, "h-d.json", COL_OPENAI, OPENAI_API_KEY);
        ingestJsonCollection(db, "h-d.json", COL_OPENAI_KMS, null);

        // Show all records, with all their fields
        findAll(db, COL_OPENAI);;

        // Run a BM25 Query
        String lQuery = "AutoAir";
        runQueryCollectionBm25(db, COL_OPENAI, lQuery);

        // Run the Similarity Search
        String vQuery = "quiet stainless steel dishwasher with third rack";
        runQueryCollectionSimilaritySearch(db, COL_OPENAI, vQuery);

        // Run the Hybrid Search
        runQueryFindAndRerank(db, COL_OPENAI, vQuery, lQuery);
    }

    public static void createSchema(Database db) {

        // Create a collection where we will send the OPENAI API KEY in the header
        db.createCollection(COL_OPENAI, new CollectionDefinition()
                //.indexing(new CollectionDefinition.IndexingOptions().deny(List.of("content")))
                .lexical(new Analyzer(STANDARD).addFilter(PORTER_STEM.getValue()))
                .vector(1536, SimilarityMetric.DOT_PRODUCT)
                .vectorize("openai", "text-embedding-ada-002"));
        System.out.println("Collection '" + COL_OPENAI + "' created");

        // Create a collection where the openAI KEY in managed by Astra
        db.createCollection(COL_OPENAI_KMS, new CollectionDefinition()
                //.indexing(new CollectionDefinition.IndexingOptions().deny(List.of("content")))
                .lexical(new Analyzer(STANDARD).addFilter(PORTER_STEM.getValue()))
                .vector(1536, SimilarityMetric.DOT_PRODUCT)
                .vectorize("openai", "text-embedding-ada-002", "API_KEY_CEDRICK"));
        System.out.println("Collection '" + COL_OPENAI_KMS + "' created");
    }

    public static void ingestJsonCollection(Database db, String fileName, String name, String openAiKey)
    throws Exception {
        File file = new File(Main.class.getClassLoader().getResource(fileName).toURI());
        try (FileInputStream stream = new FileInputStream(file)) {

            // 0. Load the file, omdId could be our id
            List<Document> docs = new ObjectMapper().readValue(stream, new TypeReference<>() {});

            // 1. Mapping
            docs.forEach(doc -> {
                doc.id(doc.getString("omsid"));
                doc.vectorize(doc.getString("content"));
                doc.lexical(doc.getString("content"));
            });

            // 2. We insert the data providing openai API KEY in the header
            Collection<Document> col1 = db.getCollection(name);
            CollectionInsertManyOptions options = new CollectionInsertManyOptions();
            if (openAiKey !=null) options.embeddingApiKey(openAiKey);
            col1.insertMany(docs, options);
        }
    }

    public static void findAll(Database db, String collection) {
        db.getCollection(collection)
                .find(null, new CollectionFindOptions().projection(Projection.include("*")))
                .forEach(System.out::println);
    }

    public static void runQueryCollectionBm25(Database db, String collection, String term) {
        RerankingHeadersProvider rerankingHeadersProvider = new RerankingAPIKeyHeaderProvider(ASTRA_TOKEN);
        db.getCollection(collection)
          .find(Filters.match(term), new CollectionFindOptions()
                  .dataAPIClientOptions(new DataAPIClientOptions().logRequests()))
          .toList()
          .forEach(System.out::println);

    }

    private static void runQueryCollectionSimilaritySearch(Database db, String colOpenai, String query) {
        db.getCollection(colOpenai)
                .find(null, new CollectionFindOptions()
                        .sort(Sort.vectorize(query))
                        .dataAPIClientOptions(new DataAPIClientOptions().embeddingAPIKey(OPENAI_API_KEY)))
                .toList()
                .forEach(System.out::println);
    }

    private static void runQueryFindAndRerank(Database db, String colOpenai, String vQuery, String term) {
        EmbeddingHeadersProvider authEmbedding = new EmbeddingAPIKeyHeaderProvider(OPENAI_API_KEY);
        RerankingHeadersProvider rerankingHeadersProvider = new RerankingAPIKeyHeaderProvider(ASTRA_TOKEN);
        CollectionFindAndRerankOptions options = new CollectionFindAndRerankOptions()
                .embeddingAuthProvider(authEmbedding)
                .rerankingAuthProvider(rerankingHeadersProvider)
                .includeScores(true)
                .limit(10)
                .sort(Sort.hybrid(vQuery, term))
                .projection(Projection.include("$vectorize"))
                .hybridLimits(10);
        db.getCollection(colOpenai)
          .findAndRerank(options)
          .toList()
          .forEach(System.out::println);
    }

}
