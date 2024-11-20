package com.datastax.astra.client.databases;

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
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.DatabaseSerializer;
import com.datastax.astra.internal.utils.Assert;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Options used to connect to a database. If not provided, the default options are used.
 */
@Setter
@Accessors(fluent = true, chain = true)
public class DatabaseOptions extends BaseOptions<DatabaseOptions> implements Cloneable {

    /** Serializer for the Collections. */
    private static final DataAPISerializer DEFAULT_SERIALIZER = new DatabaseSerializer();

    /**
     * The keyspace to use for the database.
     */
    String keyspace = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /**
     * Constructor with options and not token override.
     *
     * @param options
     *      data API client options
     */
    public DatabaseOptions(String token, DataAPIClientOptions options) {
        Assert.notNull(options, "options");
        this.dataAPIClientOptions = options.clone();
        this.token = token;
        this.serializer = new DatabaseSerializer();
    }

    /**
     * Gets keyspace
     *
     * @return value of keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    @Override
    public DatabaseOptions clone() {
        // Cloning options, token, and serializer
        DatabaseOptions cloned = (DatabaseOptions) super.clone();
        cloned.keyspace = keyspace;
        return cloned;
    }
}
