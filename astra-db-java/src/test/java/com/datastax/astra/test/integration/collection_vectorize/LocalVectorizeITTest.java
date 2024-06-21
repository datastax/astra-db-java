package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
@Disabled
public class LocalVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        return DataAPIClients.createDefaultLocalDatabase();
    }

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
                .listEmbeddingProviders().entrySet()) {
            this.testEmbeddingProvider(entry.getKey(), entry.getValue());
        }
    }

}
