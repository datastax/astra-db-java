package com.datastax.astra.client.tables.definition.indexes;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a column definition for a table index, including the column's name and type.
 * This class is designed for use in scenarios such as serialization/deserialization with libraries
 * like Jackson and for method chaining in fluent-style APIs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableIndexColumnDefinition {

    /**
     * The name of the column.
     */
    String name;

    /**
     * The type of the column.
     */
    TableIndexMapTypes type;

    /**
     * Constructor that accepts a column name.
     *
     * @param name
     *      the name of the column.
     */
    public TableIndexColumnDefinition(String name) {
        this.name = name;
    }
}
