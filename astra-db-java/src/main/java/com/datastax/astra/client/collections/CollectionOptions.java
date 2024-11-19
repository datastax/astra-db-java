package com.datastax.astra.client.collections;

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

import com.datastax.astra.client.core.commands.CommandOptions;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;

import javax.print.Doc;

/**
 * The options to use for the data API client.
 */
public class CollectionOptions<T> extends CommandOptions<CollectionOptions<T>> {

    public static final DataAPISerializer DEFAULT_SERIALIZER = new DocumentSerializer();

    /**
     * The class to use for the collection.
     */
    private final Class<T> clazz;

    /**
     * Enforcing the command type for this class.
     *
     * @param db
     *      parent database
     * @param clazz
     *      current class
     */
    public CollectionOptions(Database db, Class<T> clazz) {
        this(db.getDatabaseOptions().getToken(), db.getDatabaseOptions().getDataAPIClientOptions(), clazz);
    }

    /**
     * Enforcing the command type for this class.
     *
     * @param token
     *      current token
     * @param options
     *      data API client options to override if needed
     */
    public CollectionOptions(String token, DataAPIClientOptions options, Class<T> clazz) {
        super(token, CommandType.GENERAL_METHOD, options);
        this.clazz = clazz;
        serializer(DEFAULT_SERIALIZER);
    }

    /**
     * Gets clazz
     *
     * @return value of clazz
     */
    public Class<T> getClazz() {
        return clazz;
    }
}
