package com.datastax.astra.test.integration.local;

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
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

/**
 * Vectorize integration tests using JinaAI embeddings against local HCD/DSE.
 * <p>
 * Tests require:
 * <ul>
 *   <li>Local HCD or DSE instance running with vectorize enabled</li>
 *   <li>JinaAI API key configured in test-config.properties or JINA_API_KEY env var</li>
 * </ul>
 */
@EnabledIfLocalAvailable
@Slf4j
@DisplayName("Local Vectorize JinaAI Integration Tests")
public class LocalVectorizeJinaAIIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getEmbeddingProviderId() {
        return "jinaAI";
    }

    @Override
    protected String getApiKey() {
        return getConfig().getJinaApiKey();
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

    @Override
    public void should_test_vectorize_with_api_header() {
        if (!getConfig().hasJinaApiKey()) {
            log.info("Skipping test - JinaAI API key not configured");
            return;
        }
        super.should_test_vectorize_with_api_header();
    }
}
