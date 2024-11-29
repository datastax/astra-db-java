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

import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;

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
 * DataAPIClient devClient = DataAPIClients.local();
 *
 * // Get you the database for a local deployment of Data API
 * DataAPIClient devClient = DataAPIClients.astraDev("token");
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
     * Creates and configures a {@link DataAPIClient} for interaction with a local instance of DataAPI,
     * a data gateway that facilitates working with Apache Cassandra®. This method is tailored for
     * development and testing workflows, enabling simplified and efficient access to local database
     * resources without the need for extensive configuration.
     *
     * <p>The returned {@link DataAPIClient} is preconfigured with:
     * <ul>
     *   <li>An authentication token from {@link UsernamePasswordTokenProvider}.</li>
     *   <li>A destination set to {@code DataAPIDestination.CASSANDRA}.</li>
     *   <li>Feature flags for tables enabled.</li>
     *   <li>Request logging enabled.</li>
     * </ul>
     *
     * @return A fully configured {@link DataAPIClient} ready for interacting with the local DataAPI instance.
     *         This client provides a streamlined interface for executing data operations, abstracting away
     *         the complexity of direct database interactions.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient client = DataAPIClients.local();
     * }
     * </pre>
     */
    public static DataAPIClient clientCassandra() {
        return clientCassandra(
                UsernamePasswordTokenProvider.DEFAULT_USERNAME,
                UsernamePasswordTokenProvider.DEFAULT_CREDENTIALS);
    }

    public static DataAPIClient clientCassandra(String username, String password) {
        return new DataAPIClient(
                new UsernamePasswordTokenProvider(username, password).getToken(),
                new DataAPIClientOptions()
                        .destination(DataAPIDestination.CASSANDRA)
                        .enableFeatureFlagTables()
                        .logRequests());
    }

    public static DataAPIClient clientHCD() {
        return clientHCD(
                UsernamePasswordTokenProvider.DEFAULT_USERNAME,
                UsernamePasswordTokenProvider.DEFAULT_CREDENTIALS);
    }

    public static DataAPIClient clientHCD(String username, String password) {
        return new DataAPIClient(
                new UsernamePasswordTokenProvider(username, password).getToken(),
                new DataAPIClientOptions()
                        .destination(DataAPIDestination.HCD)
                        .enableFeatureFlagTables()
                        .logRequests());
    }

    /**
     * Creates and configures a {@link Database} client specifically designed for interaction with a local instance
     * of the Data API and Cassandra. This method simplifies the setup process by combining the creation of a {@link DataAPIClient}
     * with the integration of a {@link Database} abstraction. It is tailored for local development and testing,
     * enabling seamless interaction with Apache Cassandra® through Stargate with minimal configuration.
     *
     * <p>Upon creation, this method ensures that a default keyspace is available in the local Stargate instance
     * by automatically invoking {@link com.datastax.astra.client.admin.DatabaseAdmin#createKeyspace(String)}. This guarantees that developers
     * have a ready-to-use environment for executing database operations during their development or testing workflows.
     *
     * <p>The returned {@link Database} client is preconfigured with:
     * <ul>
     *   <li>A connection to the default local Stargate endpoint.</li>
     *   <li>An automatically created keyspace, identified by {@code DEFAULT_KEYSPACE}.</li>
     * </ul>
     * This setup allows developers to focus on application logic rather than database configuration or connectivity.
     *
     * @return A {@link Database} client configured for use with a local Stargate instance, including a default
     *         keyspace for immediate interaction. This client abstracts database connectivity and administrative tasks,
     *         streamlining development workflows.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Database db = localDbWithDefaultKeyspace();
     * }
     * </pre>
     */
    public static Database localDbWithDefaultKeyspace() {
        Database db = clientCassandra().getDatabase(DEFAULT_ENDPOINT_LOCAL);
        db.getDatabaseAdmin().createKeyspace(DEFAULT_KEYSPACE);
        return db;
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
                .logRequests());
    }

    /**
     * Creates a {@link DataAPIClient} configured for interacting with Astra in a production environment. This
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
     * DataAPIClient devClient = DataAPIClients.astra("your_astra_dev_token");
     * // Utilize devClient for development database operations
     * }
     * </pre>
     */
    public static DataAPIClient astra(String token) {
        return new DataAPIClient(token, new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA)
                .logRequests());
    }



    /**
     * Creates a {@link DataAPIClient} specifically configured for interacting with Astra in a test environment.
     * This method is designed for testing scenarios, providing an isolated environment to safely execute
     * database operations without impacting development or production data.
     *
     * <p>The returned {@link DataAPIClient} is preconfigured to:
     * <ul>
     *   <li>Authenticate using the provided test-specific token.</li>
     *   <li>Target the {@code DataAPIDestination.ASTRA_TEST} environment.</li>
     *   <li>Enable request logging for better visibility during test operations.</li>
     * </ul>
     *
     * This setup ensures that all database interactions are restricted to Astra's test environment,
     * preserving the integrity of other environments while facilitating thorough testing.
     *
     * @param token The authentication token required for accessing Astra's test environment. It is important
     *              to use a token that is explicitly designated for testing purposes to avoid unintended
     *              access to production or development resources.
     * @return A {@link DataAPIClient} instance configured for testing with Astra, equipped with the provided
     *         authentication token and targeting the test environment.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient testClient = DataAPIClients.astraTest("your_astra_test_token");
     * testClient.execute(query -> query.cql("SELECT * FROM test_table").execute());
     * }
     * </pre>
     */
    public static DataAPIClient astraTest(String token) {
        return new DataAPIClient(token, new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA_TEST)
                .logRequests());
    }

}
