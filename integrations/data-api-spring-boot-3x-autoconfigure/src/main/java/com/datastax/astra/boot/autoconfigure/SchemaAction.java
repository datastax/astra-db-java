package com.datastax.astra.boot.autoconfigure;

/*-
 * #%L
 * Data API Spring Boot 3.x Autoconfigure
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

/**
 * Defines the schema management strategy for collections and tables.
 * Similar to JPA's ddl-auto property, this controls how the application
 * handles collection/table creation and validation at startup.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public enum SchemaAction {

    /**
     * Create collections/tables if they don't exist.
     * This is the default and safest option for most applications.
     * If a collection/table already exists, it will be used as-is.
     */
    CREATE_IF_NOT_EXISTS,

    /**
     * Validate that collections/tables exist but don't create them.
     * The application will fail to start if required collections/tables are missing.
     * Use this in production environments where schema should be managed separately.
     */
    VALIDATE,

    /**
     * Do nothing - assume collections/tables already exist.
     * No validation or creation will be performed.
     * Use this when you want complete manual control over schema management.
     */
    NONE;

    /**
     * Parse a string value to SchemaAction enum.
     *
     * @param value the string value (case-insensitive)
     * @return the corresponding SchemaAction
     * @throws IllegalArgumentException if the value is not a valid SchemaAction
     */
    public static SchemaAction fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return CREATE_IF_NOT_EXISTS; // default
        }
        try {
            return SchemaAction.valueOf(value.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid schema-action value: '" + value + "'. " +
                    "Valid values are: CREATE_IF_NOT_EXISTS, VALIDATE, NONE", e);
        }
    }
}
