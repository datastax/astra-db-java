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

import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.DatabaseSerializer;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents the configuration options required to connect to a database. This class encapsulates
 * various settings, such as authentication details, API version, and keyspace configuration,
 * enabling flexible and customized database connections.
 *
 * <p>If not explicitly provided, default options will be used. This ensures ease of use for
 * developers while still allowing fine-grained control over the connection configuration when needed.</p>
 *
 * <p>This class is annotated with {@code @Setter} and {@code @Accessors(fluent = true, chain = true)},
 * enabling a fluent, chainable API for setting properties.</p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Fluent setters for convenient configuration of options.</li>
 *   <li>Support for default configurations when options are not specified.</li>
 *   <li>Encapsulation of essential parameters such as keyspace, API version, and authentication token.</li>
 * </ul>
 *
 * <h2>Example usage:</h2>
 * <pre>
 * {@code
 * // Create a DatabaseOptions object with custom settings
 * DatabaseOptions options = new DatabaseOptions()
 *         .keyspace("my_keyspace")
 *         .authToken("my_auth_token")
 *         .apiVersion("v2")
 *         .logRequests(true);
 *
 * // Use the options when initializing a Database instance
 * Database database = new Database("https://my-endpoint.com", options);
 * }
 * </pre>
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
     * Default constructor.
     */
    public DatabaseOptions() {
        this(null, null);
    }

    /**
     * Constructor with options and not token override.
     *
     * @param token
     *      authentication token
     * @param options
     *      data API client options
     */
    public DatabaseOptions(String token, DataAPIClientOptions options) {
        super(token, CommandType.KEYSPACE_ADMIN, DEFAULT_SERIALIZER, options);
    }

    /**
     * Gets keyspace
     *
     * @return value of keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    /** {@inheritDoc} */
    @Override
    public DatabaseOptions clone() {
        // Cloning options, token, and serializer
        DatabaseOptions cloned = (DatabaseOptions) super.clone();
        cloned.token    = token;
        cloned.keyspace = keyspace;
        return cloned;
    }
}
