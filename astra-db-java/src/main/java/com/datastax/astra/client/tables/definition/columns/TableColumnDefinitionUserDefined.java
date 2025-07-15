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

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a column definition for a user Defined Type (UDT) in a database schema.
 * Extends {@link TableColumnDefinition} to include details about the type of elements stored in the list.
 * <p>
 * This class is used to configure columns of type {@link TableColumnTypes#USER_DEFINED}, allowing the specification
 * of the data type for user defined values stored in the column.
 * </p>
 */
@Getter
@Setter
public class TableColumnDefinitionUserDefined extends TableColumnDefinition {

    /**
     * The data type of the values stored in the list.
     */
    private String udtName;

    /**
     * Constructs a new {@code ColumnDefinitionList} instance with the column type set to {@link TableColumnTypes#LIST}.
     */
    public TableColumnDefinitionUserDefined() {
        super(TableColumnTypes.USER_DEFINED);
    }

    /**
     * Constructs a new {@code TableColumnDefinitionUserDefined} instance with the specified value type.
     *
     * @param udtName the name of the user defined type (UDT)
     */
    public TableColumnDefinitionUserDefined(String udtName) {
        this();
        this.udtName = udtName;
    }
}
