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
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Vectorize integration tests using Nvidia embeddings against local HCD/DSE.
 * <p>
 * Tests require:
 * <ul>
 *   <li>Local HCD or DSE instance running with vectorize enabled</li>
 *   <li>Nvidia API key or Astra token configured</li>
 * </ul>
 * <p>
 * Note: Nvidia vectorize on local HCD may use Astra token for authentication.
 */
@EnabledIfLocalAvailable
@Slf4j
@DisplayName("Local Vectorize Nvidia Integration Tests")
public class LocalVectorizeNvidiaIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getApiKey() {
        // Try Nvidia API key first, fall back to Astra token
        String nvidiaKey = getConfig().getNvidiaApiKey();
        if (nvidiaKey != null && !nvidiaKey.isEmpty()) {
            return nvidiaKey;
        }
        return getConfig().getAstraToken();
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "nvidia";
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of();
    }

    @Test
    @Override
    public void should_test_vectorize_with_api_header() {
        // Nvidia vectorize test requires special setup on local HCD
        log.info("Skipping Nvidia vectorize test - requires special HCD configuration");
    }
}
