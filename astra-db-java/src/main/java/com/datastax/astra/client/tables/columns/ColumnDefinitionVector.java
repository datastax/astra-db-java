package com.datastax.astra.client.tables.columns;

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
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ColumnDefinitionVector extends ColumnDefinition {

    /**
     * Vector dimension.
     */
    private Integer dimension;

    /**
     * Similarity metric.
     */
    private SimilarityMetric metric;

    /**
     * Service for vectorization
     */
    private VectorServiceOptions service;

    public ColumnDefinitionVector() {
        super(ColumnTypes.VECTOR);
    }

    public ColumnDefinitionVector dimension(int dimension) {
        this.dimension = dimension;
        return this;
    }

    public ColumnDefinitionVector metric(SimilarityMetric m) {
        this.metric = m;
        return this;
    }

    public ColumnDefinitionVector service(VectorServiceOptions service) {
        this.service = service;
        return this;
    }



}
