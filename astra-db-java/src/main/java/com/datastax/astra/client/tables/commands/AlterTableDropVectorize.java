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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an operation to drop vectorized columns from a table in a database schema alteration.
 * Implements the {@link AlterTableOperation} interface to specify the "dropVectorize" operation.
 * <p>
 * This class supports specifying columns to drop and an optional "IF EXISTS" clause to
 * ensure the operation only proceeds if the specified columns exist.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTableDropVectorize dropVectorize = new AlterTableDropVectorize("column1", "column2")
 *      .ifExists()
 *      .columns("column3");
 * }
 * </pre>
 */
@Setter
@Getter
public class AlterTableDropVectorize implements AlterTableOperation {

    /**
     * A set of column names to be dropped.
     * Ensures that column names are stored in insertion order and no duplicates are included.
     */
    private Set<String> columns = new LinkedHashSet<>();

    /**
     * Default constructor. Creates an empty {@code AlterTableDropVectorize} instance.
     */
    public AlterTableDropVectorize() {}

    /**
     * Constructs a new {@code AlterTableDropVectorize} instance with the specified column names.
     *
     * @param name the names of the columns to drop
     */
    public AlterTableDropVectorize(String... name) {
        this.columns.addAll(Arrays.asList(name));
    }

    /**
     * Constructs a new {@code AlterTableDropVectorize} instance with the specified set of column names.
     *
     * @param columns the set of column names to drop
     */
    public AlterTableDropVectorize(Set<String> columns) {
        this.columns.addAll(columns);
    }

    /**
     * Adds additional column names to the list of columns to drop.
     *
     * @param columns the names of the columns to add
     * @return this {@code AlterTableDropVectorize} instance
     */
    public AlterTableDropVectorize columns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * Returns the name of this operation, which is "dropVectorize".
     *
     * @return the operation name
     */
    @Override
    public String getOperationName() {
        return "dropVectorize";
    }
}
