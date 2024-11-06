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
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.dtsx.astra.sdk.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean introspector will get information of a Bean to populate fields
 * directly from the output.
 */
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
            throw new IllegalArgumentException("Please annotate your bean with @Table(name=\"table_name\")");
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

            AnnotatedMethod getter = property.getGetter();
            field.setGetter((getter != null) ? getter.getAnnotated() : null);
            AnnotatedMethod setter = property.getSetter();
            field.setSetter((setter != null) ? setter.getAnnotated() : null);

            AnnotatedField annfield = property.getField();
            Column column = annfield.getAnnotated().getAnnotation(Column.class);
            if (column != null) {
                if (Utils.hasLength(column.value())) {
                    field.setColumnName(column.value());
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
                // Control for consistency ib between parameters of @Column
                if (field.getDimension() > 0
                        && field.getColumnType() != ColumnTypes.VECTOR) {
                    throw new IllegalArgumentException("Field " + field.getName() + " provides a dimension  must be of type VECTOR");
                }
                if (field.getValueType() != null
                        && field.getColumnType() != ColumnTypes.LIST
                        && field.getColumnType() != ColumnTypes.SET
                        && field.getColumnType() != ColumnTypes.MAP) {
                    throw new IllegalArgumentException("Field " + field.getName() + " provides a valueType  must be of type LIST, SET or MAP");
                }
            }
            if (field.getKeyType() != null && field.getColumnType() != ColumnTypes.MAP) {
                throw new IllegalArgumentException("Field " + field.getName() + " provides a keyType  must be of type MAP");
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
     * Build the partition Key based on annotated fields.
     *
     * @return
     *      list of partition keys
     */
    public List<String> getPartitionBy() {
        return getFields().values().stream()
                .filter(e -> e.getPartitionByPosition() != null)
                .sorted((f1, f2) -> f1.getPartitionByPosition().compareTo(f2.getPartitionByPosition()))
                .map(EntityFieldDefinition::getColumnName)
                .collect(Collectors.toList());
    }

    /**
     * Build the clustering Key based on annotated fields.
     *
     * @return
     *      list of clustering keys
     */
    public Map<String, Integer> getPartitionSort() {
        List<EntityFieldDefinition> fields = getFields().values().stream()
                .filter(e -> e.getPartitionSortPosition() != null)
                .sorted((f1, f2) -> f1.getPartitionSortPosition().compareTo(f2.getPartitionSortPosition()))
                .toList();
        Map<String, Integer> cc = new LinkedHashMap<>();
        for (EntityFieldDefinition field : fields) {
            cc.put(field.getColumnName(), field.getPartitionSortOrder().getCode());
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
            if (field.getColumnType() == null) {
                throw new IllegalArgumentException("Missing column type in annotation @Column for field '" + field.getName() + "'");
            }
            column.append("type", field.getColumnType().getValue());

            // -- FIXME SUPPORT MAP, SET, LIST, VECTOR
            columns.append(field.getColumnName(), column);
        });
        definition.append("columns", columns);

        Document primaryKey = new Document();
        primaryKey.append("partitionBy", bean.getPartitionBy());
        if (!bean.getPartitionSort().isEmpty()) {
            primaryKey.append("partitionSort", bean.getPartitionSort());
        }
        definition.append("primaryKey", primaryKey);
        return doc;
    }

}
