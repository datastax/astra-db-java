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

import com.datastax.astra.client.core.headers.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.test.integration.AbstractVectorizeApiHeaderIT;
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

/**
 * Vectorize integration tests using AWS Bedrock embeddings against local HCD/DSE.
 * <p>
 * Tests require:
 * <ul>
 *   <li>Local HCD or DSE instance running with vectorize enabled</li>
 *   <li>AWS Bedrock credentials configured in test-config.properties or env vars</li>
 * </ul>
 */
@Slf4j
@EnabledIfLocalAvailable
@DisplayName("Local Vectorize AWS Bedrock Integration Tests")
public class LocalVectorizeAwsBedrockIT extends AbstractVectorizeApiHeaderIT {

    @Override
    protected String getEmbeddingProviderId() {
        return "bedrock";
    }

    @Override
    protected String getApiKey() {
        // AWS Bedrock uses IAM credentials, not an API key
        return null;
    }

    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of("region", getConfig().getBedrockRegion());
    }

    @Override
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
