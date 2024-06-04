package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.internal.command.LoggingCommandObserver;
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
        return gcpEuropeWest4();
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
