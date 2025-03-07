package com.datastax.astra.client.core.lexical;

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
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Subclass representing the vector options.
 */
@Setter
@Accessors(fluent = true, chain = true)
public class LexicalOptions {

    /**
     * Size of the vector.
     */
    private boolean enabled;

    /**
     * Similarity metric.
     */
    private LexicalAnalyzers analyzer;

    /**
     * Service for vectorization
     */
    private List<LexicalFilters> filters;

    /**
     * Default constructor.
     */
    public LexicalOptions() {}

    /**
     * Gets enabled
     *
     * @return value of enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets analyzer
     *
     * @return value of analyzer
     */
    public LexicalAnalyzers getAnalyzer() {
        return analyzer;
    }

    /**
     * Gets filters
     *
     * @return value of filters
     */
    public List<LexicalFilters> getFilters() {
        return filters;
    }
}

