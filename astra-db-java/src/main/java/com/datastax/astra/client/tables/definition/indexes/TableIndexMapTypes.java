package com.datastax.astra.client.tables.definition.indexes;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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
 * Enum representing the types of map indexes in a table.
 * This enum is designed for use in scenarios such as serialization/deserialization with libraries
 * like Jackson and for method chaining in fluent-style APIs.
 */
@Getter
public enum TableIndexMapTypes {

    /**
     * Index on map Keys
     */
    KEYS("$keys"),

    /**
     * Index on map values
     */
    ENTRIES("$entries"),

    /**
     * Index on map values
     */
    VALUES("$values");

    /**
     * Key Used in the JSON.
     */
    private final String value;

    /**
     * Constructor.
     * @param value the value to set.
     */
    TableIndexMapTypes(String value) {
        this.value = value;
    }

}

