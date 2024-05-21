package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.DataAPIKeywords;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.COHERE_EMBED_ENGLISH_V2;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.COHERE_EMBED_ENGLISH_V3;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.HF_MINI_LM_L6;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_CODE;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_DE;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_EN;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_ES;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.JINA_AI_EMBEDDINGS_V2_ZH;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.MISTRAL_AI;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.OPENAI_3_LARGE;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.OPENAI_3_SMALL;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.OPENAI_ADA002;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.UPSTAGE_AI_SOLAR_MINI_1_QUERY;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.VERTEX_AI_GECKO_003;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.VOYAGE_AI_2;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.VOYAGE_AI_CODE_2;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.VOYAGE_AI_LARGE_2;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.VOYAGE_AI_LAW_2;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.VOYAGE_AI_LITE_INSTRUCT;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
abstract class AbstractVectorizeITTest {

    /**
     * Reference to working DataApiNamespace
     */
    protected static Database database;

    /**
     * Initialization of the DataApiNamespace.
     *
     * @return
     *      the instance of Data ApiNamespace
     */
    protected abstract Database initDatabase();

    /**
     * Initialize the Test database on an Astra Environment.
     *
     * @param env
     *      target environment
     * @param cloud
     *      target cloud
     * @param region
     *      target region
     * @return
     *      the database instance
     */
    public static Database initAstraDatabase(AstraEnvironment env, String dbName, CloudProviderType cloud, String region) {
        log.info("Working in environment '{}'", env.name());
        AstraDBAdmin client = getAstraDBClient(env);
        DatabaseAdmin databaseAdmin = client.createDatabase(dbName, cloud, region);
        Database db = databaseAdmin.getDatabase();
        return db;
    }

    /**
     * Access AstraDBAdmin for different environment (to create DB).
     *
     * @param env
     *      astra environment
     * @return
     *      instance of AstraDBAdmin
     */
    public static AstraDBAdmin getAstraDBClient(AstraEnvironment env) {
        switch (env) {
            case DEV:
                return DataAPIClients.createForAstraDev(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_DEV'")))
                        .getAdmin();
            case PROD:
                return DataAPIClients.create(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN'")))
                        .getAdmin();
            case TEST:
                return DataAPIClients.createForAstraTest(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_TEST")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_TEST'")))
                        .getAdmin();
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
    }
    /**
     * Initialization of the working Namespace.
     *
     * @return
     *      current Namespace
     */
    protected Database getDatabase() {
        if (database == null) {
            database = initDatabase();
        }
        return database;
    }

    protected void dropCollection(String name) {
        getDatabase().dropCollection(name);
        log.info("Collection {} dropped", name);
    }

    protected void dropAllCollections() {
        getDatabase().listCollections().forEach(collection -> {
            getDatabase().dropCollection(collection.getName());
            log.info("Collection {} dropped", collection.getName());
        });
    }
    protected void dropCollection(EmbeddingModel model) {
        getDatabase().dropCollection(model.name().toLowerCase());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Error while waiting for the drop", e);
        }
        log.info("Collection {} dropped", model.name().toLowerCase());
    }

    @Test
    public void shouldTestEmbeddingsOpenAI() {
        testEmbeddingModelHeader(OPENAI_ADA002, System.getenv("OPENAI_API_KEY"));
        testEmbeddingModelHeader(OPENAI_3_SMALL, System.getenv("OPENAI_API_KEY"));
        testEmbeddingModelHeader(OPENAI_3_LARGE, System.getenv("OPENAI_API_KEY"));
        dropCollection(OPENAI_ADA002);
        dropCollection(OPENAI_3_SMALL);
        dropCollection(OPENAI_3_LARGE);
    }



    @Test
    public void shouldTestEmbeddingsHuggingFace() {
        dropAllCollections();
        testEmbeddingModelHeader(HF_MINI_LM_L6, System.getenv("HF_API_KEY"));
        dropCollection(HF_MINI_LM_L6);
    }

    @Test
    public void xshouldTestEmbeddingsCohere() {
        //dropAllCollections();
        //dropCollection(COHERE_EMBED_ENGLISH_V2);
        testEmbeddingModelHeader(COHERE_EMBED_ENGLISH_V2, System.getenv("COHERE_API_KEY"));
        testEmbeddingModelHeader(COHERE_EMBED_ENGLISH_V3, System.getenv("COHERE_API_KEY"));
        //dropCollection(COHERE_EMBED_ENGLISH_V2);
        //dropCollection(COHERE_EMBED_ENGLISH_V3);
    }

    @Test
    @Disabled("NOT WORKING")
    public void shouldTestEmbeddingsVertexAI() {
        dropAllCollections();
        //Collection<Document> collection = createCollectionVertex(VERTEX_AI_GECKO_003, "devoxxfrance");
        //testCollection(collection, "");
        dropCollection(VERTEX_AI_GECKO_003);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestVoyageAI() {
        dropAllCollections();
        testEmbeddingModelHeader(VOYAGE_AI_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModelHeader(VOYAGE_AI_LAW_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModelHeader(VOYAGE_AI_CODE_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModelHeader(VOYAGE_AI_LARGE_2, System.getenv("VOYAGE_API_KEY"));
        testEmbeddingModelHeader(VOYAGE_AI_LITE_INSTRUCT, System.getenv("VOYAGE_API_KEY"));
        dropCollection(VOYAGE_AI_2);
        dropCollection(VOYAGE_AI_LAW_2);
        dropCollection(VOYAGE_AI_CODE_2);
        dropCollection(VOYAGE_AI_LARGE_2);
        dropCollection(VOYAGE_AI_LITE_INSTRUCT);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestJina() {
        dropAllCollections();
        testEmbeddingModelHeader(JINA_AI_EMBEDDINGS_V2_EN, System.getenv("JINA_API_KEY"));
        testEmbeddingModelHeader(JINA_AI_EMBEDDINGS_V2_DE, System.getenv("JINA_API_KEY"));
        testEmbeddingModelHeader(JINA_AI_EMBEDDINGS_V2_ES, System.getenv("JINA_API_KEY"));
        testEmbeddingModelHeader(JINA_AI_EMBEDDINGS_V2_ZH, System.getenv("JINA_API_KEY"));
        testEmbeddingModelHeader(JINA_AI_EMBEDDINGS_V2_CODE, System.getenv("JINA_API_KEY"));
        dropCollection(JINA_AI_EMBEDDINGS_V2_EN);
        dropCollection(JINA_AI_EMBEDDINGS_V2_DE);
        dropCollection(JINA_AI_EMBEDDINGS_V2_ES);
        dropCollection(JINA_AI_EMBEDDINGS_V2_ZH);
        dropCollection(JINA_AI_EMBEDDINGS_V2_CODE);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestMistralAI() {
        dropAllCollections();
        testEmbeddingModelHeader(MISTRAL_AI, System.getenv("MISTRAL_API_KEY"));
        dropCollection(MISTRAL_AI);
    }

    @Test
    @Disabled("NOT READY")
    public void shouldTestUpstageAI() {
        dropAllCollections();
        testEmbeddingModelHeader(UPSTAGE_AI_SOLAR_MINI_1_QUERY, System.getenv("UPSTAGE_API_KEY"));
    }

    protected Collection<Document> createCollectionHeader(EmbeddingModel model, String apiKey, Map<String, Object> parameters) {
        return getDatabase().createCollection(
                model.name().toLowerCase(),
                // Create collection with a Service in vectorize
                CollectionOptions.builder()
                        .vectorDimension(model.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(model.getProvider(), model.getName(), null, parameters)
                        .build(),
                // Save API Key at collection level
                new CommandOptions<>().embeddingAPIKey(apiKey));
    }

    private Collection<Document> createCollectionSharedSecret(EmbeddingModel model, String keyName, Map<String, Object> parameters) {
        return getDatabase().createCollection(
                model.name().toLowerCase(),
                // Create collection with a Service in vectorize
                CollectionOptions.builder()
                        .vectorDimension(model.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(model.getProvider(), model.getName(), keyName, parameters)
                        .build(), new CommandOptions<>());
    }

    protected void testEmbeddingModelHeader(EmbeddingModel model, String apiKey) {
        testEmbeddingModelHeader(model, apiKey, null);
    }

    protected void testEmbeddingModelHeader(EmbeddingModel model, String apiKey, Map<String, Object> parameters) {
        log.info("Testing model {}", model);
        Collection<Document> collection = createCollectionHeader(model, apiKey, parameters);
        log.info("Collection created {}", collection.getName());
        testCollection(collection, apiKey);
    }

    protected void testEmbeddingModelSharedSecret(EmbeddingModel model, String keyName, Map<String, Object> parameters) {
        log.info("Testing model {}", model);
        Collection<Document> collection = createCollectionSharedSecret(model, keyName, parameters);
        log.info("Collection created {}", collection.getName());
        testCollection(collection, null);
    }



    protected void testCollection(Collection<Document> collection, String apiKey) {
        List<Document> entries = List.of(
                new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
                new Document(3).vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).vectorize("He's underneath the window"),
                new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).vectorize("Anyway, what you gonna do about it?")
        );

        // Ingestion
        InsertManyResult res = collection.insertMany(entries, new InsertManyOptions().embeddingAPIKey(apiKey));
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                new FindOneOptions()
                        .sort("You shouldn't come around here singing up at people like tha")
                        .projection(Projections.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        .embeddingAPIKey(apiKey)
                        .includeSimilarity());
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);
    }





}
