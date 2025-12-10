package com.datastax.astra.test.collections;

import com.datastax.astra.test.AbstractVectorizeITTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

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
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "astra_dev")
@DisabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "(?!astra_dev)")
public class Astra_08_VectorizeEmbeddingApiKeyITTest extends AbstractVectorizeITTest {


    @Test
    public void should_test_embedding_providers() {
//        testVectorizeWithApiKeyHeader(
//                System.getenv("EMBEDDING_PROVIDER"),
//                System.getenv("EMBEDDING_API_KEY"));
//        // Flush the collections
//        getDatabase()
//                .listCollectionNames()
//                .forEach(name -> getDatabase().dropCollection(name));
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
