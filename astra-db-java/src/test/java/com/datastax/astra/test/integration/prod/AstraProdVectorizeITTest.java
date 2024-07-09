package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Testing Vectorize in DEV
 */
public class AstraProdVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        return testVectorizeDb();
        //return gcpUsEast1();
        //return azureEastUs();
        //return awsUSWest1();
    }

    private Database gcpUsEast1() {
        Database db = initAstraDatabase(AstraEnvironment.PROD, "gcp_us_east1", CloudProviderType.GCP, "us-east1");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }
    private Database awsUSWest1() {
        Database db = initAstraDatabase(AstraEnvironment.PROD, "aws_us-west-1", CloudProviderType.AWS, "us-west-1");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    private Database testVectorizeDb() {
        Database db = initAstraDatabase(AstraEnvironment.PROD, "test_vectorize", CloudProviderType.AWS, " us-east-2");
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
    public void testOneProvider() {
        dropAllCollections();

        // --- OPEN AI ---
        //shouldTestOneProvider("openai");
        shouldTestOneProviderSharedKey("openai", "OPENAI_API_KEY");

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
}
