package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing Vectorize in DEV
 */
public class AstraDevVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        return gcpEuropeWest4();
        //return gcpUsCentral();
        //return azureEastUs();
        //return awsUSWest2();
    }

    private Database awsUSWest2() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "aws_us-west-2", CloudProviderType.AWS, "us-west-2");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database gcpUsCentral() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "gcp_us_central1", CloudProviderType.GCP, "us-central1");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database gcpEuropeWest4() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "gcp_europe_west4", CloudProviderType.GCP, "europe-west4");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database azureEastUs() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "azure_eastus", CloudProviderType.AZURE, "eastus");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    @Test
    public void testHuggingFaceDedicated() {
        Database db = initDatabase();

        String providerName   = "huggingfaceDedicated";
        String huggingFaceKey = "HF_DEDICATED_API_KEY";
        String collectionName = "collection_hf_dedicated";

        Map<String, EmbeddingProvider> providers =  db.getDatabaseAdmin().listEmbeddingProviders();
        assertThat(providers.get(providerName)).isNotNull();

        // Create collection for HF Dedicated
        dropAllCollections();
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions.builder();
        builder.vectorSimilarity(SimilarityMetric.COSINE);
        builder.vectorDimension(384);
        Map<String, Object > params = new HashMap<>();
        params.put("endpointName", "f70gexilb56vxkhc");
        params.put("regionName", "us-east-1");
        params.put("cloudName", "aws");
        builder.vectorize(providerName, null, huggingFaceKey, params);
        Collection<Document> collection = db.createCollection(collectionName, builder.build(), new CommandOptions<>());
        assertThat(db.collectionExists(collectionName)).isTrue();

        // Insertion With Vectorize
        testCollectionSharedKey(collection);

        /*
        System.out.println("TESTING PROVIDER [" + key + "]");
        Map<String, Object> params = getParameters(key);
        provider.getModels().forEach(model -> {
            System.out.println("Processing MODEL " + model.getName());
            try {
                log.info("Testing model {}", model);

                log.info("Collection created {}", collection.getName());
                testCollectionSharedKey(collection);
                collection.drop();

            } catch(Exception e) {
                log.error("Error while testing model {}", model, e);
            }*/
    }

    @Test
    public void testOneProvider() {
        dropAllCollections();
        shouldTestOneProvider("openai");
        shouldTestOneProvider("jinaAI");
        shouldTestOneProvider("voyageAI");
        dropAllCollections();
        shouldTestOneProvider("huggingface");
        shouldTestOneProvider("upstageAI");
        shouldTestOneProvider("mistral");
        dropAllCollections();
        shouldTestOneProvider("nvidia");
        shouldTestOneProvider("azureOpenAI");
        dropAllCollections();
        shouldTestOneProviderSharedKey("azureOpenAI", "stefano");
        shouldTestOneProviderSharedKey("huggingface", "HF_API_KEY");
        dropAllCollections();
        shouldTestOneProviderSharedKey("jinaAI", "JINA_API_KEY");
        dropAllCollections();
        shouldTestOneProviderSharedKey("voyageAI", "VOYAGE_API_KEY");
        shouldTestOneProviderSharedKey("upstageAI", "UPSTAGE_API_KEY");
        dropAllCollections();
        shouldTestOneProviderSharedKey("mistral", "MISTRAL_API_KEY");
        shouldTestOneProviderSharedKey("openai", "k1");
    }

    @Test
    public void shouldTestAllProviders() {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .listEmbeddingProviders().entrySet()) {
            //this.testEmbeddingProvider(entry.getKey(), entry.getValue());
            System.out.println("Provider: " + entry.getKey());
        }
    }
}
