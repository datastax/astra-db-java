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
 * Vectorize integration tests using MistralAI embeddings against Astra DB.
 */
@EnabledIfAstra
@Slf4j
@DisplayName("Astra / Collections / Vectorized / MistralAI")
public class Astra_Collections_07_Vectorize_MistralAIIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getApiKey() {
        return getConfig().getMistralApiKey();
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "mistral";
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

    @Test
    public void should_test_vectorize_with_api_header() {
        if (!getConfig().hasMistralApiKey()) {
            log.info("Skipping test - MistralAI API key not configured");
            return;
        }
        super.should_test_vectorize_with_api_header();
    }
}
