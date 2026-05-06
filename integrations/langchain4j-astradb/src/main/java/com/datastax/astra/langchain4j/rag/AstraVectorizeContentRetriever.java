package com.datastax.astra.langchain4j.rag;

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

import com.datastax.astra.langchain4j.store.embedding.AstraDbEmbeddingStore;
import com.datastax.astra.langchain4j.store.embedding.EmbeddingSearchRequestAstra;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.filter.Filter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Retrieve Content for Astra with Vectorize in.
 */
public class AstraVectorizeContentRetriever implements ContentRetriever {

    /** display name. */
    public static final String DEFAULT_DISPLAY_NAME = "Default";

    /** embedding store. */
    private final AstraDbEmbeddingStore embeddingStore;

    /** result provider. */
    private final Integer maxResultsProvider;

    /** min score provider. */
    private final Double minScoreProvider;

    /** filter provider. */
    private final Filter filterProvider;

    /** display name. */
    private final String displayName;

    /**
     * Hidden constructor.
     *
     * @param displayName
     *      display name
     * @param embeddingStore
     *      embedding store
     * @param maxResults
     *      max results
     * @param minScore
     *      min score
     * @param dynamicFilter
     *      dynamic filter
     */
    private AstraVectorizeContentRetriever(String displayName, AstraDbEmbeddingStore embeddingStore, Integer maxResults, Double minScore, Filter dynamicFilter) {
        this.displayName = (String) Utils.getOrDefault(displayName, "Default");
        this.embeddingStore = ValidationUtils.ensureNotNull(embeddingStore, "embeddingStore");
        this.maxResultsProvider = maxResults != null ? maxResults : 20;
        this.minScoreProvider = minScore != null ? minScore : 0.1;
        this.filterProvider = dynamicFilter;
    }

    /**
     * Retrieve information.
     *
     * @param query
     *      query
     * @return
     *      list of contents
     */
    public List<Content> retrieve(Query query) {

        EmbeddingSearchRequestAstra searchRequest = EmbeddingSearchRequestAstra
                .builderAstra()
                .queryVectorize(query.text())
                .filter(filterProvider)
                .minScore(minScoreProvider)
                .maxResults(maxResultsProvider)
                .build();

        return embeddingStore.search(searchRequest).matches()
                .stream().map(EmbeddingMatch::embedded)
                .map(Content::from)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AstraVectorizeContentRetriever{displayName='" + this.displayName + '\'' + '}';
    }

    /**
     * Instance a builder.
     *
     * @return
     *      instance of the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder.
     */
    public static class Builder {
        /** display name. */
        private String displayName;

        /** embedding store. */
        private AstraDbEmbeddingStore embeddingStore;

        /** result provider. */
        private Integer dynamicMaxResults;

        /** dynamic Min Score. */
        private Double dynamicMinScore;

        /** dynamic Filter. */
        private Filter dynamicFilter;

        /**
         * Default constructor.
         */
        Builder() {
        }

        /**
         * Accessor for maxResults.
         * @param maxResults
         *      maxResults
         * @return
         *      current ref
         */
        public Builder maxResults(Integer maxResults) {
            this.dynamicMaxResults = maxResults;
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
            this.dynamicMinScore = minScore;
            return this;
        }

        /**
         * Accessor for filter.
         *
         * @param filter
         *      filter
         * @return
         *      current ref
         */
        public Builder filter(Filter filter) {
            this.dynamicFilter = filter;
            return this;
        }

        /**
         * Accessor for displayName.
         * @param displayName
         *      displayName
         * @return
         *      current ref
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Accessor for embeddingStore.
         * @param embeddingStore
         *      embeddingStore
         * @return
         *      current ref
         */
        public Builder embeddingStore(AstraDbEmbeddingStore embeddingStore) {
            this.embeddingStore = embeddingStore;
            return this;
        }

        /**
         * Operation to build the component
         * @return
         *      instance of the components
         */
        public AstraVectorizeContentRetriever build() {
            return new AstraVectorizeContentRetriever(
                    this.displayName,
                    this.embeddingStore,
                    this.dynamicMaxResults,
                    this.dynamicMinScore,
                    this.dynamicFilter);
        }

        /** {@inheritDoc} */
        public String toString() {
            return "AstraVectorizeContentRetriever.Builder(displayName=" + this.displayName + ", embeddingStore=" + this.embeddingStore + ", dynamicMaxResults=" + this.dynamicMaxResults + ", dynamicMinScore=" + this.dynamicMinScore + ", dynamicFilter=" + this.dynamicFilter + ")";
        }
    }
}
