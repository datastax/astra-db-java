package com.datastax.astra.client.core.headers;

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

/**
 * To use the interface embeddings API the user needs to be authenticated against the embedding provider.
 * - You can set the header variable `x-embedding-api-key` to authenticate against the embedding provider.
 * - You can set multiple headers to authenticate against multiple embedding providers.
 */
public interface RerankingHeadersProvider extends HeadersProvider, Cloneable {

    /**
     * Clone the EmbeddingHeadersProvider.
     *
     * @return
     *      a new instance of the EmbeddingHeadersProvider.
     */
    RerankingHeadersProvider copy();
}
