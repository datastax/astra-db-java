package com.datastax.astra.client.tables.mapping;

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

import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.columns.ColumnTypeMapper;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.dtsx.astra.sdk.utils.Utils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bean introspector will get information of a Bean to populate fields
 * directly from the output.
 */
@Slf4j
@Data
public class EntityBeanDefinition<T> {

    /** Class introspected. */
    final Class<T> clazz;

    /** Table name. */
    final String name;

    /** Class of the bean. */
    final Map<String, EntityFieldDefinition> fields;

    /**
     * Mapper for the serialization
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Constructor for the beam.
     *
     * @param clazz
     *      class type
     */
    public EntityBeanDefinition(Class<T> clazz) {
        this.clazz  = clazz;
        this.fields = new HashMap<>();

        // Table Name
        EntityTable tableAnn = clazz.getAnnotation(EntityTable.class);
        if (tableAnn == null) {
            throw new IllegalArgumentException("Invalid class: It should be annotated with @Table(name=\"table_name\")");
        }
        this.name = tableAnn.value();

        // Find properties
        List<BeanPropertyDefinition> properties = OBJECT_MAPPER
              .getSerializationConfig()
              .introspect(TypeFactory.defaultInstance().constructType(clazz))
              .findProperties();

        // Fields
        for (BeanPropertyDefinition property : properties) {
            EntityFieldDefinition field = new EntityFieldDefinition();
            field.setName(property.getName());
            field.setType(property.getPrimaryType().getRawClass());
            if (Map.class.isAssignableFrom(field.getType())) {
                JavaType keyType = property.getPrimaryType().getBindings().getBoundType(0);
                JavaType valueType = property.getPrimaryType().getBindings().getBoundType(1);
                if (keyType != null) {
                    field.setGenericKeyType(keyType.getRawClass()); // Get the key's raw class
                }
                if (valueType != null) {
                    field.setGenericValueType(valueType.getRawClass()); // Get the value's raw class
                }
            }
            // Handle List or Set types
            if (List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())) {
                JavaType elementType = property.getPrimaryType().getBindings().getBoundType(0);
                if (elementType != null) {
                    field.setGenericValueType(elementType.getRawClass()); // Get the element's raw class
                }
            }
            AnnotatedMethod getter = property.getGetter();
            field.setGetter((getter != null) ? getter.getAnnotated() : null);
            AnnotatedMethod setter = property.getSetter();
            field.setSetter((setter != null) ? setter.getAnnotated() : null);

            AnnotatedField annfield = property.getField();
            Column column = annfield.getAnnotated().getAnnotation(Column.class);
            if (column != null) {
                if (Utils.hasLength(column.name())) {
                    field.setColumnName(column.name());
                }
                if (column.type() != ColumnTypes.UNDEFINED) {
                    field.setColumnType(column.type());
                }
                if (column.valueType() != ColumnTypes.UNDEFINED) {
                    field.setValueType(column.valueType());
                }
                if (column.keyType() != ColumnTypes.UNDEFINED) {
                    field.setKeyType(column.keyType());
                }
                field.setDimension(column.dimension());
                field.setMetric(column.metric());
            } else {
                log.warn("Field {} is not annotated with @Column", field.getName());
            }

            PartitionBy partitionBy = annfield.getAnnotated().getAnnotation(PartitionBy.class);
            if (partitionBy != null) {
                field.setPartitionByPosition(partitionBy.value());
            }

            PartitionSort partitionSort = annfield.getAnnotated().getAnnotation(PartitionSort.class);
            if (partitionSort != null) {
                field.setPartitionSortPosition(partitionSort.position());
                field.setPartitionSortOrder(partitionSort.order());
            }
            fields.put(field.getName(), field);
        }
    }

    /**
     * Build the partition Key based on annotated fields with @PartitionBy with position.
     *
     * @return
     *      An ordered list of partition keys (partition key)
     */
    public List<String> getPartitionBy() {
        return getFields().values().stream()
                .filter(e -> e.getPartitionByPosition() != null)
                .sorted(Comparator.comparing(EntityFieldDefinition::getPartitionByPosition))
                .map(efd -> {
                    if (Utils.hasLength(efd.getColumnName())) {
                        return efd.getColumnName();
                    }
                    return efd.getName();
                })
                .collect(Collectors.toList());
    }

    /**
     * Build the partition sort based on annotated fields @PartitionSort with position and order.
     *
     * @return
     *      an ordered map representing the partition sort (clustering columns)
     */
    public Map<String, Integer> getPartitionSort() {
        List<EntityFieldDefinition> fields = getFields().values().stream()
                .filter(e -> e.getPartitionSortPosition() != null)
                .sorted(Comparator.comparing(EntityFieldDefinition::getPartitionSortPosition))
                .toList();
        // Order is preserved in LinkedHashMap
        Map<String, Integer> cc = new LinkedHashMap<>();
        for (EntityFieldDefinition field : fields) {
            if (Utils.hasLength(field.getColumnName())) {
                cc.put(field.getColumnName(), field.getPartitionSortOrder().getCode());
            } else {
                cc.put(field.getName(), field.getPartitionSortOrder().getCode());
            }
        }
        return cc;
    }

    public static Document createTableCommand(String tableName, Class<?> clazz) {
        EntityBeanDefinition<?> bean = new EntityBeanDefinition<>(clazz);
        if (Utils.hasLength(bean.getName()) && !bean.getName().equals(tableName)) {
            throw new IllegalArgumentException("Table name mismatch, expected '" + tableName + "' but got '" + bean.getName() + "'");
        }
        Document doc = new Document();
        doc.append("name", tableName);

        Document definition = new Document();
        doc.append("definition", definition);
        Document columns = new Document();
        bean.getFields().forEach((name, field) -> {
            Document column = new Document();
            ColumnTypes colType = field.getColumnType();
            // No types has been provided, trying to map from Java types
            if (colType == null) {
                colType = ColumnTypeMapper.getColumnType(field.getType());
                if (colType == ColumnTypes.UNSUPPORTED) {
                    throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for field '" + field.getName() + "'");
                }
            }
            column.append("type", colType.getValue());

            // Vector: Dimension and Metric
            if (colType == ColumnTypes.VECTOR) {
                if (field.getDimension() == null) {
                    throw new IllegalArgumentException("Missing attribute 'dimension' in annotation '@Column' for field '" + field.getName() + "'");
                }
                column.append("dimension", field.getDimension());

                SimilarityMetric metric = SimilarityMetric.COSINE;
                if (field.getMetric() != null) {
                    metric = field.getMetric();
                }
                column.append("metric", metric.getValue());
            }

            // KeyType with MAPS
            if (colType == ColumnTypes.MAP) {
                ColumnTypes keyType = field.getKeyType();
                if (keyType == null) {
                    keyType = ColumnTypeMapper.getColumnType(field.getGenericKeyType());
                    if (keyType == ColumnTypes.UNSUPPORTED) {
                        throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for key in field '" + field.getName() + "'");
                    }
                }
                column.append("keyType", keyType.getValue());
            }

            // ValueType with MAPS, LISTS and SETS
            if (colType == ColumnTypes.MAP ||
                colType == ColumnTypes.LIST ||
                colType == ColumnTypes.SET) {
                ColumnTypes valueType = field.getValueType();
                if (valueType == null) {
                    valueType = ColumnTypeMapper.getColumnType(field.getGenericValueType());
                    if (valueType == ColumnTypes.UNSUPPORTED) {
                        throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for value in field '" + field.getName() + "'");
                    }
                }
                column.append("valueType", valueType.getValue());
            }

            // Column Name, using the field Name if nothing provided
            String nameColumn = field.getColumnName();
            if (!Utils.hasLength(nameColumn)) {
                nameColumn = field.getName();
            }
            columns.append(nameColumn, column);
        });
        definition.append("columns", columns);

        // Primary Key
        Document primaryKey = new Document();
        primaryKey.append("partitionBy", bean.getPartitionBy());
        if (!bean.getPartitionSort().isEmpty()) {
            primaryKey.append("partitionSort", bean.getPartitionSort());
        }
        definition.append("primaryKey", primaryKey);
        return doc;
    }

}
