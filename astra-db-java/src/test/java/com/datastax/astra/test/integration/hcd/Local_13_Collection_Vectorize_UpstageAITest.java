package com.datastax.astra.test.integration.hcd;

import com.datastax.astra.test.integration.AbstractVectorizeApiHeaderITTest;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
@EnabledIfSystemProperty(named = "UPSTAGE_API_KEY", matches = ".*")
public class Local_13_Collection_Vectorize_UpstageAITest extends AbstractVectorizeApiHeaderITTest {

    @Override
    protected String getEmbeddingProviderId() {
        return "upstageAI";
    }

    @Override
    protected String getApiKey() {
        return readEnvVariable("UPSTAGE_API_KEY");
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

}
