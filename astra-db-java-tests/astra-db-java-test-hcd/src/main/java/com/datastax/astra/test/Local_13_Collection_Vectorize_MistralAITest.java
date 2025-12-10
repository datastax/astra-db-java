package com.datastax.astra.test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
@EnabledIfSystemProperty(named = "MISTRAL_API_KEY", matches = ".*")
public class Local_13_Collection_Vectorize_MistralAITest extends AbstractVectorizeApiHeaderITTest {

    @Override
    protected String getEmbeddingProviderId() {
        return "mistral";
    }

    @Override
    protected String getApiKey() {
        return readEnvVariable("MISTRAL_API_KEY");
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

}
