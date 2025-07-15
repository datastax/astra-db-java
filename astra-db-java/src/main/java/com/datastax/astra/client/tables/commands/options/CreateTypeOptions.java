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
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * Set of options used when creating a table
 */
@Setter
@Accessors(fluent = true, chain = true)
public class CreateTypeOptions extends BaseOptions<CreateTypeOptions> {

    /** Improve syntax. */
    public static final CreateTypeOptions IF_NOT_EXISTS = new CreateTypeOptions().ifNotExists(true);

    /**
     * The keyspace to use for the database.
     */
    String keyspace = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /**
     * Condition to upsert the table.
     */
    boolean ifNotExists = true;

    /**
     * Default constructor
     */
    public CreateTypeOptions() {
        super(null, CommandType.TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, null);
    }

    /**
     * Accessor for serialization.
     *
     * @return
     *      accessor for serialization
     */
    public boolean isIfNotExists() {
        return ifNotExists;
    }

    /**
     * Gets keyspace
     *
     * @return value of keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

}
