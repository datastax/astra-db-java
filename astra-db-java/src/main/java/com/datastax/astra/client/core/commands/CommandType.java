package com.datastax.astra.client.core.commands;

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

/**
 * Type of Commands. Part of Command Options they are used to determine
 * the type of command to execute and associated configuration like timeouts if needed.
 */
public enum CommandType {

    /**
     * Data operation (find*, insert*, update*, delete*)
     */
    GENERAL_METHOD,

    /**
     * Table schema operation (create*, drop*)
     */
    TABLE_ADMIN,

    /**
     * Collection Schema operation (create*, drop*)
     */
    COLLECTION_ADMIN,

    /**
     * Database admin operation (create, delete, list)
     */
    DATABASE_ADMIN,

    /**
     * Keyspace admin operation (create, delete, list)
     */
    KEYSPACE_ADMIN
}
