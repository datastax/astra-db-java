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

import lombok.Getter;
/**
 * Represents a definition for table indices, allowing customization of various indexing options.
 * This class provides a fluent interface to configure column names and index properties
 * such as ASCII encoding, normalization, and case sensitivity.
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * {@code
 * TableIndexDefinition indexDefinition = new TableIndexDefinition()
 *     .column("username")
 *     .ascii(true)
 *     .normalize(false)
 *     .caseSensitive(true);
 * }
 * </pre>
 */
@Getter
public class TableIndexDefinition extends TableBaseIndexDefinition {

    /** Options for configuring the table index. */
    private TableIndexDefinitionOptions options;

    /**
     * Default constructor.
     */
    public TableIndexDefinition() {
    }

    /**
     * Sets the name of the column for the index.
     *
     * @param column the name of the column to be indexed.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition column(String column) {
        this.column = new TableIndexColumnDefinition(column, null);
        return this;
    }

    /**
     * Sets the name of the column for the index.
     *
     * @param column the name of the column to be indexed.
     * @param type the type of the column to be indexed.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition column(String column, TableIndexMapTypes type) {
        this.column = new TableIndexColumnDefinition(column, type);
        return this;
    }

    /**
     * Sets the options for configuring the table index.
     *
     * @param options an instance of {@link TableIndexDefinitionOptions} containing index options.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition options(TableIndexDefinitionOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Enables or disables ASCII encoding for the index.
     *
     * @param ascii {@code true} to enable ASCII encoding, {@code false} otherwise.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition ascii(boolean ascii) {
        if (this.options == null) {
            this.options = new TableIndexDefinitionOptions();
        }
        this.options.ascii(ascii);
        return this;
    }

    /**
     * Enables or disables normalization for the index.
     *
     * @param normalize {@code true} to enable normalization, {@code false} otherwise.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition normalize(boolean normalize) {
        if (this.options == null) {
            this.options = new TableIndexDefinitionOptions();
        }
        this.options.normalize(normalize);
        return this;
    }

    /**
     * Sets whether the index should be case-sensitive.
     *
     * @param caseSensitive {@code true} if the index should be case-sensitive, {@code false} otherwise.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition caseSensitive(boolean caseSensitive) {
        if (this.options == null) {
            this.options = new TableIndexDefinitionOptions();
        }
        this.options.caseSensitive(caseSensitive);
        return this;
    }
}