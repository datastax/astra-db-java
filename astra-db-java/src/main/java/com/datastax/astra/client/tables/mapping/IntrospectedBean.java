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

import com.dtsx.astra.sdk.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean introspector will get information of a Bean to populate fields
 * directly from the output.
 */
@Data
public class IntrospectedBean<T> {

    /** Class introspected. */
    final Class<T> clazz;

    /** Table name. */
    final String name;

    /** Class of the bean. */
    final Map<String, IntrospectedField> fields;

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
    public IntrospectedBean(Class<T> clazz) {
        this.clazz  = clazz;
        this.fields = new HashMap<>();

        // Table Name
        Table tableAnn = clazz.getAnnotation(Table.class);
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
            IntrospectedField field = new IntrospectedField();
            field.setColumnName(property.getName());
            field.setType(property.getPrimaryType().getRawClass());
            AnnotatedMethod getter = property.getGetter();
            field.setGetter((getter != null) ? getter.getAnnotated() : null);
            AnnotatedMethod setter = property.getSetter();
            field.setSetter((setter != null) ? setter.getAnnotated() : null);
            AnnotatedField annfield = property.getField();
            field.setColumnName(field.getName());
            Column column = annfield.getAnnotated().getAnnotation(Column.class);
            if (column != null) {
                if (Utils.hasLength(column.name())) {
                    field.setColumnName(column.name());
                }
                if (column.type() != null) {
                    field.setColumnType(column.type());
                }
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
