package com.datastax.astra.client.tables.definition.indexes;

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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for defining vector-based index configurations for tables.
 * This class provides configuration settings for a vector index, such as the similarity metric
 * and the source model. The use of {@code String} allows flexibility in specifying custom metrics or models.
 */
@Data
public class TableVectorIndexDefinitionOptions {

    /**
     * The similarity metric used for the vector index.
     * Example metrics might include cosine similarity, Euclidean distance, etc.
     */
    String metric;

    /**
     * The source model used for vector computations or embedding generation.
     */
    String sourceModel;

    /**
     * Default constructor.
     */
    public TableVectorIndexDefinitionOptions() {
    }

    /**
     * Sets the similarity metric for the vector index.
     *
     * @param metric the {@link SimilarityMetric} to be used for the index.
     * @return the current instance for method chaining.
     */
    public TableVectorIndexDefinitionOptions metric(SimilarityMetric metric) {
        this.metric = metric.getValue();
        return this;
    }

    /**
     * Sets the source model for the vector index.
     *
     * @param sourceModel the name or identifier of the model used for vector computations.
     * @return the current instance for method chaining.
     */
    public TableVectorIndexDefinitionOptions sourceModel(String sourceModel) {
        this.sourceModel = sourceModel;
        return this;
    }
}

