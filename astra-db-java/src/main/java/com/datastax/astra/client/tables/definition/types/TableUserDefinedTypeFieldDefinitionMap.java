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
 * Represents a column definition for a map type in a database schema.
 * Extends {@link TableColumnDefinition} to include specific details about the key and value types
 * for map columns.
 * <p>
 * This class facilitates the configuration of map columns, allowing specification of
 * the types of keys and values stored in the map.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnDefinitionMap mapColumn = new ColumnDefinitionMap(ColumnTypes.TEXT, ColumnTypes.INT);
 * ColumnTypes keyType = mapColumn.getKeyType();
 * ColumnTypes valueType = mapColumn.getValueType();
 * }
 * </pre>
 */
@Getter
@Setter
public class TableUserDefinedTypeFieldDefinitionMap extends TableUserDefinedTypeFieldDefinition {

    /**
     * The data type of the keys in the map.
     */
    private TableUserDefinedTypeFieldTypes keyType;

    /**
     * The data type of the values in the map.
     */
    private TableUserDefinedTypeFieldTypes valueType;

    /**
     * Constructs a new {@code ColumnDefinitionMap} instance with the column type set to {@link TableColumnTypes#MAP}.
     */
    public TableUserDefinedTypeFieldDefinitionMap() {
        super(TableUserDefinedTypeFieldTypes.UNSUPPORTED);
    }

    /**
     * Constructs a new {@code ColumnDefinitionMap} instance with the specified key and value types.
     *
     * @param keyType   the data type of the keys in the map
     * @param valueType the data type of the values in the map
     */
    public TableUserDefinedTypeFieldDefinitionMap(TableUserDefinedTypeFieldTypes keyType, TableUserDefinedTypeFieldTypes valueType) {
        this();
        this.keyType = keyType;
        this.valueType = valueType;
    }
}
