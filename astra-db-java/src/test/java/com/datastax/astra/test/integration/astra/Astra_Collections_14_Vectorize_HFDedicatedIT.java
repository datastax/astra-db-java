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

import java.util.HashMap;
import java.util.Map;

/**
 * Vectorize integration tests using HuggingFace Dedicated endpoints against Astra DB.
 * <p>
 * Requires additional environment variables:
 * <ul>
 *   <li>HUGGINGFACEDED_API_KEY</li>
 *   <li>HUGGINGFACEDED_ENDPOINTNAME</li>
 *   <li>HUGGINGFACEDED_REGIONNAME</li>
 *   <li>HUGGINGFACEDED_CLOUDNAME</li>
 * </ul>
 */
@EnabledIfAstra
@Slf4j
@DisplayName("Astra Vectorize HuggingFace Dedicated Integration Tests")
public class Astra_Collections_14_Vectorize_HFDedicatedIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getApiKey() {
        return getConfig().getHuggingFaceDedicatedApiKey();
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "huggingfaceDedicated";
    }

    @Test
    protected Map<String, Object> getAuthenticationParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("endpointName", readEnvVariable("HUGGINGFACEDED_ENDPOINTNAME"));
        params.put("regionName", readEnvVariable("HUGGINGFACEDED_REGIONNAME"));
        params.put("cloudName", readEnvVariable("HUGGINGFACEDED_CLOUDNAME"));
        return params;
    }

    @Override
    public void should_test_vectorize_with_api_header() {
        if (!getConfig().hasHuggingFaceDedicatedConfig()) {
            log.info("Skipping test - HuggingFace Dedicated config not configured");
            return;
        }
        super.should_test_vectorize_with_api_header();
    }
}
