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

public class AstraVectorizeContentRetriever implements ContentRetriever {

    public static final String DEFAULT_DISPLAY_NAME = "Default";

    private final AstraDbEmbeddingStore embeddingStore;

    private final Integer maxResultsProvider;

    private final Double minScoreProvider;

    private final Filter filterProvider;

    private final String displayName;

    private AstraVectorizeContentRetriever(String displayName, AstraDbEmbeddingStore embeddingStore, Integer maxResults, Double minScore, Filter dynamicFilter) {
        this.displayName = (String) Utils.getOrDefault(displayName, "Default");
        this.embeddingStore = ValidationUtils.ensureNotNull(embeddingStore, "embeddingStore");
        this.maxResultsProvider = maxResults != null ? maxResults : 20;
        this.minScoreProvider = minScore != null ? minScore : 0.1;
        this.filterProvider = dynamicFilter;
    }

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

    public String toString() {
        return "AstraVectorizeContentRetriever{displayName='" + this.displayName + '\'' + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String displayName;
        private AstraDbEmbeddingStore embeddingStore;
        private Integer dynamicMaxResults;
        private Double dynamicMinScore;
        private Filter dynamicFilter;

        public Builder maxResults(Integer maxResults) {
            this.dynamicMaxResults = maxResults;
            return this;
        }

        public Builder minScore(Double minScore) {
            this.dynamicMinScore = minScore;
            return this;
        }

        public Builder filter(Filter filter) {
            this.dynamicFilter = filter;
            return this;
        }

        Builder() {
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder embeddingStore(AstraDbEmbeddingStore embeddingStore) {
            this.embeddingStore = embeddingStore;
            return this;
        }

        public AstraVectorizeContentRetriever build() {
            return new AstraVectorizeContentRetriever(this.displayName, this.embeddingStore, this.dynamicMaxResults, this.dynamicMinScore, this.dynamicFilter);
        }

        public String toString() {
            return "AstraVectorizeContentRetriever.Builder(displayName=" + this.displayName + ", embeddingStore=" + this.embeddingStore + ", dynamicMaxResults=" + this.dynamicMaxResults + ", dynamicMinScore=" + this.dynamicMinScore + ", dynamicFilter=" + this.dynamicFilter + ")";
        }
    }
}
