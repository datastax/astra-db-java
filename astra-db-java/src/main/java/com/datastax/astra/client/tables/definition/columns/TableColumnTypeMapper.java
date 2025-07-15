package com.datastax.astra.client.tables.definition.columns;

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

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.tables.definition.types.TableDataAPIUserDefinedType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class TableColumnTypeMapper {

    /**
     * A static mapping of Java classes to their corresponding {@link TableColumnTypes}.
     */
    private static final Map<Class<?>, TableColumnTypes> typeMapping = new HashMap<>();

    /**
     * Static initializer block that populates the type mapping.
     */
    static {
        // Primitive and wrapper types
        typeMapping.put(Integer.class, TableColumnTypes.INT);
        typeMapping.put(int.class, TableColumnTypes.INT);
        typeMapping.put(Long.class, TableColumnTypes.BIGINT);
        typeMapping.put(long.class, TableColumnTypes.BIGINT);
        typeMapping.put(Double.class, TableColumnTypes.DOUBLE);
        typeMapping.put(double.class, TableColumnTypes.DOUBLE);
        typeMapping.put(Float.class, TableColumnTypes.FLOAT);
        typeMapping.put(float.class, TableColumnTypes.FLOAT);
        typeMapping.put(Boolean.class, TableColumnTypes.BOOLEAN);
        typeMapping.put(boolean.class, TableColumnTypes.BOOLEAN);
        typeMapping.put(Byte.class, TableColumnTypes.TINYINT);
        typeMapping.put(byte.class, TableColumnTypes.TINYINT);
        typeMapping.put(Short.class, TableColumnTypes.SMALLINT);
        typeMapping.put(short.class, TableColumnTypes.SMALLINT);

        // Commonly used Java types
        typeMapping.put(String.class, TableColumnTypes.TEXT);
        typeMapping.put(UUID.class, TableColumnTypes.UUID);
        typeMapping.put(BigDecimal.class, TableColumnTypes.DECIMAL);
        typeMapping.put(BigInteger.class, TableColumnTypes.VARINT);
        typeMapping.put(InetAddress.class, TableColumnTypes.INET);

        // Date and time types
        typeMapping.put(Instant.class, TableColumnTypes.TIMESTAMP);
        typeMapping.put(LocalDate.class, TableColumnTypes.DATE);
        typeMapping.put(LocalTime.class, TableColumnTypes.TIME);

        // Collection types
        typeMapping.put(List.class, TableColumnTypes.LIST);
        typeMapping.put(Set.class, TableColumnTypes.SET);
        typeMapping.put(Map.class, TableColumnTypes.MAP);
        typeMapping.put(DataAPIVector.class, TableColumnTypes.VECTOR);

        // Generic User Defined Types (UDTs)
        typeMapping.put(TableDataAPIUserDefinedType.class, TableColumnTypes.USER_DEFINED);

        // Unsupported or undefined types
        typeMapping.put(Object.class, TableColumnTypes.UNSUPPORTED);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private TableColumnTypeMapper() {}

    /**
     * Retrieves the Cassandra column type corresponding to the given Java class.
     * If the type is not explicitly mapped, {@code ColumnTypes.UNSUPPORTED} is returned.
     *
     * @param clazz the Java class to map
     * @return the corresponding {@link TableColumnTypes}, or {@code UNSUPPORTED} if the type is not mapped
     */
    public static TableColumnTypes getColumnType(Class<?> clazz) {
        return typeMapping.getOrDefault(clazz, TableColumnTypes.UNSUPPORTED);
    }
}