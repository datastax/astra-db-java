package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

@Slf4j
@EnabledIfEnvironmentVariable(named = "ENABLED_TEST_DATA_API_LOCAL", matches = "true")
public class LocalVectorizeITTest extends AbstractVectorizeITTest {

//    @Override
//    protected Database initDatabase() {
//        return DataAPIClients.createDefaultLocalDatabase();
//    }

    @Test
    public void testOneProvider() {
        //shouldTestOneProvider("azureOpenAI");
        //shouldTestOneProvider("jinaAI");
        //shouldTestOneProvider("voyageAI");
        ///shouldTestOneProvider("huggingface");
        //shouldTestOneProvider("upstageAI");
        shouldTestOneProvider("mistral");
    }

    @Test
    public void shouldTestAllProviders() {
        for (Map.Entry<String, EmbeddingProvider> entry : getDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .entrySet()) {
            this.testEmbeddingProvider(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected AstraEnvironment getAstraEnvironment() {
        return null;
    }

    @Override
    protected CloudProviderType getCloudProvider() {
        return null;
    }

    @Override
    protected String getRegion() {
        return "";
    }
}
