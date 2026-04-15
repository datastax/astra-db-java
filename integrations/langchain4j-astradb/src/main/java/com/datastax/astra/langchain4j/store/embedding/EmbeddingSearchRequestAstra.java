package com.datastax.astra.langchain4j.store.embedding;

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

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.filter.Filter;

/**
 * This abstract class represents a search request for embeddings in AstraDB
 * A user can provide a field 'vectorize' that will be converted as a embedding on the spot and use for search or insertion
 */
public class EmbeddingSearchRequestAstra extends EmbeddingSearchRequest {

    /**
     * The field to vectorize
     */
    private final String query;

    /**
     * Constructor leveraging default search request but with the vectorize options.
     * @param queryEmbedding
     *      vector or left empty to use the vectorize field
     * @param query
     *      the text fragment where embedding are computed on the spot during the search
     * @param maxResults
     *      maximum number of results to return
     * @param minScore
     *     minimum score to return
     * @param filter
     *      filter to apply
     */
    public EmbeddingSearchRequestAstra(Embedding queryEmbedding, String query, Integer maxResults, Double minScore, Filter filter) {
        super(queryEmbedding != null ? queryEmbedding : Embedding.from(new float[0]), maxResults, minScore, filter);
        this.query = query;
    }

    /**
     * Retrieve the vectorize value.
     *
     * @return
     *      current vectorize value.
     */
    public String query() {
        return query;
    }

    /**
     * Builder for the Astra Request.
     *
     * @return
     *      instance of the builder
     */
    public static EmbeddingSearchRequestAstra.Builder builderAstra() {
        return new Builder();
    }

    /**
     * Builder for the class.
     */
    public static class Builder {
        /* vectorize. */
        private String queryVectorize;
        /* embeddings. */
        private Embedding queryEmbedding;
        /* results. */
        private Integer maxResults;
        /* min scores. */
        private Double minScore;
        /* filters. */
        private Filter filter;

        /**
         * Default constructor
         */
        Builder() {
        }

        /**
         * Accessor for vectorize.
         * @param queryVectorize
         *      queryVectorize
         * @return
         *      current ref
         */
        public Builder queryVectorize(String queryVectorize) {
            this.queryVectorize = queryVectorize;
            return this;
        }

        /**
         * Accessor for queryEmbedding.
         * @param queryEmbedding
         *      queryEmbedding
         * @return
         *      current ref
         */
        public Builder queryEmbedding(Embedding queryEmbedding) {
            this.queryEmbedding = queryEmbedding;
            return this;
        }

        /**
         * Accessor for maxResults.
         * @param maxResults
         *      maxResults
         * @return
         *      current ref
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Accessor for minScore.
         * @param minScore
         *      minScore
         * @return
         *      current ref
         */
        public Builder minScore(Double minScore) {
            this.minScore = minScore;
            return this;
        }

        /**
         * Accessor for filter.
         * @param filter
         *      filter
         * @return
         *      current ref
         */
        public Builder filter(Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Operation to build.
         *
         * @return
         *      instance of the request.
         */
        public EmbeddingSearchRequestAstra build() {
            return new EmbeddingSearchRequestAstra(
                    this.queryEmbedding, this.queryVectorize,
                    this.maxResults, this.minScore, this.filter);
        }


        /** {@inheritDoc} */
        public String toString() {
            return "Search(\nqueryEmbedding=" + this.queryEmbedding
                    + "\nmaxResults=" + this.maxResults
                    + "\nminScore=" + this.minScore
                    + "\nfilter=" + this.filter + ")";
        }
    }
}
