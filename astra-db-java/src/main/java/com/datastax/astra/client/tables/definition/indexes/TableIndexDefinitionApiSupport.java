package com.datastax.astra.client.tables.definition.indexes;

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

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the API support for table index definitions, providing flags for
 * various operations such as table creation, insertion, and reading, along with
 * a CQL (Cassandra Query Language) definition.
 * <p>
 * This class is a data structure used to store and transfer settings related to
 * table index configurations.
 * </p>
 */
@Data
public class TableIndexDefinitionApiSupport {

    /**
     * Flag indicating whether table creation is supported.
     */
    private boolean createTable;

    /**
     * Flag indicating whether insertion is supported.
     */
    private boolean insert;

    /**
     * Flag indicating whether reading is supported.
     */
    private boolean read;

    /**
     * A string containing the CQL (Cassandra Query Language) definition for the table index.
     */
    private String cqlDefinition;

    /**
     * Default constructor.
     */
    public TableIndexDefinitionApiSupport() {}
}
