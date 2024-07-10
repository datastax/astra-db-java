package com.datastax.astra.test.integration.dev_vectorize;

import com.datastax.astra.test.integration.AbstractVectorizeITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing Vectorize in DEV
 */
@Slf4j
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_PROVIDER", matches = ".*")
@EnabledIfEnvironmentVariable(named = "EMBEDDING_API_KEY", matches = ".*")
public class AstraDevVectorizeEmbeddingApiKeyITTest extends AbstractVectorizeITTest {

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
    public void should_test_embedding_providers() {
        shouldTestOneProvider(
                System.getenv("EMBEDDING_PROVIDER"),
                System.getenv("EMBEDDING_API_KEY"));
        // Flush the collections
        getDatabase()
                .listCollectionNames()
                .forEach(name -> getDatabase().dropCollection(name));
    }

}
