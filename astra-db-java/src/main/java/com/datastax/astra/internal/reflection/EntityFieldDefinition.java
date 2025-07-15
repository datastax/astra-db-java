package com.datastax.astra.internal.reflection;

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

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import com.datastax.astra.client.tables.mapping.KeyValue;
import com.fasterxml.jackson.databind.JavaType;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Definition of a field in an entity.
 */
@Data
public class EntityFieldDefinition {

    // --- Java Types --
    private String   name;
    private Class<?> type;
    private Method   getter;
    private Method   setter;
    private Class<?> genericValueType;
    private Class<?> genericKeyType;
    private JavaType javaType;

    // --- Table Hints --

    // @Column
    private String      columnName;
    private TableColumnTypes columnType;
    private TableColumnTypes valueType;
    private TableColumnTypes keyType;

    // @UdtField
    private String        udtName;
    private String        udtFieldName;
    private TableUserDefinedTypeFieldTypes udtFieldType;
    private TableUserDefinedTypeFieldTypes udtFieldValueType;
    private TableUserDefinedTypeFieldTypes udtFieldKeyType;

    // @ColumnVector
    private Integer             vectorDimension;
    private String              vectorServiceProvider;
    private String              vectorModelName;
    private String              vectorSourceModel;
    private Map<String, String> vectorAuthentication;
    private Map<String, String> vectorParameters;
    private SimilarityMetric    similarityMetric;

    // @PartitionBy (partition key
    private Integer     partitionByPosition;

    // @PartitionSort(clustering key)
    private Integer     partitionSortPosition;
    private SortOrder   partitionSortOrder;

    /**
     * Default constructor.
     */
    public EntityFieldDefinition() {}

    /**
     * Constructor with field name.
     *
     * @param columnVector
     *      annotation on a field
     * @return value as a map
     */
    public static Map<String, String> toMap(KeyValue[] columnVector) {
        if (columnVector == null) {
            return null;
        }
        return Arrays.stream(columnVector).collect(Collectors.toMap(KeyValue::key, KeyValue::value));
    }
}
