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


import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * Represents the base class for table index definitions, encapsulating common properties
 * and behaviors for index definitions, including the column being indexed and API support
 * for index-related operations.
 * <p>
 * Subclasses should extend this abstract class to implement specific types of index definitions.
 * </p>
 */
@Getter
public abstract class TableBaseIndexDefinition {

    /**
     * The name of the column that the index is associated with.
     */
    protected TableIndexColumnDefinition column;

    /**
     * The API support configuration for the table index.
     */
    protected TableIndexDefinitionApiSupport apiSupport;

    /**
     * Default constructor.
     */
    public TableBaseIndexDefinition() {}
}

