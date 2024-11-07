package com.datastax.astra.client.core.vector;

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

import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subclass representing the vector options.
 */
@Data @NoArgsConstructor
public class VectorOptions {

    /**
     * Size of the vector.
     */
    private Integer dimension;

    /**
     * Similarity metric.
     */
    private String metric;

    /**
     * Service for vectorization
     */
    private VectorServiceOptions service;

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

