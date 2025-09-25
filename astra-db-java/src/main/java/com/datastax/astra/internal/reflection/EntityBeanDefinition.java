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

import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.exceptions.DataAPIClientException;
import com.datastax.astra.client.exceptions.ErrorCodesClient;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedType;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypeMapper;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeField;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypeMapper;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.ColumnVector;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides introspection and metadata for a JavaBean entity.
 * <p>
 * This class inspects a JavaBean of type {@code T} to extract and manage metadata about its properties.
 * It is used to populate fields directly based on the output or other sources.
 * </p>
 *
 * <p>The {@code EntityBeanDefinition} is useful in scenarios where JavaBean introspection is needed
 * for tasks such as data mapping, serialization, or populating object fields dynamically.</p>
 *
 * @param <T> the type of the JavaBean entity being introspected
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
        TableUserDefinedType tableUserDefinedTypeAnn = clazz.getAnnotation(TableUserDefinedType.class);
        if (tableAnn != null) {
            this.name = tableAnn.value();
        } else if (tableUserDefinedTypeAnn !=null) {
            this.name = tableUserDefinedTypeAnn.value();
        } else {
            this.name = clazz.getSimpleName().toLowerCase();
        }

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
            field.setJavaType(property.getPrimaryType());

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
            if (annfield != null) {
                Column column = annfield.getAnnotated()
                        .getAnnotation(Column.class);
                ColumnVector columnVector = annfield.getAnnotated()
                        .getAnnotation(ColumnVector.class);
                TableUserDefinedTypeField tableUserDefinedTypeField = annfield.getAnnotated()
                        .getAnnotation(TableUserDefinedTypeField.class);

                if (column != null && columnVector != null) {
                    throw new IllegalArgumentException(String.format("Field '%s' in class '%s' cannot be annotated " +
                            "with both @Column or @ColumnVector", field.getName(), clazz.getName()));
                }
                if (tableUserDefinedTypeField != null && (column != null || columnVector != null)) {
                    throw new IllegalArgumentException(String.format("Field '%s' in class '%s' cannot be annotated " +
                            "with both @UdtField and @Column or @ColumnVector", field.getName(), clazz.getName()));
                }
                if ((column!=null || columnVector!=null) && tableUserDefinedTypeAnn != null) {
                    throw new IllegalArgumentException(String.format("Field '%s' in class '%s' cannot be annotated " +
                            "with @Column or @ColumnVector as it is markerd as @Udt, please use @UdtField instead", field.getName(), clazz.getName()));
                }
                if (tableUserDefinedTypeField != null && tableAnn != null) {
                    throw new IllegalArgumentException(String.format("Field '%s' in class '%s' cannot be annotated " +
                            "with @UdtField as it is marked as @EntityTable, please use @Column/@ColumnVector instead", field.getName(), clazz.getName()));
                }

                if (column != null) {
                    if (Utils.hasLength(column.name())) {
                        field.setColumnName(column.name());
                    }
                    if (column.type() != TableColumnTypes.UNDEFINED) {
                        field.setColumnType(column.type());
                    }
                    if (column.valueType() != TableColumnTypes.UNDEFINED) {
                        field.setValueType(column.valueType());
                    }
                    if (column.keyType() != TableColumnTypes.UNDEFINED) {
                        field.setKeyType(column.keyType());
                    }
                    if (Utils.hasLength(column.udtName())) {
                        field.setUdtName(column.udtName());
                    }
                } else if (columnVector != null) {

                    field.setColumnType(TableColumnTypes.VECTOR);
                    field.setSimilarityMetric(columnVector.metric());
                    field.setVectorDimension(columnVector.dimension());
                    if (Utils.hasLength(columnVector.name())) {
                        field.setColumnName(columnVector.name());
                    }
                    if (Utils.hasLength(columnVector.provider())) {
                        field.setVectorServiceProvider(columnVector.provider());
                    }
                    if (Utils.hasLength(columnVector.modelName())) {
                        field.setVectorModelName(columnVector.modelName());
                    }
                    if (Utils.hasLength(columnVector.sourceModel())) {
                        field.setVectorSourceModel(columnVector.sourceModel());
                    }
                    if (columnVector.authentication().length > 0) {
                        field.setVectorAuthentication(EntityFieldDefinition.toMap(columnVector.authentication()));
                    }
                    if (columnVector.parameters().length > 0) {
                        field.setVectorParameters(EntityFieldDefinition.toMap(columnVector.parameters()));
                    }
                } else if (tableUserDefinedTypeField != null) {
                    if (tableUserDefinedTypeAnn != null) {
                        field.setUdtName(tableUserDefinedTypeAnn.value());
                    }
                    if (Utils.hasLength(tableUserDefinedTypeField.name())) {
                        field.setUdtFieldName(tableUserDefinedTypeField.name());
                    } else {
                        field.setUdtFieldName(field.getName());
                    }
                    field.setUdtFieldType(tableUserDefinedTypeField.type());
                    if (tableUserDefinedTypeField.valueType() != TableUserDefinedTypeFieldTypes.UNDEFINED) {
                        field.setUdtFieldValueType(tableUserDefinedTypeField.valueType());
                    }
                    if (tableUserDefinedTypeField.keyType() != TableUserDefinedTypeFieldTypes.UNDEFINED) {
                        field.setUdtFieldKeyType(tableUserDefinedTypeField.keyType());
                    }
                } else {
                    // No annotation, using the name as column name
                    field.setColumnName(field.getName());
                    // No @UdtField, using the name as UDT field name
                    field.setUdtFieldName(field.getName());
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

    /**
     * List all vector index definitions for the given table name and class.
     *
     * @param tableName
     *      the table name
     * @param clazz
     *      the class
     * @return
     *      a list of vector index definitions
     */
    public static List<TableVectorIndexDefinition> listVectorIndexDefinitions(String tableName, Class<?> clazz) {
        EntityBeanDefinition<?> bean = new EntityBeanDefinition<>(clazz);
        if (Utils.hasLength(bean.getName()) && !bean.getName().equals(tableName)) {
            throw new IllegalArgumentException("Table name mismatch, expected '" + tableName + "' but got '" + bean.getName() + "'");
        }
        List<TableVectorIndexDefinition> idxList = new ArrayList<>();
        bean.getFields().forEach((name, field) -> {
            TableColumnTypes colType = field.getColumnType();
            if (colType == TableColumnTypes.VECTOR) {
                TableVectorIndexDefinition idx = new TableVectorIndexDefinition();
                if (Utils.hasLength(field.getColumnName())) {
                    idx = idx.column(field.getColumnName());
                } else {
                    idx = idx.column(field.getName());
                }
                idx = idx.metric(field.getSimilarityMetric());
                idx = idx.sourceModel(field.getVectorSourceModel());
                idxList.add(idx);
            }
        });
        return idxList;
    }

    /**
     * Create a Type command based on the annotated fields.
     *
     * @param clazz
     *      the class
     * @return
     *      a document representing the table command
     */
    public static Document createTypeCommand(Class<?> clazz) {
        EntityBeanDefinition<?> bean = new EntityBeanDefinition<>(clazz);
        Document doc = new Document();
        doc.append("name", bean.getName());
        Document definition = new Document();
        doc.append("definition", definition);
        Document fields = new Document();
        bean.getFields().forEach((name, field) -> {
            Document udtfield = new Document();
            TableUserDefinedTypeFieldTypes fieldType = field.getUdtFieldType();
            // No types has been provided, trying to map from Java types
            if (fieldType == null) {
                fieldType = TableUserDefinedTypeFieldTypeMapper.getUdtFieldType(field.getType());
                if (fieldType == TableUserDefinedTypeFieldTypes.UNSUPPORTED) {
                    throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for field '" + field.getName() + "'");
                }
            }
            udtfield.append("type", fieldType.getValue());
            fields.append(field.getName(), udtfield);
        });
        definition.append("fields", fields);
        return doc;
    }

    /**
     * Create a table command based on the annotated fields.
     *
     * @param tableName
     *      the table name
     * @param clazz
     *      the class
     * @return
     *      a document representing the table command
     */
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
            TableColumnTypes colType = field.getColumnType();
            // No types has been provided, trying to map from Java types
            if (colType == null) {
                colType = TableColumnTypeMapper.getColumnType(field.getType());
                if (colType == TableColumnTypes.UNSUPPORTED) {
                    throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for field '" + field.getName() + "'");
                }
            }
            column.append("type", colType.getValue());

            // UDT: Name
            if (colType == TableColumnTypes.USERDEFINED) {
                if (!Utils.hasLength(field.getUdtName())) {
                    throw new IllegalArgumentException("Field '" + field.getName() + "' is missing UDT name");
                }
                column.append("udtName", field.getUdtName());
            }

            // Vector: Dimension and Metric
            if (colType == TableColumnTypes.VECTOR) {
                if (field.getVectorDimension() <= 0 || field.getVectorDimension() > 8192 ) {
                    throw new DataAPIClientException(ErrorCodesClient.INVALID_ANNOTATION, "ColumnVector", field.getName(), "dimension is required and must be in between 1 amd 8192");
                }
                column.append("dimension", field.getVectorDimension());
                column.append("metric", field.getSimilarityMetric());

                if (Utils.hasLength(field.getVectorServiceProvider())) {
                    Map<String, Object > service = new HashMap<>();
                    service.put("provider", field.getVectorServiceProvider());
                    service.put("modelName", field.getVectorModelName());
                    if (field.getVectorAuthentication() != null) {
                        service.put("authentication", field.getVectorAuthentication());
                    }
                    if (field.getVectorParameters() != null) {
                        service.put("parameters", field.getVectorParameters());
                    }
                    column.append("service", service);
                }
            }

            // KeyType with MAPS
            if (colType == TableColumnTypes.MAP) {
                TableColumnTypes keyType = field.getKeyType();
                if (keyType == null) {
                    keyType = TableColumnTypeMapper.getColumnType(field.getGenericKeyType());
                    if (keyType == TableColumnTypes.UNSUPPORTED) {
                        throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for key in field '" + field.getName() + "'");
                    }
                }
                column.append("keyType", keyType.getValue());
            }

            // ValueType with MAPS, LISTS and SETS
            if (colType == TableColumnTypes.MAP ||
                colType == TableColumnTypes.LIST ||
                colType == TableColumnTypes.SET) {
                TableColumnTypes valueType = field.getValueType();
                if (valueType == null) {
                    valueType = TableColumnTypeMapper.getColumnType(field.getGenericValueType());
                    if (valueType == TableColumnTypes.UNSUPPORTED) {
                        throw new IllegalArgumentException("Unsupported type '" + field.getType().getName() + "' for value in field '" + field.getName() + "'");
                    }
                } else if (valueType == TableColumnTypes.USERDEFINED) {
                    // Special Document for UDT
                    Document valueTypeDoc = new Document();
                    if (!Utils.hasLength(field.getUdtName())) {
                        throw new IllegalArgumentException("Field '" + field.getName() + "' is missing UDT name");
                    }
                    valueTypeDoc.append("type", valueType.getValue());
                    valueTypeDoc.append("udtName", field.getUdtName());
                    column.append("valueType", valueTypeDoc);



                } else {
                    column.append("valueType", valueType.getValue());
                }
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
