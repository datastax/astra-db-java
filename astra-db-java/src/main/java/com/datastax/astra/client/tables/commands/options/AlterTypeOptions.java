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

import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.BaseOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * Represents options for altering a type in a database schema.
 * Extends {@link BaseOptions} to provide additional functionality for type alteration commands.
 * <p>
 * This class supports a fluent, chainable API using the {@link Accessors} annotation.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTypeOptions options = new AlterTypeOptions();
 * }
 * </pre>
 */
@Setter
@Accessors(fluent = true, chain = true)
public class AlterTypeOptions extends BaseOptions<AlterTypeOptions> {

    /**
     * Constructs a new {@code AlterTypeOptions} instance with default settings.
     * Initializes the options with {@link CommandType#TABLE_ADMIN} and the default table serializer.
     */
    public AlterTypeOptions() {
        super(null, CommandType.TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, null);
    }
}

