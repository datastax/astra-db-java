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
 * Represents a column definition for a set type in a database schema.
 * Extends {@link ColumnDefinition} to include details about the type of elements stored in the set.
 * <p>
 * This class is used to configure columns of type {@link ColumnTypes#SET}, allowing the specification
 * of the data type for the values stored in the set.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnDefinitionSet setColumn = new ColumnDefinitionSet(ColumnTypes.TEXT);
 * ColumnTypes valueType = setColumn.getValueType();
 * }
 * </pre>
 */
@Getter @Setter
public class ColumnDefinitionSet extends ColumnDefinition {

    /**
     * The data type of the values stored in the set.
     */
    private ColumnTypes valueType;

    /**
     * Constructs a new {@code ColumnDefinitionSet} instance with the column type set to {@link ColumnTypes#SET}.
     */
    public ColumnDefinitionSet() {
        super(ColumnTypes.SET);
    }

    /**
     * Constructs a new {@code ColumnDefinitionSet} instance with the specified value type.
     *
     * @param valueType the data type of the values in the set
     */
    public ColumnDefinitionSet(ColumnTypes valueType) {
        this();
        this.valueType = valueType;
    }
}
