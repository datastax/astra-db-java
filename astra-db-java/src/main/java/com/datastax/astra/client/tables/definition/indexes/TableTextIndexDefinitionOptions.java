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
import lombok.Data;

/**
 * Represents the options for a text index definition in a table.
 * This class is designed for use in scenarios such as serialization/deserialization with libraries
 * like Jackson and for method chaining in fluent-style APIs.
 */
@Data
public class TableTextIndexDefinitionOptions {

    /**
     * The analyzer to use for the text index.
     */
    Analyzer analyzer;

    /**
     * Default constructor.
     */
    public TableTextIndexDefinitionOptions() {
    }

    /**
     * Constructor that accepts an analyzer.
     *
     * @param analyzer
     *      the analyzer to use.
     * @return
     *      the current instance of {@link TableTextIndexDefinitionOptions}.
     */
    public TableTextIndexDefinitionOptions analyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    /**
     * Constructor that accepts an analyzer type.
     *
     * @param analyzerType
     *      the analyzer type to use.
     * @return
     *      the current instance of {@link TableTextIndexDefinitionOptions}.
     */
    public TableTextIndexDefinitionOptions analyzer(AnalyzerTypes analyzerType) {
        this.analyzer = new Analyzer(analyzerType);
        return this;
    }
}

