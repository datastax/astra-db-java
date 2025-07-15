package com.datastax.astra.client.tables.definition.types;

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

import com.datastax.astra.internal.utils.Assert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Definition of a User-Defined Type (UDT) in Cassandra.
 */
@Data
//@JsonSerialize(using = UdtDefinitionSerializer.class)
public class TableUserDefinedTypeDefinition {

    /**
     * The columns of the table.
     */
    private LinkedHashMap<String, TableUserDefinedTypeFieldDefinition> fields = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public TableUserDefinedTypeDefinition() {
    }

    /**
     * Adds a column to the table definition.
     *
     * @param fieldName the name of the column
     * @param fieldDefinition the definition of the field
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     * @throws IllegalArgumentException if {@code columnName} is null
     */
    public TableUserDefinedTypeDefinition addField(String fieldName, TableUserDefinedTypeFieldDefinition fieldDefinition) {
        Assert.notNull(fieldName, "Column columnName");
        fields.put(fieldName, fieldDefinition);
        return this;
    }

    /**
     * Adds a column to the table with a specific type.
     *
     * @param name the name of the column
     * @param type the type of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addField(String name, TableUserDefinedTypeFieldTypes type) {
        fields.put(name, new TableUserDefinedTypeFieldDefinition(type));
        return this;
    }

    /**
     * Adds a UUID column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldUuid(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.UUID);
    }

    /**
     * Adds a text column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldText(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.TEXT);
    }

    /**
     * Adds an ascii column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldAscii(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.ASCII);
    }

    /**
     * Adds an integer column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldInt(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.INT);
    }

    /**
     * Adds a timestamp column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldTimestamp(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.TIMESTAMP);
    }

    /**
     * Adds a boolean column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldBoolean(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.BOOLEAN);
    }

    /**
     * Adds a boolean column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldBigInt(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.BIGINT);
    }

    /**
     * Adds a blob column to the table.
     *
     * @param name the name of the column
     * @return the updated {@link TableUserDefinedTypeDefinition} instance
     */
    public TableUserDefinedTypeDefinition addFieldBlob(String name) {
        return addField(name, TableUserDefinedTypeFieldTypes.BLOB);
    }

//    /**
//     * Adds a list column to the table.
//     *
//     * @param name the name of the column
//     * @param valueType the type of the elements in the list
//     * @return the updated {@link UdtDefinition} instance
//     */
//    public UdtDefinition addFieldList(String name, UdtFieldTypes valueType) {
//        fields.put(name, new UdtFieldDefinitionList(valueType));
//        return this;
//    }
//
//    /**
//     * Adds a set column to the table.
//     *
//     * @param name the name of the column
//     * @param valueType the type of the elements in the set
//     * @return the updated {@link UdtDefinition} instance
//     */
//    public UdtDefinition addFieldSet(String name, UdtFieldTypes valueType) {
//        fields.put(name, new UdtFieldDefinitionSet(valueType));
//        return this;
//    }
//
//    /**
//     * Adds a map column to the table.
//     *
//     * @param name the name of the column
//     * @param keyType the type of the keys in the map
//     * @param valueType the type of the values in the map
//     * @return the updated {@link UdtDefinition} instance
//     */
//    public UdtDefinition addFieldMap(String name, UdtFieldTypes keyType, UdtFieldTypes valueType) {
//        fields.put(name, new UdtFieldDefinitionMap(keyType, valueType));
//        return this;
//    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "TypeDefinition{" +
                    "fields=" + Arrays.toString(fields.entrySet().toArray()) +
                    '}';
        }
    }
}
