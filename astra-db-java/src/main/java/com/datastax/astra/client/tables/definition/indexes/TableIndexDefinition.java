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


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the base class for table index definitions, encapsulating common properties
 * and behaviors for index definitions, including the column being indexed and API support
 * for index-related operations.
 * <p>
 * Subclasses should extend this abstract class to implement specific types of index definitions.
 * </p>
 *
 * @param <OPTIONS> the type of options (vector, normal) for index definition.
 */
@Getter
public abstract class TableIndexDefinition<OPTIONS> {

    /**
     * The name of the column that the index is associated with.
     */
    protected TableIndexColumnDefinition column;

    /**
     * The API support configuration for the table index.
     */
    protected TableIndexDefinitionApiSupport apiSupport;

    /**
     * The Options for this particular index.
     */
    protected OPTIONS options;

    /**
     * Constructor function to create new instances in subclasses.
     */
    @JsonIgnore
    protected final Function<OPTIONS, ? extends TableIndexDefinition<OPTIONS>> constructor;

    /**
     * Constructs a new instance of {@code TableIndexDefinition} with the specified constructor function.
     *
     * @param constructor the constructor function for creating new instances of the subclass.
     */
    protected TableIndexDefinition(Function<OPTIONS, ? extends TableIndexDefinition<OPTIONS>> constructor) {
        this.constructor = constructor;
    }

    /**
     * Maps the current instance to a new instance with updated properties.
     *
     * @param updater a consumer function that updates the properties of the new instance.
     * @return a new instance with the updated properties.
     */
    protected TableIndexDefinition<OPTIONS> mapImpl(Consumer<TableIndexDefinition<OPTIONS>> updater) {
        TableIndexDefinition<OPTIONS> newInstance = constructor.apply(this.options);
        newInstance.column = this.column;
        newInstance.apiSupport = this.apiSupport;
        updater.accept(newInstance); // No need to return a value
        return newInstance;
    }

    /**
     * Sets the name of the column for the index.
     *
     * @param column the name of the column to be indexed.
     * @return a new instance with the updated column.
     */
    public TableIndexDefinition<OPTIONS> column(String column) {
        return mapImpl(def -> def.column = new TableIndexColumnDefinition(column, null));
    }

    /**
     * Sets the name of the column for the index.
     *
     * @param column the name of the column to be indexed.
     * @param type the type of the column to be indexed.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition<OPTIONS> column(String column, TableIndexMapTypes type) {
        return mapImpl(def -> def.column = new TableIndexColumnDefinition(column, type));
    }

    /**
     * Sets the options for configuring the table index.
     *
     * @param options an instance of {@link TableIndexDefinitionOptions} containing index options.
     * @return the current instance of {@code TableIndexDefinition} for method chaining.
     */
    public TableIndexDefinition<OPTIONS> options(OPTIONS options) {
        return mapImpl(def -> def.options = options);
    }

}

