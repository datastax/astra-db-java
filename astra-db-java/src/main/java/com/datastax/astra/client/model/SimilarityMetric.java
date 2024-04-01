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

import lombok.Getter;

/**
 * Similarity metric.
 */
@Getter
public enum SimilarityMetric {

    /** Distance with cosine. */
    COSINE("cosine"),
    /** Distance with Euclidean (L2). */
    EUCLIDEAN("euclidean"),
    /**Distance with dot+product (L1). */
    DOT_PRODUCT("dot_product");

    /** Value for the similarity metric. */
    final String value;

    /**
     * Constructor.
     *
     * @param value
     *      value
     */
    SimilarityMetric(String value) {
        this.value = value;
    }

    /**
     * Build from the key.
     *
     * @param value
     *      similarity value
     * @return
     *      similarity enum
     */
    public static SimilarityMetric fromValue(String value) {
        for (SimilarityMetric metric : SimilarityMetric.values()) {
            if (metric.getValue().equalsIgnoreCase(value)) {
                return metric;
            }
        }
        throw new IllegalArgumentException("Unknown SimilarityMetric: " + value);
    }
}
