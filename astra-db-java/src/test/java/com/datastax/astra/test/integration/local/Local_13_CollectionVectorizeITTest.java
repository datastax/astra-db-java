package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

@Slf4j
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
public class Local_13_CollectionVectorizeITTest extends AbstractVectorizeITTest {

    @Test
    public void testOneProvider() {
        //shouldTestOneProvider("azureOpenAI");
        //shouldTestOneProvider("jinaAI");
        //shouldTestOneProvider("voyageAI");
        ///shouldTestOneProvider("huggingface");
        //shouldTestOneProvider("upstageAI");
        shouldTestOneProvider("openai");
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


}
