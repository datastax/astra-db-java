package com.datastax.astra.langchain4j.rag;

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

    public static final Function<Query, Integer> DEFAULT_MAX_RESULTS = (query) -> {
        return 3;
    };
    public static final Function<Query, Double> DEFAULT_MIN_SCORE = (query) -> {
        return 0.0;
    };
    public static final Function<Query, Filter> DEFAULT_FILTER = (query) -> {
        return null;
    };

    public static final String DEFAULT_DISPLAY_NAME = "Default";

    private final AstraDbEmbeddingStore embeddingStore;

    private final Function<Query, Integer> maxResultsProvider;

    private final Function<Query, Double> minScoreProvider;

    private final Function<Query, Filter> filterProvider;

    private final String displayName;

    public AstraVectorizeContentRetriever(AstraDbEmbeddingStore embeddingStore) {
        this("Default", embeddingStore, DEFAULT_MAX_RESULTS, DEFAULT_MIN_SCORE, DEFAULT_FILTER);
    }

    public AstraVectorizeContentRetriever(AstraDbEmbeddingStore embeddingStore, int maxResults) {
        this("Default", embeddingStore, (query) -> {
            return maxResults;
        }, DEFAULT_MIN_SCORE, DEFAULT_FILTER);
    }

    public AstraVectorizeContentRetriever(AstraDbEmbeddingStore embeddingStore, Integer maxResults, Double minScore) {
        this("Default", embeddingStore, (query) -> {
            return maxResults;
        }, (query) -> {
            return minScore;
        }, DEFAULT_FILTER);
    }

    private AstraVectorizeContentRetriever(String displayName, AstraDbEmbeddingStore embeddingStore, Function<Query, Integer> dynamicMaxResults, Function<Query, Double> dynamicMinScore, Function<Query, Filter> dynamicFilter) {
        this.displayName = (String) Utils.getOrDefault(displayName, "Default");
        this.embeddingStore = ValidationUtils.ensureNotNull(embeddingStore, "embeddingStore");
        this.maxResultsProvider = Utils.getOrDefault(dynamicMaxResults, DEFAULT_MAX_RESULTS);
        this.minScoreProvider = Utils.getOrDefault(dynamicMinScore, DEFAULT_MIN_SCORE);
        this.filterProvider = Utils.getOrDefault(dynamicFilter, DEFAULT_FILTER);
    }

    public static AstraVectorizeContentRetriever from(AstraDbEmbeddingStore embeddingStore) {
        return builder().embeddingStore(embeddingStore).build();
    }

    public List<Content> retrieve(Query query) {
        EmbeddingSearchRequestAstra searchRequest = new EmbeddingSearchRequestAstra(null,
                query.text(),
                this.maxResultsProvider.apply(query),
                this.minScoreProvider.apply(query),
                this.filterProvider.apply(query));

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
        private Function<Query, Integer> dynamicMaxResults;
        private Function<Query, Double> dynamicMinScore;
        private Function<Query, Filter> dynamicFilter;

        public Builder maxResults(Integer maxResults) {
            if (maxResults != null) {
                this.dynamicMaxResults = (query) -> {
                    return ValidationUtils.ensureGreaterThanZero(maxResults, "maxResults");
                };
            }

            return this;
        }

        public Builder minScore(Double minScore) {
            if (minScore != null) {
                this.dynamicMinScore = (query) -> {
                    return ValidationUtils.ensureBetween(minScore, 0.0, 1.0, "minScore");
                };
            }

            return this;
        }

        public Builder filter(Filter filter) {
            if (filter != null) {
                this.dynamicFilter = (query) -> {
                    return filter;
                };
            }

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

        public Builder dynamicMaxResults(Function<Query, Integer> dynamicMaxResults) {
            this.dynamicMaxResults = dynamicMaxResults;
            return this;
        }

        public Builder dynamicMinScore(Function<Query, Double> dynamicMinScore) {
            this.dynamicMinScore = dynamicMinScore;
            return this;
        }

        public Builder dynamicFilter(Function<Query, Filter> dynamicFilter) {
            this.dynamicFilter = dynamicFilter;
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
