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

import com.datastax.astra.client.observer.LoggingCommandObserver;
import com.datastax.astra.internal.DataAPIDatabaseAdmin;
import com.datastax.astra.internal.auth.StargateAuthenticationService;

import static com.datastax.astra.client.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * Initialization of the client in a Static way.
 */
public class DataAPIClients {

    /** Default endpoint. */
    public static final String DEFAULT_ENDPOINT = "http://localhost:8181";

    /** Default endpoint. */
    public static final String PATH_HEALTH_CHECK = "/stargate/health";

    /** Default service id. */
    public static final String DEFAULT_SERVICE_ID = "sgv2-json";

    /** Default datacenter id. */
    private static final String DEFAULT_DATACENTER = "dc1";

    /**
     * Utility class, should not be instanced.
     */
    private DataAPIClients() {}

    /**
     * Create from an Endpoint only
     */
    public static DataAPIClient localClient() {
        return new DataAPIClient(
                new StargateAuthenticationService().getToken(),
                DataAPIOptions.builder().withDestination(DataAPIDestination.CASSANDRA).build());
    }

    public static Database localDatabase() {
        Database db = localClient().getDatabase(DEFAULT_ENDPOINT, DEFAULT_NAMESPACE);
        db.registerListener("logger", new LoggingCommandObserver(Database.class));
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) db.getDatabaseAdmin();
        dbAdmin.registerListener("logger", new LoggingCommandObserver(Database.class));
        dbAdmin.createNamespace(DEFAULT_NAMESPACE);
        return db;
    }

    public static DataAPIClient astra(String token) {
        return new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIDestination.ASTRA)
                .build());
    }

    public static DataAPIClient astraDev(String token) {
        return new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIDestination.ASTRA_DEV)
                .build());
    }

    public static DataAPIClient astraTest(String token) {
        return new DataAPIClient(token, DataAPIOptions
                .builder()
                .withDestination(DataAPIDestination.ASTRA_TEST)
                .build());
    }


}
