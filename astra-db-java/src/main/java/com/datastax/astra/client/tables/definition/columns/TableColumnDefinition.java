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
 * Represents a column definition in a database schema.
 * <p>
 * This class encapsulates the type of the column and its associated API support
 * configurations, providing a structure for managing column-specific details.
 * </p>
 * <p>
 * The class uses Lombok annotations to automatically generate getter, setter, and
 * no-argument constructor methods.
 * </p>
 *
 * <p>Fields:</p>
 * <ul>
 *   <li>{@code type} - Specifies the type of the column, represented by {@link TableColumnTypes}.</li>
 *   <li>{@code apiSupport} - Provides API support details for the column, defined by {@link TableColumnDefinitionApiSupport}.</li>
 * </ul>
 *
 * <p>Constructors:</p>
 * <ul>
 *   <li>A no-argument constructor for creating a default instance of the class.</li>
 *   <li>A parameterized constructor for initializing the column definition with a specified type.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * ColumnTypes columnType = ColumnTypes.STRING;
 * ColumnDefinition columnDefinition = new ColumnDefinition(columnType);
 *
 * columnDefinition.setApiSupport(new ColumnDefinitionApiSupport());
 * columnDefinition.getApiSupport().setCreateTable(true);
 * columnDefinition.getApiSupport().setInsert(false);
 *
 * System.out.println(columnDefinition);
 * }
 * </pre>
 */
@Data
public class TableColumnDefinition {

    /**
     * The type of the column.
     */
    private TableColumnTypes type;

    /**
     * API support details for the column when not fully supported by the Data API.
     */
    private TableColumnDefinitionApiSupport apiSupport;

    /**
     * Default constructor.
     */
    public TableColumnDefinition() {
    }

    /**
     * Constructor with type.
     *
     * @param type the column type
     */
    public TableColumnDefinition(TableColumnTypes type) {
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }

}
