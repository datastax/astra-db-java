package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.DataAPIKeywords;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.SimilarityMetric;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return databaseAdmin.getDatabase();
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

    public static String getApiKey(String provider) {
        if (provider.equals("openai")) {
            return System.getenv("OPENAI_API_KEY");
        } else if (provider.equals("huggingface")) {
            return System.getenv("HF_API_KEY");
        } else if (provider.equals("azureOpenAI")) {
            return System.getenv("AZURE_OPENAI_API_KEY");
        } else if (provider.equals("voyageAI")) {
            return System.getenv("VOYAGE_API_KEY");
        } else if (provider.equals("jinaAI")) {
            return System.getenv("JINA_API_KEY");
        } else if (provider.equals("mistral")) {
            return System.getenv("MISTRAL_API_KEY");
        } else if (provider.equals("upstageAI")) {
            return System.getenv("UPSTAGE_API_KEY");
        } else if (provider.equals("cohere")) {
            return System.getenv("COHERE_API_KEY");
        } else if (provider.equals("vertexAI")) {
            return System.getenv("VERTEX_API_KEY");
        }
        return null;
    }

    public Map<String, Object> getParameters(String provider) {
        if (provider.equals("azureOpenAI")) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("deploymentId", "text-embedding-3-small-steo");
            parameters.put("resourceName", "steo-azure-openai");
            return parameters;
        }
        return null;
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

    public void testEmbeddingProvider(String key, EmbeddingProvider provider) {
        System.out.println("TESTING PROVIDER [" + key + "]");
        String apiKey              = getApiKey(key);
        Map<String, Object> params = getParameters(key);
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);
                Collection<Document> collection = createCollectionHeader(key, model, apiKey, params);
                log.info("Collection created {}", collection.getName());
                testCollection(collection, apiKey);
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }
        });
    }

    private void testEmbeddingProviderSharedKey(String key, EmbeddingProvider provider, String keyName) {
        System.out.println("TESTING PROVIDER [" + key + "]");
        Map<String, Object> params = getParameters(key);
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);
                Collection<Document> collection = createCollectionSharedSecret(key, model, keyName, params);
                log.info("Collection created {}", collection.getName());
                testCollectionSharedKey(collection);
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }
        });
    }

    private void testCollectionSharedKey(Collection<Document> collection) {
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
        InsertManyResult res = collection.insertMany(entries);
        assertThat(res.getInsertedIds()).hasSize(8);
        log.info("{} Documents inserted", res.getInsertedIds().size());
        Optional<Document> doc = collection.findOne(null,
                new FindOneOptions()
                        .sort("You shouldn't come around here singing up at people like tha")
                        .projection(Projections.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        .includeSimilarity());
        log.info("Document found {}", doc);
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(Integer.class)).isEqualTo(7);
        assertThat(doc.get().getDouble(DataAPIKeywords.SIMILARITY.getKeyword())).isGreaterThan(.8);
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
   
    // ===================================================================================
    // ===================================================================================
    // ===================================================================================

    protected Collection<Document> createCollectionHeader(String provider, EmbeddingProvider.Model model, String apiKey, Map<String, Object> parameters) {
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions.builder();
        builder.vectorSimilarity(SimilarityMetric.COSINE);
        if (model.getVectorDimension() != null) {
            builder.vectorDimension(model.getVectorDimension());
        }
        builder.vectorize(provider, model.getName(), null, parameters);
        return getDatabase().createCollection(
                getCollectionNameFromModel(model.getName()), builder.build(),
                new CommandOptions<>().embeddingAPIKey(apiKey));
    }

    private String getCollectionNameFromModel(String modelName) {
        System.out.println("MODEL NAME: " + modelName);
        String name=  modelName.toLowerCase()
                .replaceAll("-", "_")
                .replaceAll("/", "_")
                .replaceAll("\\.", "");
        // Truncate
        name = name.substring(0,Math.min(name.length(), 25));
        System.out.println("Collection NAME: " + name);
        return name;
    }

    private Collection<Document> createCollectionSharedSecret(String provider, EmbeddingProvider.Model model, String keyName, Map<String, Object> parameters) {
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions.builder();
        builder.vectorSimilarity(SimilarityMetric.COSINE);
        if (model.getVectorDimension() != null) {
            builder.vectorDimension(model.getVectorDimension());
        }
        builder.vectorize(provider, model.getName(), keyName, parameters);
        return getDatabase().createCollection(
                getCollectionNameFromModel(model.getName()),
                builder.build(), new CommandOptions<>());
    }

    protected void testEmbeddingModelSharedSecret(String provider, EmbeddingProvider.Model model, String keyName, Map<String, Object> parameters) {
        log.info("Testing model {}", model);
        Collection<Document> collection = createCollectionSharedSecret(provider, model, keyName, parameters);
        log.info("Collection created {}", collection.getName());
        testCollection(collection, null);
    }


    public void shouldTestOneProvider(String provider) {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .listEmbeddingProviders().entrySet()) {
            if (entry.getKey().equals(provider)) {
                this.testEmbeddingProvider(entry.getKey(), entry.getValue());
            }
        }
    }

    public void shouldTestOneProviderSharedKey(String provider, String keyName) {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .listEmbeddingProviders().entrySet()) {
            if (entry.getKey().equals(provider)) {
                this.testEmbeddingProviderSharedKey(entry.getKey(), entry.getValue(), keyName);
            }
        }
    }


}
