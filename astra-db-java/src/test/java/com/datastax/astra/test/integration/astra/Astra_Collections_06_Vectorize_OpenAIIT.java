package com.datastax.astra.test.integration.astra;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.test.integration.AbstractVectorizeApiHeaderIT;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Vectorize integration tests using OpenAI embeddings against Astra DB.
 * <p>
 * Tests require:
 * <ul>
 *   <li>test.environment set to astra_dev, astra_prod, or astra_test</li>
 *   <li>Astra token configured in test-config-astra.properties or env var</li>
 *   <li>OpenAI API key configured in test-config.properties or env var</li>
 * </ul>
 */
@Slf4j
@EnabledIfAstra
@DisplayName("Astra Vectorize OpenAI Integration Tests")
public class Astra_Collections_06_Vectorize_OpenAIIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getApiKey() {
        return getConfig().getOpenAiApiKey();
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "openai";
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

    @Test
    public void should_test_vectorize_with_api_header() {
        if (!getConfig().hasOpenAiApiKey()) {
            log.info("Skipping test - OpenAI API key not configured");
            return;
        }
        super.should_test_vectorize_with_api_header();
    }
}
