package com.datastax.astra.internal.serdes.tables;

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

import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.reflection.EntityBeanDefinition;
import com.datastax.astra.internal.reflection.EntityFieldDefinition;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.fasterxml.jackson.databind.JavaType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Pivot class to interact with Table is a Row. User can wirj POJO that will be converted to Row.
 */
public class RowMapper {

    /**
     * Private constructor to prevent instantiation.
     */
    private RowMapper() {
    }

    /**
     * Map any object as a Row
     *
     * @param input
     *      input object
     * @return
     *      a row
     */
    public static <T> Row mapAsRow(T input) {
        if (input == null || input instanceof Row) {
            return (Row) input;
        }
        EntityBeanDefinition<?> bean = new EntityBeanDefinition<>(input.getClass());
        Row row = new Row();
        bean.getFields().forEach((name, field) -> {
            try {
                row.put(field.getColumnName() != null ? field.getColumnName() : name,
                        field.getGetter().invoke(input));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        return row;
    }

    /**
     * Mapping a row to a bean. If the bean is annotated with @Entity, the column names will be used.
     * @param row
     *      row to map
     * @param serializer
     *      serializer
     * @param inputRowClass
     *     input class
     * @return
     *     the bean
     * @param <T>
     *     type of the bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T mapFromRow(Row row, DataAPISerializer serializer, Class<T> inputRowClass) {
        try {
            if (inputRowClass == Row.class) {
                return (T) row;
            }
            if (row == null) {
                return null;
            }
            EntityBeanDefinition<T> beanDef = new EntityBeanDefinition<>(inputRowClass);
            T input = inputRowClass.getDeclaredConstructor().newInstance();

            for (EntityFieldDefinition fieldDef : beanDef.getFields().values()) {
                String columnName = fieldDef.getColumnName() != null ?
                        fieldDef.getColumnName() :
                        fieldDef.getName();
                Object columnValue = row.columnMap.get(columnName);
                if (columnValue == null) {
                    continue; // Handle nulls as needed
                }

                // Use the JavaType directly
                JavaType javaType = fieldDef.getJavaType();

                // Convert the column value to the field's type
                Object value = serializer
                        .getMapper()
                        .convertValue(columnValue, javaType);

                // Set the value to the bean
                if (fieldDef.getSetter() != null) {
                    fieldDef.getSetter().invoke(input, value);
                } else {
                    Field field = inputRowClass.getDeclaredField(fieldDef.getName());
                    field.setAccessible(true);
                    field.set(input, value);
                }
            }

            return input;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map row to bean", e);
        }
    }

}
