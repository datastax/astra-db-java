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

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AdminOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.datastax.astra.internal.utils.Assert;

import java.util.UUID;

import static com.datastax.astra.client.exceptions.InvalidEnvironmentException.throwErrorRestrictedAstra;

/**
 * Serves as the primary entry point to the Data API client, providing an intuitive and streamlined interface
 * for interacting with the Data API. The client is compatible with both Astra environments and on-premise
 * DataStax Enterprise installations, offering flexibility across deployment scenarios.
 *
 * <p>The {@code DataAPIClient} is designed to simplify database interactions by providing:</p>
 *
 * <ul>
 *   <li>A high-level, user-friendly API that adheres to fluent programming principles.</li>
 *   <li>Support for builder patterns to accommodate complex client configurations.</li>
 *   <li>Idiomatic method naming and design inspired by the MongoDB API, ensuring a familiar experience for developers.</li>
 * </ul>
 *
 * <p>This design philosophy facilitates quick onboarding and ease of use while enabling advanced customizations when needed.</p>
 *
 * <h2>Core Features:</h2>
 * <ul>
 *   <li>Data manipulation capabilities for databases, collections, and tables.</li>
 *   <li>Administrative operations, such as database and keyspace creation (requires appropriate privileges).</li>
 *   <li>Support for destination-specific options, including Astra and DSE environments.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * DataAPIClientOptions options = new DataAPIClientOptions()
 *         .destination(DataAPIDestination.DSE) // Set the destination
 *         .httpClientOptions(new HttpClientOptions()
 *                 .httpRedirect(HttpClient.Redirect.NORMAL) // Configure HTTP redirects
 *                 .httpProxy(new HttpProxy("localhost", 8080)) // Set up an HTTP proxy
 *                 .httpRetries(1, Duration.ofSeconds(10))) // Configure retries
 *         .timeoutOptions(new TimeoutOptions()
 *                 .requestTimeoutMillis(1000)) // Set request timeout
 *         .enableFeatureFlagTables() // Enable feature flag for tables
 *         .addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true"); // Add custom headers
 * DataAPIClient client = new DataAPIClient("token", options);
 * }</pre>
 */
public class DataAPIClient {

    /**
     * The authentication token used as credentials in HTTP requests, specifically as the Authorization bearer token.
     * This token is crucial for accessing and interacting with Astra environments, where it plays a role in determining
     * the access level (e.g., Administrator, User) and scope (e.g., Database, Organization) of the bearer.
     * <p>
     * Tokens generated within Astra environments must adhere to a specific format, starting with "AstraCS:..". These tokens
     * are obtained via the Astra UI during the database creation process and are designed to not expire, ensuring long-term
     * access.
     * </p>
     * <p>
     * In scenarios involving DataStax Enterprise or on-premise deployments, authentication tokens must be acquired through
     * the authentication endpoint provided by Stargate. For more information on generating and managing tokens in these contexts,
     * refer to the Stargate authentication documentation:
     * <a href="https://stargate.io/docs/latest/secure/authnz.html">Stargate Authentication & Authorization</a>.
     * </p>
     * @see <a href="https://stargate.io/docs/latest/secure/authnz.html">Stargate Authentication & Authorization Documentation</a>
     */
    private final String token;

    /**
     * Represents the advanced configuration settings for the client, encapsulating various HTTP-related properties.
     * This includes settings such as the HTTP version, request and connection timeouts, and the user agent among others.
     * <p>
     * Providing an instance of {@code DataAPIOptions} allows for fine-grained control over the HTTP behavior of the client,
     * enabling customization to fit specific requirements. If an instance is not explicitly provided, the client
     * defaults to a pre-configured set of options designed for general use.
     * </p>
     **
     * @see DataAPIClientOptions for more details on the available configuration parameters and their effects.
     */
    private final DataAPIClientOptions options;

    /**
     * Constructs a {@code DataAPIClient} instance using the specified authentication token. This constructor
     * initializes the client with default {@link DataAPIClientOptions} for its configuration.
     * <p>
     * The provided token is used for authenticating HTTP requests made by this client. It is essential for accessing
     * secured resources. If specific HTTP configurations are required (e.g., custom timeouts, HTTP version), use the
     * other constructor that accepts both a token and a {@link DataAPIClientOptions} instance.
     * </p>
     * <p>
     * This constructor is suitable for scenarios where default client settings are sufficient and no advanced
     * configuration is needed. It simplifies the initialization process for quick setup and use.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient client = new DataAPIClient("token");
     * }
     * </pre>
     *
     * @param token The authentication token to be used for HTTP requests. This token should follow the format expected
     *              by the server, typically starting with "AstraCS:.." for Astra environments.
     */
    public DataAPIClient(String token) {
        this(token, new DataAPIClientOptions());
    }

    /**
     * Constructs a {@code DataAPIClient} with specified authentication token and HTTP client configuration options.
     * <p>
     * This constructor allows for the explicit specification of both the authentication token and the advanced
     * HTTP configuration settings. The authentication token is essential for securing access to the API, while the
     * {@link DataAPIClientOptions} object provides granular control over the HTTP client's behavior, including timeouts,
     * HTTP version, and other properties impacting connectivity and request handling.
     * </p>
     * <p>
     * It is recommended to use this constructor when you need to customize the HTTP client beyond the default
     * configuration, such as setting custom timeouts or specifying a particular HTTP protocol version. The provided
     * {@code Assert} methods ensure that neither the token nor the options are null or empty, enforcing the presence of
     * essential configuration details at the time of client initialization.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClientOptions options = new DataAPIClientOptions()
     *         .destination(DataAPIDestination.DSE) // Set the destination
     *         .httpClientOptions(new HttpClientOptions()
     *                 .httpRedirect(HttpClient.Redirect.NORMAL) // Configure HTTP redirects
     *                 .httpProxy(new HttpProxy("localhost", 8080)) // Set up an HTTP proxy
     *                 .httpRetries(1, Duration.ofSeconds(10))) // Configure retries
     *         .timeoutOptions(new TimeoutOptions()
     *                 .requestTimeoutMillis(1000)) // Set request timeout
     *         .enableFeatureFlagTables() // Enable feature flag for tables
     *         .addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true"); // Add custom headers
     * DataAPIClient client = new DataAPIClient("token", options);
     * }
     * </pre>
     *
     * @param token The authentication token to be used for securing API access. This token should adhere to the
     *              format required by the API, typically starting with "AstraCS:.." for Astra environments.
     * @param options The {@link DataAPIClientOptions} specifying the detailed HTTP client configurations, offering
     *                customization over aspects such as timeouts and protocol versions.
     * @throws IllegalArgumentException if the token is empty or null, or if the options are null.
     */
    public DataAPIClient(String token, DataAPIClientOptions options) {
        Assert.hasLength(token, "token");
        Assert.notNull(options, "options");
        this.token   = token;
        this.options = options;
    }

    /**
     * Constructs a new instance of the {@link DataAPIClient} without a default token. The token will instead need to
     * be specified when calling `.getDatabase()` or `.getAdmin()`. Prefer this method when using a db-scoped token
     * instead of a more universal token.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClientOptions options = new DataAPIClientOptions()
     *                 .timeoutOptions(new TimeoutOptions()
     *                     .connectTimeoutMillis(1000)
     *                     .requestTimeoutMillis(1000))
     *                 .httpClientOptions(new HttpClientOptions()
     *                     .httpVersion(HttpClient.Version.HTTP_2)
     *                 );
     * DataAPIClient myClient = new DataAPIClient(myOptions);
     * // The client is now ready to perform actions with custom configurations.
     * }
     * </pre>
     *
     * @param options - The default options to use when spawning new instances of {@link Database} or {@link AstraDBAdmin}.
     */
    public DataAPIClient(DataAPIClientOptions options) {
        Assert.notNull(options, "options");
        this.token   = null;
        this.options = options;
    }

    // --------------------------------------------------
    // ---       Access AstraDBAdmin                  ---
    // --------------------------------------------------

    /**
     * Retrieves an administration client specifically designed for Astra deployments. This client is used for
     * performing administrative tasks such as database creation, user management, and configuration adjustments.
     * It provides a programmatic interface for managing Astra resources securely and efficiently.
     *
     * <p>This method has three variants, allowing for flexibility in token usage:</p>
     * <ul>
     *   <li>{@link #getAdmin()}: Uses the authentication token provided during the {@code DataAPIClient} initialization.</li>
     *   <li>{@link #getAdmin(String superToken)}: Uses a custom token with elevated privileges, overriding the default token.</li>
     *   <li>{@link #getAdmin(AdminOptions adminOptions)}: Allows fine-grained control by specifying both the token and
     *       additional options.</li>
     * </ul>
     *
     * <p>To perform administrative tasks, the token must belong to a user with sufficient privileges (e.g., Database
     * Administrator or Organization Administrator). If these conditions are not met, a {@code SecurityException} is thrown.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Example 1: Using the default token provided at client initialization
     * DataAPIClient apiClient = new DataAPIClient("AstraCS:your_admin_token_here");
     * AstraDBAdmin adminClient = apiClient.getAdmin();
     * adminClient.createDatabase("new_database", "keyspace_name");
     *
     * // Example 2: Using a custom super token for administrative operations
     * AstraDBAdmin adminClientWithSuperToken = apiClient.getAdmin("AstraCS:your_super_admin_token_here");
     * adminClientWithSuperToken.createDatabase("another_database", "another_keyspace");
     *
     * // Example 3: Using advanced options for fine-grained control
     * AdminOptions options = new AdminOptions("AstraCS:custom_token", new DataAPIClientOptions().logRequests());
     * AstraDBAdmin advancedAdminClient = apiClient.getAdmin(options);
     * advancedAdminClient.createDatabase("custom_database", "custom_keyspace");
     * }
     * </pre>
     *
     * @param adminOptions
     *      The options to configure the administration client, including the authentication token.
     * @return An instance of {@link AstraDBAdmin} configured with the appropriate authentication token and options,
     *      ready for administrative operations.
     */
    public AstraDBAdmin getAdmin(AdminOptions adminOptions) {
        if (!options.isAstra()) {
            throwErrorRestrictedAstra("getAdmin()", options.getDestination());
        }
        return new AstraDBAdmin(adminOptions);
    }

    /**
     * Retrieves an administration client using the default authentication token provided during
     * {@code DataAPIClient} initialization.
     *
     * @return An instance of {@link AstraDBAdmin} configured with the default token.
     * @throws SecurityException if the token does not have the necessary privileges or the operation is not in an Astra environment.
     * @see #getAdmin(AdminOptions)
     */
    public AstraDBAdmin getAdmin() {
        return getAdmin(new AdminOptions(token, options));
    }

    /**
     * Retrieves an administration client using the default authentication token provided during
     * {@code DataAPIClient} initialization.
     *
     * @param superToken The custom token to use for administrative operations.
     * @return An instance of {@link AstraDBAdmin} configured with the default token.
     * @see #getAdmin(AdminOptions)
     */
    public AstraDBAdmin getAdmin(String superToken) {
        return getAdmin(new AdminOptions(superToken, options));
    }

    // --------------------------------------------------
    // ---       Access Database                      ---
    // --------------------------------------------------

    /**
     * Retrieves a database client configured to interact with the Data API. This client enables direct communication
     * with the specified Data API endpoint, supporting a wide range of data manipulation operations such as querying,
     * inserting, updating, and deleting data.
     *
     * <p>The {@code getDatabase} method has multiple variants to cater to different usage scenarios:</p>
     * <ul>
     *   <li>{@link #getDatabase(String)}: Connects to the Data API using a specified API endpoint with default options.</li>
     *   <li>{@link #getDatabase(UUID)}: Connects to the Data API using a database identifier, automatically resolving the endpoint.</li>
     *   <li>{@link #getDatabase(String, DatabaseOptions)}: Allows customization of database options while connecting to a specific API endpoint.</li>
     *   <li>{@link #getDatabase(UUID, DatabaseOptions)}: Resolves the endpoint using a database identifier and applies custom database options.</li>
     *   <li>{@link #getDatabase(UUID, String, DatabaseOptions)}: Provides fine-grained control by specifying the database ID, region, and additional options.</li>
     * </ul>
     *
     * <p>By providing flexibility in how connections are established and configured, these methods simplify the process
     * of interacting with Cassandra databases through the Data API. They are suitable for various deployment scenarios,
     * including Astra cloud services and on-premise installations.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Example 1: Connect using a direct API endpoint
     * String apiEndpoint = "https://<database_id>-<database_region>.apps.astra.datastax.com";
     * DataAPIClient apiClient = new DataAPIClient("yourAuthTokenHere");
     * Database databaseClient = apiClient.getDatabase(apiEndpoint);
     *
     * // Example 2: Connect using a database ID (with automatic endpoint resolution)
     * UUID databaseId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
     * Database databaseClientById = apiClient.getDatabase(databaseId);
     *
     * // Example 3: Customize options while connecting
     * DatabaseOptions options = new DatabaseOptions("yourAuthTokenHere", new DataAPIClientOptions().logRequests());
     * Database customDatabaseClient = apiClient.getDatabase(apiEndpoint, options);
     * }
     * </pre>
     *
     * @return A {@link Database} client tailored for interaction with the Data API, configured according to the
     *         provided parameters.
     *
     * @param apiEndpoint The URL of the Data API endpoint to connect to.
     * @param dbOptions The options to configure the database client.
     * @throws IllegalArgumentException If the provided parameters are invalid or insufficient for resolving the endpoint.
     */
    public Database getDatabase(String apiEndpoint, DatabaseOptions dbOptions) {
        return new Database(apiEndpoint, dbOptions);
    }

    /**
     * Retrieves a database client configured to connect to the Data API using the specified API endpoint and keyspace.
     * <p>
     * Uses default {@link DatabaseOptions} for configuration.
     * </p>
     *
     * @param apiEndpoint The URL of the Data API endpoint to connect to.
     * @param keyspace The name of the keyspace to use for database operations.
     * @return A {@link Database} client configured with default options for the specified endpoint and keyspace.
     * @see #getDatabase(String, DatabaseOptions)
     */
    public Database getDatabase(String apiEndpoint, String keyspace) {
        DatabaseOptions dbOptions = new DatabaseOptions(token, options).keyspace(keyspace);
        return new Database(apiEndpoint, dbOptions);
    }

    /**
     * Retrieves a database client configured to connect to the Data API using the specified API endpoint.
     * <p>
     * Uses default {@link DatabaseOptions} for configuration.
     * </p>
     *
     * @param apiEndpoint The URL of the Data API endpoint to connect to.
     * @return A {@link Database} client configured with default options for the specified endpoint.
     * @see #getDatabase(String, DatabaseOptions)
     */
    public Database getDatabase(String apiEndpoint) {
        return getDatabase(apiEndpoint, new DatabaseOptions(token, options));
    }

    /**
     * Retrieves a database client configured to connect to the Data API using a database identifier.
     * <p>
     * Automatically resolves the API endpoint based on the database ID and uses default {@link DatabaseOptions}.
     * </p>
     *
     * @param databaseId The unique identifier of the database.
     * @return A {@link Database} client configured with default options for the resolved endpoint.
     * @see #getDatabase(UUID, DatabaseOptions)
     */
    public Database getDatabase(UUID databaseId) {
        return getDatabase(lookupEndpoint(databaseId, null), new DatabaseOptions(token, options));
    }

    /**
     * Retrieves a database client configured to connect to the Data API using a database identifier,
     * with custom {@link DatabaseOptions}.
     * <p>
     * Automatically resolves the API endpoint based on the database ID.
     * </p>
     *
     * @param databaseId The unique identifier of the database.
     * @param dbOptions  The options to configure the database client.
     * @return A {@link Database} client configured for the resolved endpoint and custom options.
     * @see #getDatabase(UUID)
     */
    public Database getDatabase(UUID databaseId, DatabaseOptions dbOptions) {
        return getDatabase(databaseId, null, dbOptions);
    }

    /**
     * Retrieves a database client configured to connect to the Data API using a database identifier,
     * a specified region, and custom {@link DatabaseOptions}.
     *
     * @param databaseId The unique identifier of the database.
     * @param region     The region where the database is deployed (optional).
     * @param dbOptions  The options to configure the database client.
     * @return A {@link Database} client configured for the specified database ID, region, and options.
     * @see #getDatabase(UUID, DatabaseOptions)
     */
    public Database getDatabase(UUID databaseId, String region, DatabaseOptions dbOptions) {
        return getDatabase(lookupEndpoint(databaseId, region), dbOptions);
    }

    // --------------------------------------------------
    // ---       Getters                              ---
    // --------------------------------------------------

    /**
     * Gets token
     *
     * @return value of token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets options
     *
     * @return value of options
     */
    public DataAPIClientOptions getOptions() {
        return options;
    }

    /**
     * Compute the endpoint for the database based on its ID and region.
     * @param databaseId
     *      database ID (mandotory)
     * @param region
     *     region (optional), if not provided the default region of the database is used.
     * @return
     *      the endpoint for the database
     */
    private String lookupEndpoint(UUID databaseId, String region) {
        Assert.notNull(databaseId, "databaseId");
        String dbRegion = region;
        if (dbRegion == null) {
            dbRegion = getAdmin().getDatabaseInfo(databaseId).getRegion();
        }
        return new AstraApiEndpoint(databaseId, dbRegion, options.getAstraEnvironment()).getApiEndPoint();
    }

}
