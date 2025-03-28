package com.datastax.astra.test.integration.local;

import com.datastax.astra.test.integration.AbstractVectorizeApiHeaderITTest;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
@EnabledIfSystemProperty(named = "OPENAI_API_KEY", matches = ".*")
public class Local_13_Collection_Vectorize_OpenAIITest extends AbstractVectorizeApiHeaderITTest {

    @Override
    protected String getEmbeddingProviderId() {
        return "openai";
    }

    @Override
    protected String getApiKey() {
        return readEnvVariable("OPENAI_API_KEY");
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

}
