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

import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.internal.command.LoggingCommandObserver;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;

/**
 * Provides utility methods for initializing and configuring clients to interact with the Data API. This class
 * simplifies the creation of Data API clients by abstracting the complexities associated with configuring
 * clients for different environments and settings.
 *
 * <p>Depending on the application's requirements and the operational environment, {@code DataAPIClients} can
 * tailor the Data API client with appropriate configuration options such as authentication credentials, connection
 * timeouts, proxy settings, and more. This enables developers to quickly and easily set up their Data API clients
 * without delving into the intricate details of each configuration option.</p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * {@code
 * // Get you the client for a local deployment of Data API
 * DataAPIClient devClient = DataAPIClients.localClient();
 *
 * // Get you the database for a local deployment of Data API
 * DataAPIClient devClient = DataAPIClients.localDatabase();
 *
 * // Default target environment Astra Production
 * DataAPIClient devClient = DataAPIClients.astra("token");
 * }
 * </pre>
 *
 * <p>Note: This class should be used as a starting point for initializing Data API clients. It is advisable to
 * review the specific configuration options relevant to your use case and adjust them accordingly.</p>
 */
public class DataAPIClients {

    /** Default Http endpoint for local deployment. */
    public static final String DEFAULT_ENDPOINT_LOCAL = "http://localhost:8181";

    /**
     * Utility class, should not be instanced.
     */
    private DataAPIClients() {}

    /**
     * Creates and configures a {@link DataAPIClient} for interaction with a local instance of Stargate, a
     * data gateway for working with Apache Cassandra®. This method is specifically designed for scenarios
     * where the application is intended to communicate with a Stargate instance running locally, facilitating
     * development and testing workflows by providing easy access to local database resources.
     *
     * @return A fully configured {@link DataAPIClient} ready for interacting with the local Stargate instance, equipped
     *         with the necessary authentication token and targeting options for Cassandra. This client abstracts away
     *         the complexities of direct database communication, providing a simplified interface for data operations.
     */
    public static DataAPIClient local() {
        return new DataAPIClient(
                new UsernamePasswordTokenProvider().getToken(),
                new DataAPIClientOptions()
                        .destination(DataAPIDestination.CASSANDRA)
                        .enableFeatureFlagTables()
                        .logRequests()
                        .addObserver(new LoggingCommandObserver(DataAPIClient.class)));
    }

    /**
     * Creates a {@link DataAPIClient} configured for interacting with Astra in a development environment. This
     * method simplifies the setup of a client specifically tailored for development purposes, where you might
     * need different configurations or less stringent security measures compared to a production environment.
     * The client is configured to target Astra's development environment, ensuring that operations do not
     * affect production data.
     *
     * @param token The authentication token required for accessing Astra's development environment. This token
     *              should have the necessary permissions for development activities and be protected accordingly.
     * @return A {@link DataAPIClient} instance ready for development activities with Astra, configured with the
     *         provided authentication token and targeting Astra's development environment.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient devClient = DataAPIClients.astraDev("your_astra_dev_token");
     * // Utilize devClient for development database operations
     * }
     * </pre>
     */
    public static DataAPIClient astra(String token) {
        return new DataAPIClient(token, new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA)
                .addObserver(new LoggingCommandObserver(DataAPIClient.class)));
    }

    /**
     * Creates a {@link DataAPIClient} configured for interacting with Astra in a development environment. This
     * method simplifies the setup of a client specifically tailored for development purposes, where you might
     * need different configurations or less stringent security measures compared to a production environment.
     * The client is configured to target Astra's development environment, ensuring that operations do not
     * affect production data.
     *
     * @param token The authentication token required for accessing Astra's development environment. This token
     *              should have the necessary permissions for development activities and be protected accordingly.
     * @return A {@link DataAPIClient} instance ready for development activities with Astra, configured with the
     *         provided authentication token and targeting Astra's development environment.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient devClient = DataAPIClients.astraDev("your_astra_dev_token");
     * // Utilize devClient for development database operations
     * }
     * </pre>
     */
    public static DataAPIClient astraDev(String token) {
        return new DataAPIClient(token, new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA_DEV)
                .addObserver(new LoggingCommandObserver(DataAPIClient.class)));
    }

    /**
     * Creates a {@link DataAPIClient} specifically configured for interacting with Astra in a test environment.
     * This setup is ideal for testing scenarios, where isolation from development and production environments
     * is critical to ensure the integrity and stability of test results. By directing the client to Astra's
     * test environment, it facilitates safe, isolated testing of database interactions without risking the
     * alteration of development or production data.
     *
     * @param token The authentication token required for accessing Astra's test environment. Ensure that this
     *              token is designated for testing purposes to prevent unintended access to or effects on
     *              non-test data and resources.
     * @return A {@link DataAPIClient} instance specifically for use in testing scenarios with Astra, equipped
     *         with the necessary authentication token and configured to target the test environment.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient testClient = DataAPIClients.astraTest("your_astra_test_token");
     * // Execute test database operations with testClient
     * }
     * </pre>
     */
    public static DataAPIClient astraTest(String token) {
        return new DataAPIClient(token, new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA_TEST)
                .addObserver(new LoggingCommandObserver(DataAPIClient.class)));
    }

    /**
     * Creates and configures a {@link Database} client specifically designed for interaction with a local instance
     * of Stargate. This method streamlines the process of setting up a client for local database interactions,
     * encapsulating both the creation of a {@link DataAPIClient} and its integration within a {@link Database}
     * abstraction. This setup is ideal for local development and testing, providing a straightforward path to
     * interact with Cassandra through Stargate with minimal setup.
     *
     * @return A {@link Database} client ready for use with a local Stargate instance, fully configured for immediate
     *         interaction with the database. This client enables developers to focus on their application logic rather
     *         than the intricacies of database connectivity and command execution.
     */
    public static Database defaultLocalDatabase() {
        Database db = local().getDatabase(DEFAULT_ENDPOINT_LOCAL);
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) db.getDatabaseAdmin();
        dbAdmin.createKeyspace(DEFAULT_KEYSPACE);
        return db;
    }



}
