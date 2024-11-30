package com.datastax.astra.client.tables.commands.options;

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

import com.datastax.astra.client.core.commands.BaseOptions;
import com.datastax.astra.client.core.commands.CommandType;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * Represents options for altering a table in a database schema.
 * Extends {@link BaseOptions} to provide additional functionality for table alteration commands.
 * <p>
 * This class supports a fluent, chainable API using the {@link Accessors} annotation.
 * Common options include specifying whether to include an "IF EXISTS" condition during the operation.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTableOptions options = new AlterTableOptions()
 *     .ifExists(true);
 * }
 * </pre>
 */
@Setter
@Accessors(fluent = true, chain = true)
public class AlterTableOptions extends BaseOptions<AlterTableOptions> {

    /**
     * A predefined instance of {@code AlterTableOptions} with the "IF EXISTS" condition enabled.
     * This improves syntax for commonly used configurations.
     */
    public static final AlterTableOptions IF_EXISTS = new AlterTableOptions().ifExists(true);

    /**
     * Indicates whether the "IF EXISTS" condition should be applied to the table alteration.
     * When {@code true}, the operation will only proceed if the table exists.
     */
    private boolean ifExists = true;

    /**
     * Constructs a new {@code AlterTableOptions} instance with default settings.
     * Initializes the options with {@link CommandType#TABLE_ADMIN} and the default table serializer.
     */
    public AlterTableOptions() {
        super(null, CommandType.TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, null);
    }

    /**
     * Retrieves the value of the "IF EXISTS" condition.
     * This condition determines whether the operation should check for the existence of the table.
     *
     * @return {@code true} if the "IF EXISTS" condition is enabled, {@code false} otherwise
     */
    public boolean isIfExists() {
        return ifExists;
    }
}

