package com.datastax.astra.client.model.tables;

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

import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.VectorServiceOptions;
import com.datastax.astra.client.model.query.Sort;
import com.datastax.astra.client.model.tables.columns.ColumnDefinition;
import com.datastax.astra.client.model.tables.columns.ColumnDefinitionList;
import com.datastax.astra.client.model.tables.columns.ColumnDefinitionMap;
import com.datastax.astra.client.model.tables.columns.ColumnDefinitionSet;
import com.datastax.astra.client.model.tables.columns.ColumnDefinitionVector;
import com.datastax.astra.client.model.tables.columns.ColumnTypes;
import com.datastax.astra.internal.utils.Assert;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.LinkedHashMap;

@Data @NoArgsConstructor
public class TableDefinition {

    private LinkedHashMap<String, ColumnDefinition> columns = new LinkedHashMap<>();

    private PrimaryKey primaryKey = new PrimaryKey();

    public TableDefinition addColumn(String columnName, ColumnDefinition columnDefinition) {
        Assert.notNull(columnName, "Column columnName");
        columns.put(columnName, columnDefinition);
        return this;
    }

    public TableDefinition addColumn(String name, ColumnTypes type) {
        columns.put(name, new ColumnDefinition(type));
        return this;
    }

    public TableDefinition addColumnText(String name) {
        return addColumn(name, ColumnTypes.TEXT);
    }

    public TableDefinition addColumnInt(String name) {
        return addColumn(name, ColumnTypes.INT);
    }

    public TableDefinition addColumnBoolean(String name) {
        return addColumn(name, ColumnTypes.BOOLEAN);
    }

    public TableDefinition addColumnList(String name, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionList(valueType));
        return this;
    }

    public TableDefinition addColumnSet(String name, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionSet(valueType));
        return this;
    }

    public TableDefinition addColumnMap(String name,  ColumnTypes keyType, ColumnTypes valueType) {
        columns.put(name, new ColumnDefinitionMap(keyType, valueType));
        return this;
    }

    public TableDefinition addColumnVector(String name, Integer dimension) {
        return addColumnVector(name, dimension, null, null);
    }

    public TableDefinition addColumnVector(String name, Integer dimension, SimilarityMetric metric) {
        return addColumnVector(name, dimension, metric, null);
    }

    public TableDefinition addColumnVector(String name, Integer dimension, SimilarityMetric metric, VectorServiceOptions service) {
        ColumnDefinitionVector colDefVector = new ColumnDefinitionVector();
        colDefVector.setDimension(dimension);
        if (metric != null) {
            colDefVector.setMetric(metric.getValue());
        }
        if (service != null) {
            colDefVector.setService(service);
        }
        columns.put(name,colDefVector);
        return this;
    }

    public TableDefinition withPartitionKey(String... partitionKeys) {
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

    public TableDefinition withClusteringColumns(Sort... clusteringColumns) {
        if (clusteringColumns != null) {
            primaryKey.setPartitionSort(new LinkedHashMap<>());
            Arrays.asList(clusteringColumns).forEach(cc -> {
                    if (!columns.containsKey(cc.getField())) {
                        throw new IllegalArgumentException("Cannot create primaryKey: Column '" + cc.getField() + "' has not been found in table");
                    }
                    primaryKey.getPartitionSort().put(cc.getField(), cc.getOrder().getCode());
               }
            );
        }
        return this;
    }



}
