package com.datastax.astra.client.collections;

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

import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Set of options to define and initialize a collection.
 */
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class CollectionDefinition {

    /**
     * The 'defaultId' to allow working with different types of identifiers.
     */
    private DefaultIdOptions defaultId;

    /**
     * Vector options.
     */
    private VectorOptions vector;

    /**
     * Indexing options
     */
    private IndexingOptions indexing;

    /**
     * Subclass representing the indexing options.
     */
    @Setter
    public static class DefaultIdOptions {

        /**
         * Type for the default id.
         */
        private CollectionDefaultIdTypes type;

        /**
         * Default constructor.
         */
        public DefaultIdOptions() {
            // marshalled by jackson
        }

        /**
         * Default constructor.
         *
         * @param type type for the default id
         */
        public DefaultIdOptions(CollectionDefaultIdTypes type) {
            this.type = type;
        }

        /**
         * Gets type
         *
         * @return value of type
         */
        public CollectionDefaultIdTypes getType() {
            return type;
        }
    }

    /**
     * Subclass representing the indexing options.
     */
    @Setter
    @NoArgsConstructor
    public static class IndexingOptions {

        /**
         * If not empty will index everything but those properties.
         */
        List<String> deny;

        /**
         * If not empty will index just those properties.
         */
        List<String> allow;

        /**
         * Gets deny
         *
         * @return value of deny
         */
        public List<String> getDeny() {
            return deny;
        }

        /**
         * Gets allow
         *
         * @return value of allow
         */
        public List<String> getAllow() {
            return allow;
        }
    }

    /*** Access the vector options.
     *
     * @return
     *      vector options
     */
    public VectorOptions getVector() {
        return vector;
    }

    /**
     * Access the indexing options.
     *
     * @return indexing options
     */
    public IndexingOptions getIndexing() {
        return indexing;
    }

    /**
     * Gets defaultId
     *
     * @return value of defaultId
     */
    public DefaultIdOptions getDefaultId() {
        return defaultId;
    }

    /**
     * Builder pattern.
     *
     * @param type
     *      default id type
     * @return self reference
     */
    public CollectionDefinition defaultId(CollectionDefaultIdTypes type) {
        this.defaultId = new DefaultIdOptions(type);
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param size size
     * @return self reference
     */
    public CollectionDefinition vectorDimension(int size) {
        getVector().dimension(size);
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param metric similarity metric
     * @return self reference
     */
    public CollectionDefinition vectorSimilarity(@NonNull SimilarityMetric metric) {
        getVector().metric(metric.getValue());
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param properties size
     * @return self reference
     */
    public CollectionDefinition indexingDeny(@NonNull String... properties) {
        if (getIndexing() == null) {
            indexing = new IndexingOptions();
        }
        if (getIndexing().getAllow() != null) {
            throw new IllegalStateException("'indexing.deny' and 'indexing.allow' are mutually exclusive");
        }
        getIndexing().deny(Arrays.asList(properties));
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param properties size
     * @return self reference
     */
    public CollectionDefinition indexingAllow(String... properties) {
        if (getIndexing() == null) {
            indexing = new IndexingOptions();
        }
        if (getIndexing().getDeny() != null) {
            throw new IllegalStateException("'indexing.deny' and 'indexing.allow' are mutually exclusive");
        }
        getIndexing().allow(Arrays.asList(properties));
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param dimension dimension
     * @param function  function
     * @return self reference
     */
    public CollectionDefinition vector(int dimension, @NonNull SimilarityMetric function) {
        if (getVector() == null) {
            vector = new VectorOptions();
        }
        return vectorSimilarity(function).vectorDimension(dimension);
    }

    /**
     * Enable Vectorization within the collection.
     *
     * @param provider provider Name (LLM)
     * @param modeName mode name
     * @return self reference
     */
    public CollectionDefinition vectorize(String provider, String modeName) {
        return vectorize(provider, modeName, null);
    }

    /**
     * Enable Vectorization within the collection.
     *
     * @param provider        provider Name (LLM)
     * @param modeName        mode name
     * @param sharedSecretKey name of the key in the system
     * @return self reference
     */
    public CollectionDefinition vectorize(String provider, String modeName, String sharedSecretKey) {
        VectorServiceOptions embeddingService = new VectorServiceOptions();
        embeddingService.provider(provider).modelName(modeName);
        if (sharedSecretKey != null) {
            // --> Since 1.3.1 the suffix is not needed anymore
            //embeddingService.setAuthentication(Map.of("providerKey", keyName + ".providerKey"));
            embeddingService.authentication(Map.of("providerKey", sharedSecretKey));
            // <--- Since 1.3.1 the suffix is not needed anymore
        }
        getVector().service(embeddingService);
        return this;
    }

    /**
     * Enable Vectorization within the collection.
     *
     * @param provider        provider Name (LLM)
     * @param modeName        mode name
     * @param parameters      expected parameters for vectorize
     * @param sharedSecretKey name of the key in the system
     * @return self reference
     */
    public CollectionDefinition vectorize(String provider, String modeName, String sharedSecretKey, Map<String, Object> parameters) {
        vectorize(provider, modeName, sharedSecretKey);
        getVector().getService().parameters(parameters);
        return this;
    }


}
