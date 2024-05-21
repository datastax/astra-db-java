package com.datastax.astra.test.integration.collection_vectorize;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_ADA002;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_LARGE;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_SMALL;
import static com.datastax.astra.test.integration.collection_vectorize.EmbeddingModel.AZURE_OPENAI_SMALL_SHARED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing Vectorize in DEV
 */
public class AstraDevVectorizeITTest extends AbstractVectorizeITTest {

    @Override
    protected Database initDatabase() {
        Database db = initAstraDatabase(AstraEnvironment.DEV, "aws_us-west-2", CloudProviderType.AWS, "us-west-2");
        db.getCommandOptions().getObservers().put("logging", new LoggingCommandObserver(DataAPIClient.class));
        return db;
    }

    @Test
    public void shouldTestAzureOpenAISharedSecret() {
        dropCollection(AZURE_OPENAI_SMALL_SHARED);
        String azureOpenAiKeyName = "stefano";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("deploymentId", "text-embedding-3-small-steo");
        parameters.put("resourceName", "steo-azure-openai");
        testEmbeddingModelSharedSecret(AZURE_OPENAI_SMALL_SHARED, azureOpenAiKeyName, parameters);
        dropCollection(AZURE_OPENAI_SMALL_SHARED);
    }

    @Test
    public void shouldTestAzureOpenAI() {
        // Assessing AzureOpenAI is present
        Optional<EmbeddingProvider> provider = Optional.ofNullable(getDatabase()
                .getDatabaseAdmin()
                .listEmbeddingProviders()
                .get(AZURE_OPENAI_LARGE.getProvider()));
        assertThat(provider).isPresent();
        Map<String, EmbeddingProvider.Parameter> params = provider.get()
                .getParameters().stream().collect(Collectors.toMap(
                        EmbeddingProvider.Parameter::getName,
                        Function.identity()));
        assertThat(params).containsKeys("resourceName", "deploymentId");

        String azureOpenAiKey = System.getenv("AZURE_OPENAI_API_KEY");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("deploymentId", "text-embedding-3-small-steo");
        parameters.put("resourceName", "steo-azure-openai");

        dropCollection(AZURE_OPENAI_SMALL);
        dropCollection(AZURE_OPENAI_ADA002);
        dropCollection(AZURE_OPENAI_LARGE);
        testEmbeddingModelHeader(AZURE_OPENAI_SMALL,  azureOpenAiKey, parameters);
        testEmbeddingModelHeader(AZURE_OPENAI_ADA002, azureOpenAiKey, parameters);
        testEmbeddingModelHeader(AZURE_OPENAI_LARGE,  azureOpenAiKey, parameters);
        dropCollection(AZURE_OPENAI_SMALL);
        dropCollection(AZURE_OPENAI_ADA002);
        dropCollection(AZURE_OPENAI_LARGE);
    }



}
