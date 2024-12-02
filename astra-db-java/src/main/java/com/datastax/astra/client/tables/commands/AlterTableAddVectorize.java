package com.datastax.astra.client.tables.commands;

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

import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an operation to add vectorized columns to a table in a database schema alteration.
 * Implements the {@link AlterTableOperation} interface to specify the "addVectorize" operation.
 * <p>
 * This class allows adding columns with vectorization options and supports an optional "IF NOT EXISTS" clause.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTableAddVectorize addVectorize = new AlterTableAddVectorize()
 *      .ifNotExists()
 *      .addColumn("vectorColumn", new VectorServiceOptions());
 * }
 * </pre>
 */
@Setter @Getter
public class AlterTableAddVectorize implements AlterTableOperation {

    /**
     * A map of column names to their respective {@link VectorServiceOptions} configurations.
     * Represents the vectorized columns to be added to the table.
     */
    private Map<String, VectorServiceOptions> columns = new HashMap<>();

    /**
     * Default constructor.
     */
    public AlterTableAddVectorize() {
        // left blank, serialization with jackson
    }

    /**
     * Returns the name of this operation, which is "addVectorize".
     *
     * @return the operation name
     */
    @Override
    public String getOperationName() {
        return "addVectorize";
    }

    /**
     * Adds a new column with the specified name and vectorization options.
     *
     * @param name the name of the column to add
     * @param def  the vectorization options for the column
     * @return this {@code AlterTableAddVectorize} instance
     */
    public AlterTableAddVectorize addColumn(String name, VectorServiceOptions def) {
        columns.put(name, def);
        return this;
    }

    /**
     * Replaces the current column definitions with the specified map of column names and vectorization options.
     *
     * @param columns a map of column names to their vectorization options
     * @return this {@code AlterTableAddVectorize} instance
     */
    public AlterTableAddVectorize columns(Map<String, VectorServiceOptions> columns) {
        this.columns = columns;
        return this;
    }
}
