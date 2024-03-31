package com.datastax.astra.client.model;

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
 * List of possible types for the collection 'defaultId'.
 */
@Getter
public enum CollectionIdTypes {

    /**
     * Represent a BSON ObjectId.
     */
    OBJECT_ID("objectId"),

    /**
     * UUID in version v6 allowing natural ordering.
     */
    UUIDV6("uuidv6"),

    /**
     * UUID in version v7, random and time-based.
     */
    UUIDV7("uuidv7"),

    /**
     * UUID v4, the default random UUID.
     */
    UUID("uuid");

    private final String value;

    /**
     * Constructor.
     *
     * @param value
     *      value to the types
     */
    CollectionIdTypes(String value) {
        this.value = value;
    }

    /**
     * Creates a CollectionIdTypes from its string value.
     *
     * @param value The string value to look for.
     * @return The corresponding CollectionIdTypes enum constant.
     * @throws IllegalArgumentException if the value does not correspond to any CollectionIdTypes.
     */
    public static CollectionIdTypes fromValue(String value) {
        for (CollectionIdTypes type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
