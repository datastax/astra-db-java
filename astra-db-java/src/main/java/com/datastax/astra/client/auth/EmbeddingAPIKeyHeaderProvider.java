package com.datastax.astra.client.auth;

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

import java.util.Map;

/**
 * Default header when working with an embedding service provider.
 */
public class EmbeddingAPIKeyHeaderProvider implements EmbeddingHeadersProvider {

    /** Headers param with the embedding ApiKey when dealing with a embedding service provider. */
    public static String HEADER_EMBEDDING_API_KEY = "x-embedding-api-key";

    private final String apiKey;

    /**
     * Constructor.
     *
     * @param apiKey
     *      the apiKey to use for the embedding provider.
     */
    public EmbeddingAPIKeyHeaderProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Build the Header for this provider.
     *
     * @return
     *      headers to use for the embedding provider.
     */
    @Override
    public Map<String, String> getHeaders() {
        return Map.of(HEADER_EMBEDDING_API_KEY, apiKey);
    }
}
