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

import com.datastax.astra.client.core.headers.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.test.integration.AbstractVectorizeApiHeaderIT;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Vectorize integration tests using AWS Bedrock embeddings against Astra DB.
 * <p>
 * Requires AWS credentials:
 * <ul>
 *   <li>BEDROCK_HEADER_AWS_ACCESS_ID</li>
 *   <li>BEDROCK_HEADER_AWS_SECRET_ID</li>
 *   <li>BEDROCK_HEADER_AWS_REGION</li>
 * </ul>
 */
@EnabledIfAstra
@Slf4j
@DisplayName("Astra Vectorize AWS Bedrock Integration Tests")
public class Astra_Collections_13_Vectorize_AwsBedrockIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getApiKey() {
        // AWS Bedrock uses IAM credentials, not a single API key
        return null;
    }

    @Override
    protected String getEmbeddingProviderId() {
        return "bedrock";
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("region", getConfig().getBedrockRegion());
        return params;
    }

    @Test
    protected EmbeddingHeadersProvider getEmbeddingAuthProvider() {
        return new AWSEmbeddingHeadersProvider(
                getConfig().getBedrockAccessId(),
                getConfig().getBedrockSecretId()
        );
    }

    @Override
    public void should_test_vectorize_with_api_header() {
        if (!getConfig().hasBedrockCredentials()) {
            log.info("Skipping test - AWS Bedrock credentials not configured");
            return;
        }
        super.should_test_vectorize_with_api_header();
    }
}
