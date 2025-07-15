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

import com.datastax.astra.internal.serdes.tables.RowSerializer;
import lombok.Data;

/**
 * Represents the API support configuration for a column definition.
 * <p>
 * This class defines the permissions and CQL (Cassandra Query Language) configuration
 * related to a specific column in a database schema. It includes options for
 * creating tables, inserting data, reading data, and the column's CQL definition.
 * </p>
 * <p>
 * This class is annotated with {@code @Data} and {@code @NoArgsConstructor} from Lombok,
 * which automatically generate getters, setters, and a no-argument constructor.
 * </p>
 *
 * <p>Fields:</p>
 * <ul>
 *   <li>{@code createTable} - Specifies whether the column is included in table creation.</li>
 *   <li>{@code insert} - Indicates if the column supports insert operations.</li>
 *   <li>{@code read} - Indicates if the column supports read operations.</li>
 *   <li>{@code cqlDefinition} - Defines the CQL expression associated with the column.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnDefinitionApiSupport columnSupport = new ColumnDefinitionApiSupport();
 * columnSupport.setCreateTable(true);
 * columnSupport.setInsert(true);
 * columnSupport.setRead(false);
 * columnSupport.setCqlDefinition("text");
 *
 * System.out.println(columnSupport);
 * }
 * </pre>
 */
@Data
public class TableColumnDefinitionApiSupport {

    /**
     * Specifies whether the column is included in table creation.
     */
    private boolean createTable;

    /**
     * Indicates if the column supports insert operations.
     */
    private boolean insert;

    /**
     * Indicates if the column supports read operations.
     */
    private boolean read;

    /**
     * Defines the CQL expression associated with the column.
     */
    private String cqlDefinition;

    /**
     * Default constructor.
     */
    public TableColumnDefinitionApiSupport() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link RowSerializer} to serialize the object
     * into a marshalled string representation.
     * </p>
     */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}

