package com.datastax.astra.client.tables.definition;

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

import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionList;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionMap;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionSet;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.internal.utils.Assert;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedHashMap;

@Data
public class TableDefinition {

    private LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();

    private TablePrimaryKey primaryKey = new TablePrimaryKey();

    /**
     * Default constructor.
     */
    public TableDefinition() {
    }

    /**
     * Adds a column to the table definition.
     *
     * @param columnName the name of the column
     * @param columnDefinition the definition of the column
     * @return the updated {@link TableDefinition} instance
     * @throws IllegalArgumentException if {@code columnName} is null
     */
    public TableDefinition addColumn(String columnName, ColumnDefinition columnDefinition) {
        Assert.notNull(columnName, "Column columnName");
        columns.put(columnName, columnDefinition);
        return this;
    }

    /**
     * Adds a column to the table with a specific type.
     *
     * @param name the name of the column
     * @param type the type of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumn(String name, ColumnTypes type) {
        columns.put(name, new ColumnDefinition(type));
        return this;
    }

    /**
     * Adds a text column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnText(String name) {
        return addColumn(name, ColumnTypes.TEXT);
    }

    /**
     * Adds an ascii column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnAscii(String name) {
        return addColumn(name, ColumnTypes.ASCII);
    }

    /**
     * Adds an integer column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnInt(String name) {
        return addColumn(name, ColumnTypes.INT);
    }

    /**
     * Adds a timestamp column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnTimestamp(String name) {
        return addColumn(name, ColumnTypes.TIMESTAMP);
    }

    /**
     * Adds a boolean column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnBoolean(String name) {
        return addColumn(name, ColumnTypes.BOOLEAN);
    }

    /**
     * Adds a boolean column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnBigInt(String name) {
        return addColumn(name, ColumnTypes.BIGINT);
    }

    /**
     * Adds a blob column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnBlob(String name) {
        return addColumn(name, ColumnTypes.BLOB);
    }

    /**
     * Adds a list column to the table.
     *
     * @param name the name of the column
     * @param valueType the type of the elements in the list
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnList(String name, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionList(valueType));
        return this;
    }

    /**
     * Adds a set column to the table.
     *
     * @param name the name of the column
     * @param valueType the type of the elements in the set
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnSet(String name, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionSet(valueType));
        return this;
    }

    /**
     * Adds a map column to the table.
     *
     * @param name the name of the column
     * @param keyType the type of the keys in the map
     * @param valueType the type of the values in the map
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnMap(String name, ColumnTypes keyType, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionMap(keyType, valueType));
        return this;
    }

    /**
     * Adds a vector column to the table.
     *
     * @param name the name of the column
     * @param colDefVector the definition of the vector column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addColumnVector(String name, ColumnDefinitionVector colDefVector) {
        columns.put(name, colDefVector);
        return this;
    }

    /**
     * Adds a partition key to the table's primary key.
     *
     * @param partitionKey the name of the partition key column
     * @return the updated {@link TableDefinition} instance
     */
    public TableDefinition addPartitionBy(String partitionKey) {
        primaryKey.getPartitionBy().add(partitionKey);
        return this;
    }

    /**
     * Adds a sort column to the table's primary key.
     *
     * @param column the sort column to add
     * @return the updated {@link TableDefinition} instance
     * @throws IllegalArgumentException if {@code column} is null or invalid
     */
    public TableDefinition addPartitionSort(Sort column) {
        Assert.notNull(column, "Column");
        Assert.notNull(column.getOrder(), "column order");
        Assert.hasLength(column.getField(), "column name");
        if (primaryKey.getPartitionSort() == null) {
            primaryKey.setPartitionSort(new LinkedHashMap<>());
        }
        primaryKey.getPartitionSort().put(column.getField(), column.getOrder().getCode());
        return this;
    }

    /**
     * Sets the partition keys for the table.
     *
     * @param partitionKeys the partition keys
     * @return the updated {@link TableDefinition} instance
     * @throws IllegalArgumentException if any of the partition keys are not valid columns
     */
    public TableDefinition partitionKey(String... partitionKeys) {
        if (partitionKeys != null) {
            primaryKey.getPartitionBy().clear();
            Arrays.asList(partitionKeys).forEach(pk -> {
                if (!columns.containsKey(pk)) {
                    throw new IllegalArgumentException("Cannot create primaryKey: Column '" + pk + "' has not been found in table");
                }
                primaryKey.getPartitionBy().add(pk);
            });
        }
        return this;
    }

    /**
     * Sets the clustering columns for the table.
     *
     * @param clusteringColumns the clustering columns
     * @return the updated {@link TableDefinition} instance
     * @throws IllegalArgumentException if any of the clustering columns are not valid columns
     */
    public TableDefinition clusteringColumns(Sort... clusteringColumns) {
        if (clusteringColumns != null) {
            primaryKey.setPartitionSort(new LinkedHashMap<>());
            Arrays.asList(clusteringColumns).forEach(cc -> {
                if (!columns.containsKey(cc.getField())) {
                    throw new IllegalArgumentException("Cannot create primaryKey: Column '" + cc.getField() + "' has not been found in table");
                }
                primaryKey.getPartitionSort().put(cc.getField(), cc.getOrder().getCode());
            });
        }
        return this;
    }



}
