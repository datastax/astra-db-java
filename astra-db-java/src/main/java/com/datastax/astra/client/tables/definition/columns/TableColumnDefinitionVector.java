package com.datastax.astra.client.tables.definition.columns;

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

/**
 * Represents a column definition for vector-based data.
 * Extends {@link TableColumnDefinition} to include properties and methods
 * specific to vector data such as dimension, similarity metric, and vectorization service.
 * <p>
 * This class facilitates the configuration of vector columns in a data schema.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnDefinitionVector vectorColumn = new ColumnDefinitionVector()
 *      .dimension(128)
 *      .metric(SimilarityMetric.COSINE);
 * }
 * </pre>
 */
@Getter
@Setter
public class TableColumnDefinitionVector extends TableColumnDefinition {

    /**
     * The dimension of the vector.
     * Represents the number of components in the vector space.
     */
    private Integer dimension;

    /**
     * The service options for vectorization.
     * Configures how vectors are generated or processed.
     */
    private VectorServiceOptions service;

    /**
     * Constructs a new {@code ColumnDefinitionVector} instance
     * with the column type set to {@link TableColumnTypes#VECTOR}.
     */
    public TableColumnDefinitionVector() {
        super(TableColumnTypes.VECTOR);
    }

    /**
     * Sets the dimension of the vector and returns the updated instance.
     *
     * @param dimension the dimension of the vector
     * @return this {@code ColumnDefinitionVector} instance
     */
    public TableColumnDefinitionVector dimension(int dimension) {
        this.dimension = dimension;
        return this;
    }

    /**
     * Sets the vectorization service options and returns the updated instance.
     *
     * @param service the vectorization service options
     * @return this {@code ColumnDefinitionVector} instance
     */
    public TableColumnDefinitionVector service(VectorServiceOptions service) {
        this.service = service;
        return this;
    }
}