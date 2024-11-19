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

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.DatabaseSerializer;
import com.datastax.astra.internal.utils.Assert;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Options used to connect to a database. If not provided, the default options are used.
 */
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class DatabaseOptions implements Cloneable {

    /**
     * The default keyspace to use for the database.
     */
    public static final String DEFAULT_KEYSPACE = "default_keyspace";

    /** Serializer for the Collections. */
    private static final DataAPISerializer DEFAULT_SERIALIZER = new DatabaseSerializer();

    /**
     * The keyspace to use for the database.
     */
    String keyspace = DEFAULT_KEYSPACE;

    /**
     * The token to use for the database.
     */
    String token;

    /**
     * The options to use for the data API client.
     */
    DataAPIClientOptions dataAPIClientOptions;

    /**
     * The serializer for the database Objects.
     */
    DataAPISerializer serializer = DEFAULT_SERIALIZER;

    /**
     * Constructor with options and not token override.
     *
     * @param options
     *      data API client options
     */
    public DatabaseOptions(DataAPIClientOptions options) {
        Assert.notNull(options, "options");
        this.dataAPIClientOptions = options.clone();
    }

    /**
     * Gets keyspace
     *
     * @return value of keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Gets token
     *
     * @return value of token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets dataAPIClientOptions
     *
     * @return value of dataAPIClientOptions
     */
    public DataAPIClientOptions getDataAPIClientOptions() {
        return dataAPIClientOptions;
    }

    /**
     * Gets databaseSerializer
     *
     * @return value of databaseSerializer
     */
    public DataAPISerializer getSerializer() {
        return serializer;
    }

    @Override
    public DatabaseOptions clone() {
        try {
            DatabaseOptions cloned = (DatabaseOptions) super.clone();
            // Perform deep cloning for mutable objects
            cloned.dataAPIClientOptions = (this.dataAPIClientOptions != null)
                    ? this.dataAPIClientOptions.clone()
                    : null;
            cloned.serializer = (this.serializer != null) ? this.serializer : null;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
}
