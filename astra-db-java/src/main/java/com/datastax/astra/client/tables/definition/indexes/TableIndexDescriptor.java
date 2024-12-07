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

import com.datastax.astra.internal.serdes.tables.RowSerializer;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a descriptor for a table index, including the table's name and associated index options.
 * This class is designed for use in scenarios such as serialization/deserialization with libraries
 * like Jackson and for method chaining in fluent-style APIs.
 */
@Getter
@Setter
public class TableIndexDescriptor {

    /**
     * The name of the table.
     */
    private String name;

    /**
     * The options defining the table's index characteristics.
     */
    private TableIndexDefinition definition;

    /**
     * Default constructor for serialization/deserialization purposes.
     */
    public TableIndexDescriptor() {
        // Constructor intentionally left blank for serialization with Jackson.
    }

    /**
     * Constructs a {@code TableIndexDescriptor} with the specified table name.
     *
     * @param name the name of the table.
     */
    public TableIndexDescriptor(String name) {
        // Constructor intentionally left blank for serialization with Jackson.
        this.name = name;
    }

    /**
     * Sets the name of the table.
     *
     * @param name the name of the table.
     * @return the current instance for method chaining.
     */
    public TableIndexDescriptor name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the index definition for the table.
     *
     * @param def the {@link TableIndexDefinition} defining the index options for the table.
     * @return the current instance for method chaining.
     */
    public TableIndexDescriptor definition(TableIndexDefinition def) {
        this.definition = def;
        return this;
    }

    /**
     * Converts the object to a string using the {@link RowSerializer}.
     *
     * @return a string representation of this {@code TableIndexDescriptor}.
     */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}
