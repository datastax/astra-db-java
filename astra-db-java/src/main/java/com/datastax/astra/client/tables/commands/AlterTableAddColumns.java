package com.datastax.astra.client.tables.commands;

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

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionList;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionListUserDefined;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionMap;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionMapUserDefined;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionSet;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionSetUserDefined;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.USERDEFINED;

/**
 * Represents an operation to add columns to an existing table in a database schema.
 * This class implements the {@link AlterTableOperation} interface, providing functionality
 * to define new columns and their types for the "alter table add" operation.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTableAddColumns operation = new AlterTableAddColumns()
 *     .ifNotExists()
 *     .addColumn("name", ColumnTypes.TEXT)
 *     .addColumnInt("age")
 *     .addColumnMap("attributes", ColumnTypes.TEXT, ColumnTypes.TEXT);
 * }
 * </pre>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Supports adding columns with various data types, including primitive, list, set, and map types.</li>
 *   <li>Chainable methods for building operations fluently.</li>
 *   <li>Ability to specify the "IF NOT EXISTS" clause to avoid errors if the column already exists.</li>
 * </ul>
 */
@Setter @Getter
public final class AlterTableAddColumns implements AlterTableOperation {

    /**
     * A map of column names to their definitions.
     * The map preserves the order of added columns.
     */
    LinkedHashMap<String, TableColumnDefinition> columns = new LinkedHashMap<>();

    /**
     * Constructs a new {@code AlterTableAddColumns} instance.
     */
    public AlterTableAddColumns() {
    }

    /**
     * Returns the name of this operation.
     * Always returns "add" for this operation type.
     *
     * @return the operation name.
     */
    @Override
    public String getOperationName() {
        return "add";
    }

    /**
     * Adds a column with the specified name and type to the table.
     *
     * @param name the name of the column.
     * @param type the type of the column.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumn(String name, TableColumnTypes type) {
        columns.put(name, new TableColumnDefinition(type));
        return this;
    }

    /**
     * Adds a user defined type (udt) column.
     *
     * @param name the name of the column
     * @param udtName the type of the udt
     * @return the updated {@link TableDefinition} instance
     */
    public AlterTableAddColumns addColumnUserDefinedType(String name, String udtName) {
        Assert.hasLength(name, "name");
        Assert.hasLength(udtName, "udtName");
        TableColumnDefinition udtDef = new TableColumnDefinition(USERDEFINED);
        udtDef.setUdtName(udtName);
        columns.put(name, udtDef);
        return this;
    }

    /**
     * Adds a list column to the table.
     *
     * @param name the name of the column
     * @param udtName the name of the Udt
     * @return the updated {@link TableDefinition} instance
     */
    public AlterTableAddColumns addColumnListUserDefinedType(String name, String udtName) {
        columns.put(name, new TableColumnDefinitionListUserDefined(udtName));
        return this;
    }

    /**
     * Adds a list column to the table.
     *
     * @param name the name of the column
     * @param udtName the name of the Udt
     * @return the updated {@link TableDefinition} instance
     */
    public AlterTableAddColumns addColumnSetUserDefinedType(String name, String udtName) {
        columns.put(name, new TableColumnDefinitionSetUserDefined(udtName));
        return this;
    }

    /**
     * Adds a list column to the table.
     *
     * @param name the name of the column
     * @param udtName the name of the Udt
     * @return the updated {@link TableDefinition} instance
     */
    public AlterTableAddColumns addColumnMapUserDefinedType(String name, String udtName, TableColumnTypes keytype) {
        columns.put(name, new TableColumnDefinitionMapUserDefined(udtName, keytype));
        return this;
    }

    /**
     * Adds a column defined by a {@link TableColumnDefinitionVector}.
     *
     * @param name the name of the column.
     * @param cdv  the column definition vector.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnVector(String name, TableColumnDefinitionVector cdv) {
        columns.put(name, cdv);
        return this;
    }

    /**
     * Adds a column with the TEXT type.
     *
     * @param name the name of the column.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnText(String name) {
        return addColumn(name, TableColumnTypes.TEXT);
    }

    /**
     * Adds a column with the INT type.
     *
     * @param name the name of the column.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnInt(String name) {
        return addColumn(name, TableColumnTypes.INT);
    }

    /**
     * Adds a column with the BOOLEAN type.
     *
     * @param name the name of the column.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnBoolean(String name) {
        return addColumn(name, TableColumnTypes.BOOLEAN);
    }

    /**
     * Adds a column with the LIST type, where the list items are of the specified value type.
     *
     * @param name      the name of the column.
     * @param valueType the type of the values in the list.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnList(String name, TableColumnTypes valueType) {
        columns.put(name, new TableColumnDefinitionList(valueType));
        return this;
    }

    /**
     * Adds a column with the SET type, where the set items are of the specified value type.
     *
     * @param name      the name of the column.
     * @param valueType the type of the values in the set.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnSet(String name, TableColumnTypes valueType) {
        columns.put(name, new TableColumnDefinitionSet(valueType));
        return this;
    }

    /**
     * Adds a column with the MAP type, where the keys and values are of the specified types.
     *
     * @param name     the name of the column.
     * @param keyType  the type of the keys in the map.
     * @param valueType the type of the values in the map.
     * @return the current instance for chaining.
     */
    public AlterTableAddColumns addColumnMap(String name, TableColumnTypes keyType, TableColumnTypes valueType) {
        columns.put(name, new TableColumnDefinitionMap(keyType, valueType));
        return this;
    }
}
