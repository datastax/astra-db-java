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

import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maps Java types to corresponding Cassandra column types.
 * <p>
 * The {@code ColumnTypeMapper} provides a utility method to determine the appropriate
 * Cassandra column type for a given Java class. Commonly used Java types, such as
 * primitives, wrapper types, collections, and date/time classes, are mapped to their
 * respective Cassandra types.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ColumnTypes columnType = ColumnTypeMapper.getColumnType(String.class);
 * System.out.println(columnType); // Outputs: TEXT
 * }</pre>
 *
 * <p>Supported mappings include:</p>
 * <ul>
 *     <li>Primitives and wrappers (e.g., {@code int -> INT}, {@code boolean -> BOOLEAN})</li>
 *     <li>Common Java types (e.g., {@code String -> TEXT}, {@code UUID -> UUID})</li>
 *     <li>Date and time types (e.g., {@code LocalDate -> DATE}, {@code Instant -> TIMESTAMP})</li>
 *     <li>Collection types (e.g., {@code List -> LIST}, {@code Set -> SET}, {@code Map -> MAP})</li>
 *     <li>Default fallback for unsupported types: {@code UNSUPPORTED}</li>
 * </ul>
 */
public class TableUserDefinedTypeFieldTypeMapper {

    /**
     * A static mapping of Java classes to their corresponding {@link TableColumnTypes}.
     */
    private static final Map<Class<?>, TableUserDefinedTypeFieldTypes> typeMapping = new HashMap<>();

    /**
     * Static initializer block that populates the type mapping.
     */
    static {
        // Primitive and wrapper types
        typeMapping.put(Integer.class, TableUserDefinedTypeFieldTypes.INT);
        typeMapping.put(int.class, TableUserDefinedTypeFieldTypes.INT);
        typeMapping.put(Long.class, TableUserDefinedTypeFieldTypes.BIGINT);
        typeMapping.put(long.class, TableUserDefinedTypeFieldTypes.BIGINT);
        typeMapping.put(Double.class, TableUserDefinedTypeFieldTypes.DOUBLE);
        typeMapping.put(double.class, TableUserDefinedTypeFieldTypes.DOUBLE);
        typeMapping.put(Float.class, TableUserDefinedTypeFieldTypes.FLOAT);
        typeMapping.put(float.class, TableUserDefinedTypeFieldTypes.FLOAT);
        typeMapping.put(Boolean.class, TableUserDefinedTypeFieldTypes.BOOLEAN);
        typeMapping.put(boolean.class, TableUserDefinedTypeFieldTypes.BOOLEAN);
        typeMapping.put(Byte.class, TableUserDefinedTypeFieldTypes.TINYINT);
        typeMapping.put(byte.class, TableUserDefinedTypeFieldTypes.TINYINT);
        typeMapping.put(Short.class, TableUserDefinedTypeFieldTypes.SMALLINT);
        typeMapping.put(short.class, TableUserDefinedTypeFieldTypes.SMALLINT);

        // Commonly used Java types
        typeMapping.put(String.class, TableUserDefinedTypeFieldTypes.TEXT);
        typeMapping.put(UUID.class, TableUserDefinedTypeFieldTypes.UUID);
        typeMapping.put(BigDecimal.class, TableUserDefinedTypeFieldTypes.DECIMAL);
        typeMapping.put(BigInteger.class, TableUserDefinedTypeFieldTypes.VARINT);
        typeMapping.put(InetAddress.class, TableUserDefinedTypeFieldTypes.INET);

        // Date and time types
        typeMapping.put(Instant.class, TableUserDefinedTypeFieldTypes.TIMESTAMP);
        typeMapping.put(LocalDate.class, TableUserDefinedTypeFieldTypes.DATE);
        typeMapping.put(LocalTime.class, TableUserDefinedTypeFieldTypes.TIME);

        // Collection types
//        typeMapping.put(List.class, UdtFieldTypes.LIST);
//        typeMapping.put(Set.class, UdtFieldTypes.SET);
//        typeMapping.put(Map.class, UdtFieldTypes.MAP);

        // Unsupported or undefined types
        typeMapping.put(Object.class, TableUserDefinedTypeFieldTypes.UNSUPPORTED);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private TableUserDefinedTypeFieldTypeMapper() {}

    /**
     * Retrieves the Cassandra column type corresponding to the given Java class.
     * If the type is not explicitly mapped, {@code ColumnTypes.UNSUPPORTED} is returned.
     *
     * @param clazz the Java class to map
     * @return the corresponding {@link TableColumnTypes}, or {@code UNSUPPORTED} if the type is not mapped
     */
    public static TableUserDefinedTypeFieldTypes getUdtFieldType(Class<?> clazz) {
        return typeMapping.getOrDefault(clazz, TableUserDefinedTypeFieldTypes.UNSUPPORTED);
    }
}