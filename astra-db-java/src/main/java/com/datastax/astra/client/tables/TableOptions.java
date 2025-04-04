package com.datastax.astra.client.tables;

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

import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.datastax.astra.client.core.commands.CommandType.TABLE_ADMIN;
import static com.datastax.astra.client.tables.Table.DEFAULT_TABLE_SERIALIZER;

/**
 * The options to use for the data API client.
 */
@Setter
@Accessors(fluent = true, chain = true)
public class TableOptions extends BaseOptions<TableOptions> {

    /**
     * The keyspace to use for the database.
     */
    String keyspace = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /**
     * Default constructor nor overriding token nor options
     */
    public TableOptions() {
        this(null, null);
    }

    /**
     * Constructor with options and not token override.
     *
     * @param token
     *      the token to use for the database
     * @param options
     *      data API client options
     */
    public TableOptions(String token, DataAPIClientOptions options) {
        super(token, TABLE_ADMIN, DEFAULT_TABLE_SERIALIZER, options);
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
