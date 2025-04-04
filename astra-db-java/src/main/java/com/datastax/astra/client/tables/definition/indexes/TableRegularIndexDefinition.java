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
public class TableRegularIndexDefinition extends TableIndexDefinition<TableIndexDefinitionOptions> {

    /**
     * Default constructor.
     */
    public TableRegularIndexDefinition() {
        super(TableRegularIndexDefinition::new);
    }

    /**
     * Constructor that accepts a set of index options.
     *
     * @param options the index options to use.
     */
    protected TableRegularIndexDefinition(TableIndexDefinitionOptions options) {
        super(TableRegularIndexDefinition::new);
        this.options = options;
    }

    @Override
    public TableRegularIndexDefinition column(String column) {
        return (TableRegularIndexDefinition) super.column(column);
    }

    @Override
    public TableRegularIndexDefinition column(String column, TableIndexMapTypes type) {
        return (TableRegularIndexDefinition) super.column(column, type);
    }

    @Override
    public TableRegularIndexDefinition options(TableIndexDefinitionOptions options) {
        return (TableRegularIndexDefinition) super.options(options);
    }

    /**
     * Enables or disables ASCII encoding for the index.
     *
     * @param ascii {@code true} to enable ASCII encoding, {@code false} otherwise.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableRegularIndexDefinition ascii(boolean ascii) {
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
    public TableRegularIndexDefinition normalize(boolean normalize) {
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
    public TableRegularIndexDefinition caseSensitive(boolean caseSensitive) {
        if (this.options == null) {
            this.options = new TableIndexDefinitionOptions();
        }
        this.options.caseSensitive(caseSensitive);
        return this;
    }

}