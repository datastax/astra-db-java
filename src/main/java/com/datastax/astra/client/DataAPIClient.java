package com.datastax.astra.client;

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

import com.datastax.astra.internal.astra.AstraApiEndpoint;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * Entry point to access the Data API.
 */
public class DataAPIClient {

    /** Token in use with the solution. */
    private final String token;

    /** Options to setup the client. */
    private final DataAPIOptions options;

    /**
     * Constructor with a token
     *
     * @param token
     *      authentication token.
     */
    public DataAPIClient(String token) {
        this(token, DataAPIOptions.builder().build());
    }

    /**
     * Constructor with a token and options.
     *
     * @param token
     *      authentication token
     * @param options
     *      set of options for the http client
     */
    public DataAPIClient(String token, DataAPIOptions options) {
        Assert.hasLength(token, "token");
        Assert.notNull(options, "options");
        this.token   = token;
        this.options = options;
    }

    // --------------------------------------------------
    // ---       Access AstraDBAdmin                  ---
    // --------------------------------------------------

    /**
     * Access the administration client (crud on databases).
     *
     * @return
     *      administration client
     */
    public AstraDBAdmin getAdmin() {
        return getAdmin(this.token);
    }

    /**
     * Access the administration client (crud on databases) by providing a stronger token.
     *
     * @param superUserToken
     *      provide a token with enough privileges for admin operations
     * @return
     *      administration client
     */
    public AstraDBAdmin getAdmin(String superUserToken) {
       return new AstraDBAdmin(superUserToken, getAstraEnvironment(), options);
    }

    /**
     * Allow to find the environment from the destination.
     *
     * @return
     *      current astra environment
     */
    private Optional<AstraEnvironment> findAstraEnvironment() {
        if (options.getDestination() != null) {
            switch (options.getDestination()) {
                case ASTRA:
                    return Optional.of(AstraEnvironment.PROD);
                case ASTRA_DEV:
                    return Optional.of(AstraEnvironment.DEV);
                case ASTRA_TEST:
                    return Optional.of(AstraEnvironment.TEST);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the current Astra Environment.
     *
     * @return
     *      current astra environment
     */
    private AstraEnvironment getAstraEnvironment() {
        return findAstraEnvironment()
                .orElseThrow(() -> new IllegalArgumentException("'destination' should be ASTRA* to use the AstraDBAdmin"));
    }

    // --------------------------------------------------
    // ---       Access Database                      ---
    // --------------------------------------------------

    /**
     * Access a database client to interact with the Data API.
     *
     * @param databaseId
     *      unique identifier of the database
     * @param namespace
     *      namespace to use
     * @return
     *      database client
     */
    public Database getDatabase(UUID databaseId, String namespace) {
        Assert.notNull(databaseId, "databaseId");
        Assert.hasLength(namespace, "namespace");
        return new Database(new AstraApiEndpoint(databaseId,
                getAdmin().getDatabaseInformations(databaseId).getInfo().getRegion(),
                getAstraEnvironment()).getApiEndPoint(),
                namespace);
    }

    /**
     * Access a database client to interact with the Data API.
     * @param databaseId
     *      unique identifier of the database
     * @return
     *      database client
     */
    public Database getDatabase(UUID databaseId) {
        return getDatabase(databaseId, DEFAULT_NAMESPACE);
    }

    /**
     * Access a database client to interact with the Data API.
     * @param apiEndpoint
     *      url of the api in used
     * @param namespace
     *      namespace to use
     * @return
     *      database client
     */
    public Database getDatabase(String apiEndpoint, String namespace) {
        return new Database(apiEndpoint, token, namespace, options);
    }

    /**
     * Access a database client to interact with the Data API.
     *
     * @param apiEndpoint
     *      url of the api in used
     * @return
     *      database client
     */
    public Database getDatabase(String apiEndpoint) {
        return getDatabase(apiEndpoint, DEFAULT_NAMESPACE);
    }




}
