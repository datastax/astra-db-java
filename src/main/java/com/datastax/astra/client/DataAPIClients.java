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

import com.datastax.astra.client.internal.observer.LoggingCommandObserver;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.internal.auth.StargateAuthenticationService;

import static com.datastax.astra.client.admin.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * Initialization of the client in a Static way.
 */
public class DataAPIClients {

    /** Default endpoint. */
    public static final String DEFAULT_ENDPOINT = "http://localhost:8181";

    /**
     * Utility class, should not be instanced.
     */
    private DataAPIClients() {}

    /**
     * Create DataApiClient to interact with a local instance of Stargate.
     * @return
     *      DataApiClient for local instance
     *
     */
    public static DataAPIClient localClient() {
        return new DataAPIClient(
                new StargateAuthenticationService().getToken(),
                DataAPIOptions.builder().withDestination(DataAPIOptions.DataAPIDestination.CASSANDRA).build());
    }

    /**
     * Create DataApiClient and Database to interact with a local instance of Stargate.
     *
     * @return
     *    Database client for local instance
     */
    public static Database localDatabase() {
        Database db = localClient().getDatabase(DEFAULT_ENDPOINT, DEFAULT_NAMESPACE);
        db.registerListener("logger", new LoggingCommandObserver(Database.class));
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) db.getDatabaseAdmin();
        dbAdmin.registerListener("logger", new LoggingCommandObserver(Database.class));
        dbAdmin.createNamespace(DEFAULT_NAMESPACE);
        return db;
    }


    /**
     * Create DataApiClient and Database to interact with Astra,
     *
     * @param token
     *      authentication token
     * @return
     *      Database client to use Astra
     */
    public static DataAPIClient astra(String token) {
        return new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIOptions.DataAPIDestination.ASTRA)
                .build());
    }

    /**
     * Create DataApiClient and Database to interact with Astra in development environment.
     *
     * @param token
     *      authentication token
     * @return
     *      Database client to use Astra DEV
     */
    public static DataAPIClient astraDev(String token) {
        return new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIOptions.DataAPIDestination.ASTRA_DEV)
                .build());
    }

    /**
     * Create DataApiClient and Database to interact with Astra in test  environment.
     *
     * @param token
     *      authentication token
     * @return
     *      Database client to use Astra TEST
     */
    public static DataAPIClient astraTest(String token) {
        return new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIOptions.DataAPIDestination.ASTRA_TEST)
                .build());
    }

}
