package com.datastax.astra.test.integration.astra.collections;

import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_ASTRA_TOKEN;
import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_CLOUD_PROVIDER;
import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_CLOUD_REGION;
import static com.datastax.astra.test.integration.AbstractDataAPITest.ENV_VAR_DESTINATION;

/**
 * Testing Vectorize in DEV
 */
@EnabledIfSystemProperty(named = ENV_VAR_ASTRA_TOKEN,     matches = ".*")
@EnabledIfSystemProperty(named = ENV_VAR_CLOUD_PROVIDER,  matches = ".*")
@EnabledIfSystemProperty(named = ENV_VAR_CLOUD_REGION,    matches = ".*")
@EnabledIfSystemProperty(named = ENV_VAR_DESTINATION, matches = "astra_(dev|prod|test)")
public class Astra_06_VectorizeITTest extends AbstractVectorizeITTest {

//    @Override
//    protected Database initDatabase() {
//        return testVectorizeDb();
//        //return gcpUsEast1();
//        //return azureEastUs();
//        //return awsUSWest1();
//    }
//
//    private Database gcpUsEast1() {
//        Database db = initAstraDatabase(AstraEnvironment.PROD, "gcp_us_east1", CloudProviderType.GCP, "us-east1");
//        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
//        return db;
//    }
//    private Database awsUSWest1() {
//        Database db = initAstraDatabase(AstraEnvironment.PROD, "aws_us-west-1", CloudProviderType.AWS, "us-west-1");
//        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
//        return db;
//    }
//
//    private Database testVectorizeDb() {
//        Database db = initAstraDatabase(AstraEnvironment.PROD, "test_vectorize", CloudProviderType.AWS, " us-east-2");
//        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
//        return db;
//    }
//
//    private Database gcpEuropeWest4() {
//        Database db = initAstraDatabase(AstraEnvironment.DEV, "gcp_europe_west4", CloudProviderType.GCP, "europe-west4");
//        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
//        return db;
//    }
//
//    private Database azureEastUs() {
//        Database db = initAstraDatabase(AstraEnvironment.DEV, "azure_eastus", CloudProviderType.AZURE, "eastus");
//        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
//        return db;
//    }

    @Test
    public void testOneProvider() {
        dropAllCollections();

        // --- OPEN AI ---
        //shouldTestOneProvider("openai");
        testVectorizeWithSharedKey("openai", "OPENAI_API_KEY");

        // --- JINA_AI ---
        //shouldTestOneProvider("jinaAI");
        //shouldTestOneProviderSharedKey("jinaAI", "JINA_API_KEY");

        // --- Voyage AI ---
        //shouldTestOneProvider("voyageAI");
        //shouldTestOneProviderSharedKey("voyageAI", "VOYAGE_API_KEY");

        // --- HUGGING FACE ---
        //shouldTestOneProvider("huggingface");
        //shouldTestOneProviderSharedKey("huggingface", "HF_API_KEY");

        // shouldTestOneProvider("upstageAI");
        // shouldTestOneProviderSharedKey("upstageAI", "UPSTAGE_API_KEY");

        // shouldTestOneProvider("mistral");
        // shouldTestOneProviderSharedKey("mistral", "MISTRAL_API_KEY");

        // shouldTestOneProvider("nvidia");

//        shouldTestOneProvider("azureOpenAI");
//        shouldTestOneProviderSharedKey("azureOpenAI", "stefano");


    }

    @Test
    public void shouldTestAllProviders() {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .entrySet()) {
            //this.testEmbeddingProvider(entry.getKey(), entry.getValue());
            System.out.println("Provider: " + entry.getKey());
        }
    }

    @Override
    protected String getApiKey() {
        return "";
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "";
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }
}
