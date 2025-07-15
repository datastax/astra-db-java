package com.datastax.astra.client.tables.commands.results;

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

import com.datastax.astra.client.tables.definition.columns.TableColumnDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Represents the result of a single row insertion into a table, containing the
 * inserted primary key values and the schema of the primary key.
 * <p>
 * This class provides utility methods to retrieve the inserted ID as a structured row.
 * </p>
 */
@Data
@AllArgsConstructor
public class TableInsertOneResult {

    /**
     * The list of inserted primary key values. The order corresponds to the primary key schema.
     */
    private ArrayList<Object> insertedId;

    /**
     * The schema of the primary key as a map, where keys are column names and values are
     * {@link TableColumnDefinition} objects describing the column details.
     */
    private LinkedHashMap<String, TableColumnDefinition> primaryKeySchema;

    /**
     * No-argument constructor that initializes {@code insertedId} to an empty {@link ArrayList}
     * and {@code primaryKeySchema} to an empty {@link LinkedHashMap}.
     */
    public TableInsertOneResult() {
        this.insertedId = new ArrayList<>();
        this.primaryKeySchema = new LinkedHashMap<>();
    }

    /**
     * Converts the inserted ID values into a structured {@link Row}.
     * The column names from the primary key schema are used as keys in the resulting row,
     * and the corresponding values from the {@code insertedId} list are used as values.
     *
     * @return a {@link Row} representation of the inserted ID values
     * @throws IndexOutOfBoundsException if the size of {@code insertedId} does not match the number of columns in {@code primaryKeySchema}
     */
    public Row getInsertedIdAsRow() {
        Row row = new Row();
        for (int i = 0; i < insertedId.size(); i++) {
            row.put(primaryKeySchema.keySet().toArray()[i].toString(), insertedId.get(i));
        }
        return row;
    }
}