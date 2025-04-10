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

import com.datastax.astra.client.core.lexical.Analyzer;
import com.datastax.astra.client.core.lexical.AnalyzerTypes;
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
 * TableTextIndexDefinition vectorIndexDefinition = new TableTextIndexDefinition()
 *     .column("feature_vector")
 *     .options(new TableTextIndexDefinitionOptions());
 * }
 * </pre>
 */
public class TableTextIndexDefinition extends TableIndexDefinition<TableTextIndexDefinitionOptions> {

    /**
     * Constructs a new {@code TableVectorIndexDefinition} instance.
     */
    public TableTextIndexDefinition() {
        super(TableTextIndexDefinition::new);
    }

    /**
     * Constructs a new {@code TableVectorIndexDefinition} instance with the specified options.
     *
     * @param options the options to use for the vector index.
     */
    protected TableTextIndexDefinition(TableTextIndexDefinitionOptions options) {
        super(TableTextIndexDefinition::new);
        this.options = options;
    }

    /**
     * Setter for the column name
     *
     * @param column the name of the column to index.
     * @return self reference
     */
    @Override
    public TableTextIndexDefinition column(String column) {
        return (TableTextIndexDefinition) super.column(column);
    }

    /**
     * Setter for the column name
     *
     * @param column the name of the column to index.
     * @param type   the type of the index.
     * @return self reference
     */
    @Override
    public TableTextIndexDefinition column(String column, TableIndexMapTypes type) {
        return (TableTextIndexDefinition) super.column(column, type);
    }

    /**
     * Setter for the options
     *
     * @param options the options of the  index.
     * @return self reference
     */
    @Override
    public TableTextIndexDefinition options(TableTextIndexDefinitionOptions options) {
        return (TableTextIndexDefinition) super.options(options);
    }

    /**
     * Setter for the analyzer
     *
     * @param analyzer
     *      Value for analyzer
     * @return
     *      Self reference
     */
    public TableTextIndexDefinition analyzer(Analyzer analyzer) {
        if (options == null) {
            this.options = new TableTextIndexDefinitionOptions();
        }
        this.options.analyzer = analyzer;
        return this;
    }

    /**
     * Setter for the analyzer with a type
     *
     * @param analyzerTypes
     *      Type of the analyzer
     * @return
     *      Self reference
     */
    public TableTextIndexDefinition analyzer(AnalyzerTypes analyzerTypes) {
        if (options == null) {
            this.options = new TableTextIndexDefinitionOptions();
        }
        this.options.analyzer = new Analyzer(analyzerTypes);
        return this;
    }

}
