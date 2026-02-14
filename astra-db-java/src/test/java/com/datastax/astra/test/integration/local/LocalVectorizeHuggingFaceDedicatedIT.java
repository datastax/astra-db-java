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

import java.util.HashMap;
import java.util.Map;

/**
 * Vectorize integration tests using HuggingFace Dedicated endpoints against local HCD/DSE.
 * <p>
 * Tests require:
 * <ul>
 *   <li>Local HCD or DSE instance running with vectorize enabled</li>
 *   <li>HuggingFace Dedicated configuration in test-config.properties or env vars</li>
 * </ul>
 */
@Slf4j
@EnabledIfLocalAvailable
@DisplayName("Local Vectorize HuggingFace Dedicated Integration Tests")
public class LocalVectorizeHuggingFaceDedicatedIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getEmbeddingProviderId() {
        return "huggingfaceDedicated";
    }

    @Override
    protected String getApiKey() {
        return getConfig().getHuggingFaceDedicatedApiKey();
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("endpointName", getConfig().getHuggingFaceDedicatedEndpoint());
        params.put("regionName", getConfig().getHuggingFaceDedicatedRegion());
        params.put("cloudName", getConfig().getHuggingFaceDedicatedCloud());
        return params;
    }

    @Override
    public void should_test_vectorize_with_api_header() {
        if (!getConfig().hasHuggingFaceDedicatedConfig()) {
            log.info("Skipping test - HuggingFace Dedicated config not available");
            return;
        }
        super.should_test_vectorize_with_api_header();
    }
}
