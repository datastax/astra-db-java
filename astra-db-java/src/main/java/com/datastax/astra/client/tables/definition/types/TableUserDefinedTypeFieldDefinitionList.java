package com.datastax.astra.client.tables.definition.types;

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

import com.datastax.astra.client.tables.definition.columns.TableColumnDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a column definition for a list type in a database schema.
 * Extends {@link TableColumnDefinition} to include details about the type of elements stored in the list.
 * <p>
 * This class is used to configure columns of type {@link TableColumnTypes#LIST}, allowing the specification
 * of the data type for the values stored in the list.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnDefinitionList listColumn = new ColumnDefinitionList(ColumnTypes.TEXT);
 * ColumnTypes valueType = listColumn.getValueType();
 * }
 * </pre>
 */
@Getter
@Setter
public class TableUserDefinedTypeFieldDefinitionList extends TableUserDefinedTypeFieldDefinition {

    /**
     * The data type of the values stored in the list.
     */
    private TableUserDefinedTypeFieldTypes valueType;

    /**
     * Constructs a new {@code ColumnDefinitionList} instance with the column type set to {@link TableColumnTypes#LIST}.
     */
    public TableUserDefinedTypeFieldDefinitionList() {
        super(TableUserDefinedTypeFieldTypes.UNSUPPORTED);
    }

    /**
     * Constructs a new {@code ColumnDefinitionList} instance with the specified value type.
     *
     * @param valueType the data type of the values in the list
     */
    public TableUserDefinedTypeFieldDefinitionList(TableUserDefinedTypeFieldTypes valueType) {
        this();
        this.valueType = valueType;
    }
}
