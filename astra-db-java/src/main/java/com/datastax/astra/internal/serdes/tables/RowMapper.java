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
import com.datastax.astra.client.tables.mapping.TablePrimaryKey;
import com.datastax.astra.client.tables.mapping.TablePrimaryKeyClass;
import com.datastax.astra.internal.reflection.EntityTableBeanDefinition;
import com.datastax.astra.internal.reflection.EntityFieldDefinition;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param <T>
     *      type of the object
     * @return
     *      a row
     */
    public static <T> Row mapAsRow(T input) {
        if (input == null || input instanceof Row) {
            return (Row) input;
        }
        EntityTableBeanDefinition<?> bean = new EntityTableBeanDefinition<>(input.getClass());
        Row row = new Row();
        
        // Check if any field is annotated with @TablePrimaryKey
        final Field primaryKeyFieldFinal;
        Field tempPrimaryKeyField = null;
        for (Field f : input.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(TablePrimaryKey.class)) {
                tempPrimaryKeyField = f;
                break;
            }
        }
        primaryKeyFieldFinal = tempPrimaryKeyField;
        
        bean.getFields().forEach((name, field) -> {
            try {
                // Skip the @TablePrimaryKey field itself - we'll flatten its contents
                if (primaryKeyFieldFinal != null && name.equals(primaryKeyFieldFinal.getName())) {
                    return;
                }
                
                Object value = field.getGetter().invoke(input);

                // Check if map and key is not String
                if (value instanceof Map<?, ?> map &&
                        field.getGenericKeyType() != null &&
                        !String.class.equals(field.getGenericKeyType())) {

                    List<List<Object>> pairs = new ArrayList<>();
                    map.forEach((k, v) -> pairs.add(List.of(k, v)));
                    row.put(field.getColumnName() != null ? field.getColumnName() : name, pairs);
                } else {
                    row.put(field.getColumnName() != null ? field.getColumnName() : name, value);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // If we get IllegalArgumentException, this might be a field from a primary key class
                // Try to get it from the primary key object instead
                if (primaryKeyFieldFinal != null && e instanceof IllegalArgumentException) {
                    try {
                        primaryKeyFieldFinal.setAccessible(true);
                        Object pkValue = primaryKeyFieldFinal.get(input);
                        if (pkValue != null) {
                            Object nestedValue = field.getGetter().invoke(pkValue);
                            if (nestedValue instanceof Map<?, ?> map &&
                                    field.getGenericKeyType() != null &&
                                    !String.class.equals(field.getGenericKeyType())) {
                                List<List<Object>> pairs = new ArrayList<>();
                                map.forEach((k, v) -> pairs.add(List.of(k, v)));
                                row.put(field.getColumnName() != null ? field.getColumnName() : name, pairs);
                            } else {
                                row.put(field.getColumnName() != null ? field.getColumnName() : name, nestedValue);
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to extract field from primary key: " + name, ex);
                    }
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
        
        // If there's a @TablePrimaryKey field, flatten its contents into the row
        if (primaryKeyFieldFinal != null) {
            try {
                primaryKeyFieldFinal.setAccessible(true);
                Object primaryKeyValue = primaryKeyFieldFinal.get(input);
                if (primaryKeyValue != null) {
                    Class<?> pkClass = primaryKeyValue.getClass();
                    if (pkClass.isAnnotationPresent(TablePrimaryKeyClass.class)) {
                        // Create a bean definition for the primary key class
                        EntityTableBeanDefinition<?> pkBean = new EntityTableBeanDefinition<>(pkClass);
                        pkBean.getFields().forEach((pkFieldName, pkField) -> {
                            try {
                                Object pkFieldValue = pkField.getGetter().invoke(primaryKeyValue);
                                String columnName = pkField.getColumnName() != null ? 
                                        pkField.getColumnName() : pkFieldName;
                                row.put(columnName, pkFieldValue);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("Failed to extract primary key field: " + pkFieldName, e);
                            }
                        });
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access @TablePrimaryKey field", e);
            }
        }
        
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

            EntityTableBeanDefinition<T> beanDef = new EntityTableBeanDefinition<>(inputRowClass);
            T input = inputRowClass.getDeclaredConstructor().newInstance();

            // Check if any field is annotated with @TablePrimaryKey
            Field primaryKeyField = null;
            for (Field f : inputRowClass.getDeclaredFields()) {
                if (f.isAnnotationPresent(TablePrimaryKey.class)) {
                    primaryKeyField = f;
                    break;
                }
            }

            // If there's a @TablePrimaryKey field, reconstruct it from flattened columns
            Object primaryKeyInstance = null;
            EntityTableBeanDefinition<?> pkBeanDef = null;
            if (primaryKeyField != null) {
                Class<?> pkClass = primaryKeyField.getType();
                if (pkClass.isAnnotationPresent(TablePrimaryKeyClass.class)) {
                    primaryKeyInstance = pkClass.getDeclaredConstructor().newInstance();
                    pkBeanDef = new EntityTableBeanDefinition<>(pkClass);
                    
                    // Map flattened columns to primary key fields
                    for (EntityFieldDefinition pkFieldDef : pkBeanDef.getFields().values()) {
                        String columnName = pkFieldDef.getColumnName() != null ? 
                                pkFieldDef.getColumnName() : pkFieldDef.getName();
                        Object columnValue = row.columnMap.get(columnName);
                        
                        if (columnValue != null) {
                            JavaType javaType = pkFieldDef.getJavaType();
                            Object value = serializer.getMapper().convertValue(columnValue, javaType);
                            
                            if (pkFieldDef.getSetter() != null) {
                                pkFieldDef.getSetter().invoke(primaryKeyInstance, value);
                            } else {
                                Field field = pkClass.getDeclaredField(pkFieldDef.getName());
                                field.setAccessible(true);
                                field.set(primaryKeyInstance, value);
                            }
                        }
                    }
                    
                    // Set the reconstructed primary key to the bean
                    primaryKeyField.setAccessible(true);
                    primaryKeyField.set(input, primaryKeyInstance);
                }
            }

            for (EntityFieldDefinition fieldDef : beanDef.getFields().values()) {
                // Skip fields that belong to the primary key class - they've already been handled
                if (pkBeanDef != null && pkBeanDef.getFields().containsKey(fieldDef.getName())) {
                    continue;
                }
                
                String columnName = fieldDef.getColumnName() != null ? fieldDef.getColumnName() : fieldDef.getName();
                Object columnValue = row.columnMap.get(columnName);
                if (columnValue == null) {
                    // For collection-typed fields, assign empty collection instead of leaving null
                    JavaType nullType = fieldDef.getJavaType();
                    Class<?> rawClass = nullType.getRawClass();
                    Object emptyValue = null;
                    if (List.class.isAssignableFrom(rawClass)) {
                        emptyValue = new ArrayList<>();
                    } else if (Set.class.isAssignableFrom(rawClass)) {
                        emptyValue = new LinkedHashSet<>();
                    } else if (Map.class.isAssignableFrom(rawClass)) {
                        emptyValue = new LinkedHashMap<>();
                    }
                    if (emptyValue != null) {
                        if (fieldDef.getSetter() != null) {
                            fieldDef.getSetter().invoke(input, emptyValue);
                        } else {
                            Field field = inputRowClass.getDeclaredField(fieldDef.getName());
                            field.setAccessible(true);
                            field.set(input, emptyValue);
                        }
                    }
                    continue;
                }

                JavaType javaType = fieldDef.getJavaType();
                Object value;

                // If target is a Map and the source is a List (array-of-pairs),
                // only apply the special pair-array deserialization when the target key type is NOT String.
                if (Map.class.isAssignableFrom(javaType.getRawClass()) && columnValue instanceof java.util.List) {
                    JavaType keyType = javaType.getKeyType();
                    JavaType valueType = javaType.getContentType();
                    // defensive defaults
                    if (keyType == null) keyType = serializer.getMapper().getTypeFactory().constructType(Object.class);
                    if (valueType == null) valueType = serializer.getMapper().getTypeFactory().constructType(Object.class);

                    // If key type is String, let Jackson handle it normally (stringified-object form)
                    if (keyType.getRawClass() == String.class) {
                        // convertValue will correctly handle List->Map when keys are strings (or object form)
                        value = serializer.getMapper().convertValue(columnValue, javaType);
                    } else {
                        // Use your contextual generic deserializer instance (uses the discovered key/value types)
                        DataAPIPairArrayDeserializerToMap<Object, Object> deser =
                                new DataAPIPairArrayDeserializerToMap<>(keyType, valueType);

                        // create a JsonParser from the tree and call the deserializer
                        JsonParser treeParser = serializer.getMapper()
                                .treeAsTokens(serializer.getMapper().valueToTree(columnValue));
                        // ensure parser has current token set properly
                        if (treeParser.currentToken() == null) treeParser.nextToken();

                        // Provide a DeserializationContext - obtain from ObjectMapper
                        DeserializationContext ctxt = serializer.getMapper().getDeserializationContext();
                        Map<Object, Object> map = (Map<Object, Object>) deser.deserialize(treeParser, ctxt);

                        // If the target map implementation expects specific concrete types, convert again
                        // (e.g. to LinkedHashMap<K,V> with proper generic types)
                        value = serializer.getMapper().convertValue(map, javaType);
                    }
                } else {
                    // Default conversion for other fields
                    value = serializer.getMapper().convertValue(columnValue, javaType);
                }

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
