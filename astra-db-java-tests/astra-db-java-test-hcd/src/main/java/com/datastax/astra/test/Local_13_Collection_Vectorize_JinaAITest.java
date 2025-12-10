package com.datastax.astra.test;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
@EnabledIfSystemProperty(named = "JINA_API_KEY", matches = ".*")
public class Local_13_Collection_Vectorize_JinaAITest extends AbstractVectorizeApiHeaderITTest {

    @Override
    protected String getEmbeddingProviderId() {
        return "jinaAI";
    }

    @Override
    protected String getApiKey() {
        return readEnvVariable("JINA_API_KEY");
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

}
