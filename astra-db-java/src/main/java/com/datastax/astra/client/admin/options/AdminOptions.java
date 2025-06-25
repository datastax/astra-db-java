package com.datastax.astra.client.admin.options;

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
 * Represents the configuration options for administrative operations in the database API.
 * This class extends {@link BaseOptions} to inherit common configuration options
 * and provides additional functionality specific to administrative commands.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AdminOptions options = new AdminOptions()
 *     .token("your-token-here")
 *     .options(new DataAPIClientOptions())
 *     .serializer(new CustomSerializer());
 * }
 * </pre>
 *
 * <p>Key Features:</p>
 * <ul>
 *   <li>Provides default serialization through {@link DatabaseSerializer}.</li>
 *   <li>Fluent and chainable setter methods for seamless configuration.</li>
 *   <li>Supports administrative command types via {@link CommandType#DATABASE_ADMIN}.</li>
 * </ul>
 */
@Setter
@Accessors(fluent = true, chain = true)
public class AdminOptions extends BaseOptions<AdminOptions> {

    /**
     * Serializer for the collections.
     * Defaults to {@link DatabaseSerializer}.
     */
    public static final DataAPISerializer DEFAULT_SERIALIZER = new DatabaseSerializer();

    /**
     * Serializer for the collections.
     * Defaults to {@link DatabaseSerializer}.
     */
    public AdminOptions() {
        this(null, null);
    }

    /**
     * Constructs an {@code AdminOptions} instance with the specified token and options.
     *
     * @param token the authentication token to use.
     * @param options additional configuration options for the Data API client.
     */
    public AdminOptions(String token, DataAPIClientOptions options) {
        super(token, CommandType.DATABASE_ADMIN, DEFAULT_SERIALIZER, options);
    }

}
