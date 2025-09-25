package com.datastax.astra.test.integration.hcd;

import com.datastax.astra.test.integration.AbstractVectorizeApiHeaderITTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
public class Local_13_Collection_Vectorize_NVidiaTest extends AbstractVectorizeApiHeaderITTest {

    @Override
    protected String getEmbeddingProviderId() {
        return "nvidia";
    }

    @Override
    protected String getApiKey() {
        // Authenticated with an Astra Token
        return readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV");
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

    @Test
    public void should_test_vectorize_with_api_header() {
        // overriding as not possible
    }



}
