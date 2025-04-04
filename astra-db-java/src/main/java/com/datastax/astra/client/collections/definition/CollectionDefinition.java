package com.datastax.astra.client.collections.definition;

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

import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.AnalyzerTypes;
import com.datastax.astra.client.core.lexical.LexicalOptions;
import com.datastax.astra.client.core.rerank.CollectionRerankOptions;
import com.datastax.astra.client.core.rerank.RerankServiceOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.vector.VectorOptions;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
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
@Accessors(fluent = true, chain = true)
public class CollectionDefinition {

    /**
     * Default constructor.
     */
    public CollectionDefinition() {}

    // ---------------------
    // DefaultId Options
    // ---------------------

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
     * The 'defaultId' to allow working with different types of identifiers.
     */
    private DefaultIdOptions defaultId;

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

    // ---------------------
    // Indexing options
    // ---------------------

    /**
     * Subclass representing the indexing options.
     */
    @Setter
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
         * Default constructor.
         */
        public IndexingOptions() {}

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

    /**
     * Indexing options
     */
    private IndexingOptions indexing;

    /**
     * Access the indexing options.
     *
     * @return indexing options
     */
    public IndexingOptions getIndexing() {
        return indexing;
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

    // ---------------------
    // Vector Options
    // ---------------------

    /**
     * Vector options.
     */
    private VectorOptions vector;

    /*** Access the vector options.
     *
     * @return
     *      vector options
     */
    public VectorOptions getVector() {
        return vector;
    }

    /**
     * Builder pattern.
     *
     * @param vectorOptions size
     * @return self reference
     */
    public CollectionDefinition vector(VectorOptions vectorOptions) {
        vector = vectorOptions;
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param size size
     * @return self reference
     */
    public CollectionDefinition vectorDimension(int size) {
        if (getVector() == null) {
            vector = new VectorOptions();
        }
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
        if (getVector() == null) {
            vector = new VectorOptions();
        }
        getVector().metric(metric.getValue());
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
        if (getVector() == null) {
            vector = new VectorOptions();
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

    // ---------------------
    // Lexical options
    // ---------------------

    /**
     * Lexical options
     */
    private LexicalOptions lexical;

    /**
     * Gets lexical
     *
     * @return value of lexical
     */
    public LexicalOptions getLexical() {
        return lexical;
    }

    public CollectionDefinition disableLexical() {
        if (getLexical() == null) {
            lexical(new LexicalOptions().enabled(false));
        }
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param lexicalOptions
     *      lexical options positioned on Collection creation
     * @return
     *      self reference
     */
    public CollectionDefinition lexical(LexicalOptions lexicalOptions) {
        lexical = lexicalOptions;
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param analyzer
     *      analyzer structure
     * @return
     *      self reference
     */
    public CollectionDefinition lexical(Analyzer analyzer) {
        if (getLexical() == null) {
            lexical(new LexicalOptions().enabled(true));
        }
        getLexical().analyzer(analyzer);
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param analyzerType
     *      type of analyzer
     * @return self reference
     */
    public CollectionDefinition lexical(AnalyzerTypes analyzerType) {
        return lexical(new Analyzer(analyzerType));
    }

    // ---------------------
    // Reranking options
    // ---------------------

    /**
     * Reranking options
     */
    private CollectionRerankOptions rerank;

    /**
     * Gets reranking
     *
     * @return value of reranking
     */
    public CollectionRerankOptions getRerank() {
        return rerank;
    }

    /**
     * Builder pattern.
     *
     * @param collectionRerankOptions
     *      reranking service information
     * @return
     *      self reference
     */
    public CollectionDefinition rerank(CollectionRerankOptions collectionRerankOptions) {
        rerank = collectionRerankOptions;
        return this;
    }

    /**
     * Builder pattern.
     *
     * @param provider
     *      reranker provider
     * @param model
     *      model
     * @return self reference
     */
    public CollectionDefinition rerank(String provider, String model) {
        if (getRerank() == null) {
            rerank = new CollectionRerankOptions().enabled(true);
        }
        getRerank()
                .enabled(true)
                .service(new RerankServiceOptions().provider(provider).modelName(model));
        return this;
    }

    public CollectionDefinition disableRerank() {
        if (getRerank() == null) {
            rerank = new CollectionRerankOptions().enabled(false);
        }
        return this;
    }

}
