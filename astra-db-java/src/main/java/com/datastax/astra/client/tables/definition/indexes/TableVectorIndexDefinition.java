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

/**
 * Represents a definition for table vector indices, allowing configuration of
 * vector-specific options such as similarity metrics and source models.
 * This class provides a fluent interface for building vector index definitions.
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * TableVectorIndexDefinition vectorIndexDefinition = new TableVectorIndexDefinition()
 *     .column("feature_vector")
 *     .metric(SimilarityMetric.COSINE)
 *     .sourceModel("model_name")
 *     .options(new TableVectorIndexDefinitionOptions());
 * }
 * </pre>
 */
public class TableVectorIndexDefinition extends TableIndexDefinition<TableVectorIndexDefinitionOptions> {

    /** Initial source model. */
    public static final String SOURCE_MODEL_ADA002 = "ada002";
    /** Initial source model. */
    public static final String SOURCE_MODEL_BERT = "bert";
    /** Initial source model. */
    public static final String SOURCE_MODEL_COHERE_V3 = "cohere-v3";
    /** Initial source model. */
    public static final String SOURCE_MODEL_GECKO = "gecko";
    /** Initial source model. */
    public static final String SOURCE_MODEL_NV_QA_4 = "nv-qa-4";
    /** Initial source model. */
    public static final String SOURCE_MODEL_OPENAI_V3_LARGE = "openai-v3-large";
    /** Initial source model. */
    public static final String SOURCE_MODEL_OPENAI_V3_SMALL = "openai-v3-small";

    /**
     * Constructs a new {@code TableVectorIndexDefinition} instance.
     */
    public TableVectorIndexDefinition() {
        super(TableVectorIndexDefinition::new);
    }

    /**
     * Constructs a new {@code TableVectorIndexDefinition} instance with the specified options.
     *
     * @param options the options to use for the vector index.
     */
    protected TableVectorIndexDefinition(TableVectorIndexDefinitionOptions options) {
        super(TableVectorIndexDefinition::new);
        this.options = options;
    }

    @Override
    public TableVectorIndexDefinition column(String column) {
        return (TableVectorIndexDefinition) super.column(column);
    }

    @Override
    public TableVectorIndexDefinition column(String column, TableIndexMapTypes type) {
        return (TableVectorIndexDefinition) super.column(column, type);
    }

    @Override
    public TableVectorIndexDefinition options(TableVectorIndexDefinitionOptions options) {
        return (TableVectorIndexDefinition) super.options(options);
    }

    /**
     * Configures the similarity metric to be used for the vector index.
     *
     * @param metric an instance of {@link SimilarityMetric} representing the similarity metric.
     * @return the current instance of {@code TableVectorIndexDefinition} for method chaining.
     */
    public TableVectorIndexDefinition metric(SimilarityMetric metric) {
        if (options == null) {
            this.options = new TableVectorIndexDefinitionOptions();
        }
        this.options.metric = metric.getValue();
        return this;
    }

    /**
     * Sets the source model for the vector index.
     *
     * @param sourceModel the name of the source model to be associated with the vector index.
     * @return the current instance of {@code TableVectorIndexDefinition} for method chaining.
     */
    public TableVectorIndexDefinition sourceModel(String sourceModel) {
        if (options == null) {
            this.options = new TableVectorIndexDefinitionOptions();
        }
        this.options.sourceModel = sourceModel;
        return this;
    }

}
