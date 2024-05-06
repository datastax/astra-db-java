package com.datastax.astra.client.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.beans.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Set of options to define and initialize a collection.
 */
@Getter
@Setter
public class CollectionOptions {

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
     * Default constructor.
     */
    public CollectionOptions() {
        // left blank on purpose, built with builder
    }

    /**
     * Subclass representing the indexing options.
     */
    @Getter @Setter
    public static class DefaultIdOptions {

        /** Type for the default id. */
        private String type;

        /**
         * Default constructor.
         */
        public DefaultIdOptions() {
            // marshalled by jackson
        }

        /**
         * Default constructor.
         *
         * @param type
         *      type for the default id
         */
        public DefaultIdOptions(String type) {
            this.type = type;
        }
    }

    /**
     * Subclass representing the indexing options.
     */
    @Getter @Setter
    public static class IndexingOptions {

        /**
         * If not empty will index everything but those properties.
         */
        private List<String> deny;

        /**
         * If not empty will index just those properties.
         */
        private List<String> allow;

        /**
         * Default constructor.
         */
        public IndexingOptions() {
            // left blank, serialization with jackson
        }
    }

    /**
     * Subclass representing the vector options.
     */
    @Getter @Setter
    public static class VectorOptions {

        /**
         * Size of the vector.
         */
        private int dimension;

        /**
         * Similarity metric.
         */
        private String metric;

        /**
         * Service for vectorization
         */
        private Service service;

        /** Default constructor. */
        public VectorOptions() {
            // left blank, serialization with jackson
        }

        /**
         * Get metric as an enum.
         *
         * @return
         *      similarity metric
         */
        @JsonIgnore
        public SimilarityMetric getSimilarityMetric() {
            return SimilarityMetric.fromValue(metric);
        }
    }

    /**
     * Subclass representing the services options.
     */
    @Getter @Setter
    public static class Service {

        /** LLM provider. */
        private String provider;

        /** LLM Model name. */
        private String modelName;

        /** Authentication information like keys and secrets. */
        private Authentication authentication;

        /** Free form parameters. */
        private Map<String, Object> parameters;

        /** Default constructor. */
        public Service() {
            // left blank, serialization with jackson
        }
    }

    /**
     * Subclass representing the Authentication options.
     */
    @Getter @Setter
    public static class Authentication {

        /** Type of authentication: Oauth, API Key, etc. */
        private List<String> type;

        /** Name of the secret if sstored in Astra. */
        private String secretName;

        /** Default constructor. */
        public Authentication() {
            // left blank, serialization with jackson
        }
    }

    /**
     * Subclass representing a parameters for LLM Services
     */
    @Getter @Setter
    public static class Parameters {

        /** Type for the parameters. */
        private String type;

        /** declare if mandatory or not. */
        private boolean required;

        /** the default value for the parameter. */
        @JsonProperty("default")
        private Object defaultValue;

        /** description of the parameter. */
        private String help;

        /** Default constructor. */
        public Parameters() {
            // left blank, serialization with jackson
        }
    }

    /**
     * Gets a builder.
     *
     * @return a builder
     */
    public static CollectionOptionsBuilder builder() {
        return new CollectionOptionsBuilder();
    }

    /**
     * Builder for {@link CollectionInfo}.
     */
    public static class CollectionOptionsBuilder {

        /**
         * Options for Vector
         */
        VectorOptions vector;

        /**
         * Options for Indexing
         */
        IndexingOptions indexing;

        /**
         * Options for Default Id
         */
        String defaultId;

        /**
         * Access the vector options.
         *
         * @return
         *      vector options
         */
        private VectorOptions getVector() {
            if (vector == null) {
                vector = new VectorOptions();
            }
            return vector;
        }

        /**
         * Access the indexing options.
         *
         * @return
         *      indexing options
         */
        private IndexingOptions getIndexing() {
            if (indexing == null) {
                indexing = new IndexingOptions();
            }
            return indexing;
        }

        /**
         * Default constructor.
         */
        public CollectionOptionsBuilder() {
            // left blank, builder pattern
        }

        /**
         * Builder Pattern with the Identifiers.
         *
         * @param idType
         *      type of ids
         * @return
         *      self reference
         */
        public CollectionOptionsBuilder defaultIdType(CollectionIdTypes idType) {
            this.defaultId = idType.getValue();
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param size size
         * @return self reference
         */
        public CollectionOptionsBuilder vectorDimension(int size) {
            getVector().setDimension(size);
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param function function
         * @return self reference
         */
        public CollectionOptionsBuilder vectorSimilarity(@NonNull SimilarityMetric function) {
            getVector().setMetric(function.getValue());
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param properties size
         * @return self reference
         */
        public CollectionOptionsBuilder indexingDeny(@NonNull String... properties) {
            if (getIndexing().getAllow() != null) {
                throw new IllegalStateException("'indexing.deny' and 'indexing.allow' are mutually exclusive");
            }
            getIndexing().setDeny(Arrays.asList(properties));
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param properties size
         * @return self reference
         */
        public CollectionOptionsBuilder indexingAllow(String... properties) {
            if (getIndexing().getDeny() != null) {
                throw new IllegalStateException("'indexing.deny' and 'indexing.allow' are mutually exclusive");
            }
            getIndexing().setAllow(Arrays.asList(properties));
            return this;
        }

        /**
         * Builder pattern.
         *
         * @param dimension dimension
         * @param function  function
         * @return self reference
         */
        public CollectionOptionsBuilder vector(int dimension, @NonNull SimilarityMetric function) {
            vectorSimilarity(function);
            vectorDimension(dimension);
            return this;
        }

        /**
         * Enable Vectorization within the collection.
         *
         * @param provider
         *      provider Name (LLM)
         * @param modeName
         *      mode name
         * @return
         *      self reference
         */
        public CollectionOptionsBuilder vectorize(String provider, String modeName) {
            Service embeddingService = new Service();
            embeddingService.setProvider(provider);
            embeddingService.setModelName(modeName);
            getVector().setService(embeddingService);
            return this;
        }

        /**
         * Enable Vectorization within the collection.
         *
         * @param provider
         *      provider Name (LLM)
         * @param modeName
         *      mode name
         * @param parameters
         *      expected parameters for vectorize
         * @return
         *      self reference
         */
        public CollectionOptionsBuilder vectorize(String provider, String modeName, Map<String, Object> parameters) {
            vectorize(provider, modeName);
            getVector().getService().setParameters(parameters);
            return this;
        }

        /**
         * Build the output.
         *
         * @return collection definition
         */
        public CollectionOptions build() {
            CollectionOptions req = new CollectionOptions();
            req.vector    = this.vector;
            req.indexing  = this.indexing;
            if (defaultId != null) {
                req.defaultId = new DefaultIdOptions(defaultId);
            }
            return req;
        }
    }

}
