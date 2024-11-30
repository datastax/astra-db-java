package com.datastax.astra.client.tables.columns;

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
 * Maps Java types to Cassandra column types.
 */
public class ColumnTypeMapper {

    private static final Map<Class<?>, ColumnTypes> typeMapping = new HashMap<>();

    static {
        typeMapping.put(Integer.class, ColumnTypes.INT);
        typeMapping.put(int.class, ColumnTypes.INT);
        typeMapping.put(Long.class, ColumnTypes.BIGINT);
        typeMapping.put(long.class, ColumnTypes.BIGINT);

        typeMapping.put(Double.class, ColumnTypes.DOUBLE);
        typeMapping.put(double.class, ColumnTypes.DOUBLE);

        typeMapping.put(Float.class, ColumnTypes.FLOAT);
        typeMapping.put(float.class, ColumnTypes.FLOAT);
        typeMapping.put(Boolean.class, ColumnTypes.BOOLEAN);
        typeMapping.put(boolean.class, ColumnTypes.BOOLEAN);
        typeMapping.put(Byte.class, ColumnTypes.TINYINT);
        typeMapping.put(byte.class, ColumnTypes.TINYINT);
        typeMapping.put(Short.class, ColumnTypes.SMALLINT);
        typeMapping.put(short.class, ColumnTypes.SMALLINT);

        // Commonly used Java types
        typeMapping.put(String.class, ColumnTypes.TEXT);
        typeMapping.put(UUID.class, ColumnTypes.UUID);
        typeMapping.put(BigDecimal.class, ColumnTypes.DECIMAL);
        typeMapping.put(BigInteger.class, ColumnTypes.VARINT);
        typeMapping.put(InetAddress.class, ColumnTypes.INET);

        // Date and time types
        typeMapping.put(Instant.class, ColumnTypes.TIMESTAMP);
        typeMapping.put(LocalDate.class, ColumnTypes.DATE);
        typeMapping.put(LocalTime.class, ColumnTypes.TIME);

        // Collection types
        typeMapping.put(List.class, ColumnTypes.LIST);
        typeMapping.put(Set.class, ColumnTypes.SET);
        typeMapping.put(Map.class, ColumnTypes.MAP);
        typeMapping.put(DataAPIVector.class, ColumnTypes.VECTOR);

        // Unsupported or undefined
        typeMapping.put(Object.class, ColumnTypes.UNSUPPORTED);
    }

    public static ColumnTypes getColumnType(Class<?> clazz) {
        return typeMapping.getOrDefault(clazz, ColumnTypes.UNSUPPORTED);
    }

}
