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
 * Represents an operation to drop columns from a table in a database schema alteration.
 * Implements the {@link AlterTableOperation} interface to specify the "drop" operation.
 * <p>
 * This class supports adding columns to be dropped, with an optional "if exists" clause.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTableDropColumns dropColumns = new AlterTableDropColumns()
 *      .ifExists()
 *      .columns("column3");
 * }
 * </pre>
 */
@Setter
@Getter
@NoArgsConstructor
public final class AlterTableDropColumns implements AlterTableOperation {

    /**
     * A set of column names to be dropped.
     * Ensures that column names are stored in insertion order with no duplicates.
     */
    private Set<String> columns = new LinkedHashSet<>();

    /**
     * Constructs a new {@code AlterTableDropColumns} instance with the specified column names.
     *
     * @param name the names of the columns to drop
     */
    public AlterTableDropColumns(String... name) {
        this.columns.addAll(Arrays.asList(name));
    }

    /**
     * Constructs a new {@code AlterTableDropColumns} instance with the specified set of column names.
     *
     * @param columns the set of column names to drop
     */
    public AlterTableDropColumns(Set<String> columns) {
        this.columns.addAll(columns);
    }

    /**
     * Adds additional column names to the list of columns to drop.
     *
     * @param columns the names of the columns to add
     * @return this {@code AlterTableDropColumns} instance
     */
    public AlterTableDropColumns columns(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * Returns the name of this operation, which is "drop".
     *
     * @return the operation name
     */
    @Override
    public String getOperationName() {
        return "drop";
    }
}
