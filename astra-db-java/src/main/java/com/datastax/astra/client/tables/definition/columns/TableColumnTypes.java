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

import lombok.Getter;
/**
 * Represents the various column types supported in a database schema.
 * Each column type is associated with a string representation.
 * <p>
 * This enum includes common types like {@code TEXT}, {@code INT}, and {@code UUID},
 * as well as specialized types such as {@code VECTOR} and {@code UNSUPPORTED}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnTypes columnType = ColumnTypes.TEXT;
 * String columnValue = columnType.getValue();
 * }
 * </pre>
 */
@Getter
public enum TableColumnTypes {

    /**
     * ASCII string type.
     */
    ASCII("ascii"),

    /**
     * 64-bit signed integer.
     */
    BIGINT("bigint"),

    /**
     * Binary large object (BLOB) type.
     */
    BLOB("blob"),

    /**
     * Boolean type, representing true or false values.
     */
    BOOLEAN("boolean"),

    /**
     * Counter.
     */
    COUNTER("counter"),

    /**
     * Date type, storing only the date part without a time zone.
     */
    DATE("date"),

    /**
     * Arbitrary precision decimal number.
     */
    DECIMAL("decimal"),

    /**
     * Double-precision floating-point number.
     */
    DOUBLE("double"),

    /**
     * Time duration type.
     */
    DURATION("duration"),

    /**
     * Single-precision floating-point number.
     */
    FLOAT("float"),

    /**
     * IP address type.
     */
    INET("inet"),

    /**
     * 32-bit signed integer.
     */
    INT("int"),

    /**
     * JSON type, storing JSON formatted data.
     */
    USERDEFINED("userDefined"),

    /**
     * List collection type.
     */
    LIST("list"),

    /**
     * Map collection type, storing key-value pairs.
     */
    MAP("map"),

    /**
     * Set collection type, storing unique elements.
     */
    SET("set"),

    /**
     * 16-bit signed integer.
     */
    SMALLINT("smallint"),

    /**
     * Text or string type.
     */
    TEXT("text"),

    /**
     * Time type, storing only the time part.
     */
    TIME("time"),

    /**
     * Timestamp type, representing a specific date and time.
     */
    TIMESTAMP("timestamp"),

    /**
     * TimeUUID.
     */
    TIMEUUID("timeuuid"),

    /**
     * 8-bit signed integer.
     */
    TINYINT("tinyint"),

    /**
     * Arbitrary precision integer type.
     */
    VARINT("varint"),

    /**
     * Universally Unique Identifier (UUID) type.
     */
    UUID("uuid"),

    /**
     * Represents unsupported column types.
     */
    UNSUPPORTED("UNSUPPORTED"),

    /**
     * Represents an undefined column type.
     */
    UNDEFINED("undefined"),

    /**
     * Vector data type, typically used for machine learning or specialized computations.
     */
    VECTOR("vector");

    /**
     * The string representation of the column type.
     */
    private final String value;

    /**
     * Constructs a {@code ColumnTypes} enum constant with the specified string value.
     *
     * @param value the string representation of the column type
     */
    TableColumnTypes(String value) {
        this.value = value;
    }
}
