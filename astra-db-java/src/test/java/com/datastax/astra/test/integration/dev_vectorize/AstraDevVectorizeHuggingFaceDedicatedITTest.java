package com.datastax.astra.test.integration.dev_vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.command.CommandOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;
import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_API_KEY", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_PROVIDER", matches = ".*")
public class AstraDevVectorizeHuggingFaceDedicatedITTest extends AbstractVectorizeITTest {

    @Override
    public AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.DEV;
    }

    @Override
    public CloudProviderType getCloudProvider() {
        return CloudProviderType.valueOf(System.getenv("ASTRA_CLOUD_PROVIDER_DEV"));
    }

    @Override
    public String getRegion() {
        return System.getenv("ASTRA_CLOUD_REGION_DEV");
    }

    @Test
    public void testHuggingFaceDedicated() {
        String providerName   = System.getenv("EMBEDDING_PROVIDER");
        String collectionName = "collection_hf_dedicated";
        FindEmbeddingProvidersResult result = getDatabaseAdmin().findEmbeddingProviders();
        assertThat(result.getEmbeddingProviders().get(providerName)).isNotNull();

        // Create Collection
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions.builder();
        builder.vectorDimension(Integer.valueOf(System.getenv("HUGGINGFACEDED_DIMENSION")));
        Map<String, Object > params = new HashMap<>();
        params.put("endpointName",  System.getenv("HUGGINGFACEDED_ENDPOINTNAME"));
        params.put("regionName", System.getenv("HUGGINGFACEDED_REGIONNAME"));
        params.put("cloudName",  System.getenv("HUGGINGFACEDED_CLOUDNAME"));
        builder.vectorize(providerName, null, null, params);
        Collection<Document> collection = getDatabase()
                .createCollection(collectionName, builder.build(), new CommandOptions<>());
        assertThat(getDatabase()
                .collectionExists(collectionName)).isTrue();

        // Test Collection
        testCollection(collection, new EmbeddingAPIKeyHeaderProvider( System.getenv("EMBEDDING_API_KEY")));

        // Drop Collection
        getDatabase().dropCollection(collectionName);
    }

}
