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
import com.datastax.astra.internal.AstraApiEndpoint;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.net.http.HttpClient;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.DataAPIOptions.DataAPIDestination.ASTRA;
import static com.datastax.astra.client.admin.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * Serves as the primary entry point to the Data API client, offering streamlined access to the functionalities
 * provided by the Data API, whether deployed within Astra environments or on-premise DataStax Enterprise installations.
 * <p>
 * This client aims to simplify interactions with the Data API through a user-friendly, high-level API design. It
 * supports fluent API patterns, builder mechanisms for complex configurations, and employs idiomatic method naming
 * conventions to enhance readability and ease of use. The design philosophy of this client closely mirrors that of
 * the established MongoDB API, providing a familiar experience to developers accustomed to MongoDB's client interface.
 * </p>
 * <p>
 * Through this client, users can perform a wide range of operations, from basic data manipulation in databases and
 * collections to more advanced administrative tasks. Administrative capabilities, such as database creation and
 * namespace management, are available to users with the appropriate administrative privileges.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Initialize the client with default settings
 * DataAPIClient client = new DataAPIClient("yourAuthTokenHere");
 *
 * // Initialize the client with custom HTTP configuration
 * DataAPIClient clientWithCustomConfig = new DataAPIClient("yourAuthTokenHere", DataAPIOptions.builder()
 *                 .withHttpRequestTimeout(1000) // Request timeout in milliseconds
 *                 .withHttpConnectTimeout(10) // Connection timeout in milliseconds
 *                 .withHttpVersion(HttpClient.Version.HTTP_2) // HTTP protocol version
 *                 .withDestination("ASTRA") // Target destination, e.g., Astra
 *                 .build());
 * }
 * </pre>
 *
 * This documentation highlights the ease of starting with the DataAPIClient, whether opting for a quick setup with
 * default configurations or a more tailored approach via detailed HTTP client settings. The examples demonstrate
 * both the straightforward initialization process and the method to apply fine-grained configurations for developers'
 * specific needs.
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

    static {
        DataAPIOptions.builder()
                .withHttpRequestTimeout(1000)
                .withHttpConnectTimeout(10)
                .withHtpVersion(HttpClient.Version.HTTP_2)
                .withDestination(ASTRA)
                .build();
    }

    /**
     * Represents the advanced configuration settings for the client, encapsulating various HTTP-related properties.
     * This includes settings such as the HTTP version, request and connection timeouts, and the user agent among others.
     * <p>
     * Providing an instance of {@code DataAPIOptions} allows for fine-grained control over the HTTP behavior of the client,
     * enabling customization to fit specific requirements. If an instance is not explicitly provided, the client
     * defaults to a pre-configured set of options designed for general use.
     * </p>
     **
     * @see DataAPIOptions for more details on the available configuration parameters and their effects.
     */
    private final DataAPIOptions options;

    /**
     * Constructs a {@code DataAPIClient} instance using the specified authentication token. This constructor
     * initializes the client with default {@link DataAPIOptions} for its configuration.
     * <p>
     * The provided token is used for authenticating HTTP requests made by this client. It is essential for accessing
     * secured resources. If specific HTTP configurations are required (e.g., custom timeouts, HTTP version), use the
     * other constructor that accepts both a token and a {@link DataAPIOptions} instance.
     * </p>
     * <p>
     * This constructor is suitable for scenarios where default client settings are sufficient and no advanced
     * configuration is needed. It simplifies the initialization process for quick setup and use.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String myAuthToken = "AstraCS:...";
     * DataAPIClient client = new DataAPIClient(myAuthToken);
     * // Now the client is ready to make authenticated requests with default settings
     * }
     * </pre>
     *
     * @param token The authentication token to be used for HTTP requests. This token should follow the format expected
     *              by the server, typically starting with "AstraCS:.." for Astra environments.
     */
    public DataAPIClient(String token) {
        this(token, DataAPIOptions.builder().build());
    }

    /**
     * Constructs a {@code DataAPIClient} with specified authentication token and HTTP client configuration options.
     * <p>
     * This constructor allows for the explicit specification of both the authentication token and the advanced
     * HTTP configuration settings. The authentication token is essential for securing access to the API, while the
     * {@link DataAPIOptions} object provides granular control over the HTTP client's behavior, including timeouts,
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
     * String myAuthToken = "AstraCS:...";
     * DataAPIOptions myOptions = DataAPIOptions.builder()
     *      .withHttpRequestTimeout(1000)
     *      .withHttpConnectTimeout(500)
     *      .withHttpVersion(HttpClient.Version.HTTP_2)
     *      .build();
     *
     * DataAPIClient myClient = new DataAPIClient(myAuthToken, myOptions);
     * // The client is now ready to perform actions with custom configurations.
     * }
     * </pre>
     *
     * @param token The authentication token to be used for securing API access. This token should adhere to the
     *              format required by the API, typically starting with "AstraCS:.." for Astra environments.
     * @param options The {@link DataAPIOptions} specifying the detailed HTTP client configurations, offering
     *                customization over aspects such as timeouts and protocol versions.
     * @throws IllegalArgumentException if the token is empty or null, or if the options are null.
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
     * Retrieves an administration client specific to Astra deployments. This client is intended for performing
     * administrative tasks such as creating databases. It requires the use of a token with sufficient privileges.
     * <p>
     * To use this method effectively, the provided authentication token must be associated with a user having
     * elevated privileges, such as a Database Administrator or Organization Administrator. This ensures that
     * the client has the necessary permissions to execute administrative operations within the Astra environment.
     * </p>
     * <p>
     * The administration client provides a programmatic interface for managing various aspects of the Astra
     * deployment, enabling tasks such as database creation, user management, and configuration adjustments
     * without the need for direct interaction with the Astra UI.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DataAPIClient apiClient = new DataAPIClient("AstraCS:your_admin_token_here");
     * AstraDBAdmin adminClient = apiClient.getAdmin();
     * // Use adminClient to perform administrative operations, e.g., create a database
     * }
     * </pre>
     *
     * @return An instance of {@link AstraDBAdmin} configured with the current authentication token, ready for
     *         administrative operations.
     * @throws SecurityException if the current token does not have the necessary administrative privileges.
     */
    public AstraDBAdmin getAdmin() {
        return getAdmin(this.token);
    }


    /**
     * Retrieves an administration client capable of performing CRUD operations on databases, requiring a token with
     * advanced privileges. This method is designed for scenarios where administrative access is necessary beyond the
     * default token capabilities associated with the {@code DataAPIClient}.
     * <p>
     * The provided {@code superUserToken} should be granted sufficient privileges to perform administrative operations,
     * such as creating, updating, and deleting databases. This typically involves tokens associated with roles like
     * Database Administrator or Organization Administrator within the Astra environment.
     * </p>
     * <p>
     * Utilizing this method allows for direct access to the Astra database's administrative functionalities, enabling
     * comprehensive management capabilities through the returned {@link AstraDBAdmin} client. This includes but is not
     * limited to database creation, modification, and deletion.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String superUserToken = "AstraCS:super_user_token_here";
     * DataAPIClient apiClient = new DataAPIClient(superUserToken);
     * AstraDBAdmin adminClient = apiClient.getAdmin(superUserToken);
     * // Now you can use adminClient for administrative operations like creating a database
     * }
     * </pre>
     *
     * @param superUserToken A token with elevated privileges, enabling administrative actions within the Astra
     *        environment. This token must be authorized to perform operations such as creating and managing databases.
     * @return An instance of {@link AstraDBAdmin}, configured for administrative tasks with the provided  user token.
     * @throws SecurityException if the provided {@code superUserToken} lacks the necessary privileges for administrative operations.
     */
    public AstraDBAdmin getAdmin(String superUserToken) {
        return new AstraDBAdmin(superUserToken, getAstraEnvironment(), options);
    }

    /**
     * Find the Astra Environment from the destination provided in the initial Optional. It will help
     * shaping the Api endpoint to spawn sub database objects.
     *
     * @return
     *     astra environment if found
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
     * Access the Astra environment or throw an error. Some operation like getAdmin would anly work if the options
     * are set to Astra. If this is not the case a {@link IllegalArgumentException is thrown.}
     *
     * @return
     *      current astra environment
     * @throws IllegalArgumentException
     *      the destination was not set for Astra in initial options.
     */
    private AstraEnvironment getAstraEnvironment() {
        return findAstraEnvironment()
                .orElseThrow(() -> new IllegalArgumentException("'destination' should be ASTRA* to use the AstraDBAdmin"));
    }

    // --------------------------------------------------
    // ---       Access Database                      ---
    // --------------------------------------------------

    /**
     * Retrieves a client for a specific database, enabling interactions with the Data API. This method allows for
     * operations such as querying, updating, and managing data within the specified database and namespace.
     * <p>
     * The {@code databaseId} parameter is a unique identifier for the target database. This ID ensures that operations
     * performed through the returned client are executed against the correct database instance within the Astra
     * environment or an on-premise DataStax Enterprise setup.
     * </p>
     * <p>
     * The {@code namespace} parameter specifies the namespace (often synonymous with "keyspace" in Cassandra terminology)
     * within the database to which the client will have access. Namespaces are used to organize and isolate data within
     * the database, providing a layer of abstraction and security.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * UUID databaseId = UUID.fromString("your-database-uuid-here");
     * String namespace = "your_namespace_here";
     * DataAPIClient apiClient = new DataAPIClient("yourAuthTokenHere");
     * Database databaseClient = apiClient.getDatabase(databaseId, namespace);
     * // Use databaseClient to perform data operations within the specified namespace
     * }
     * </pre>
     *
     * @param databaseId The unique identifier of the database to interact with.
     * @param namespace The namespace within the specified database to which operations will be scoped.
     * @return A {@link Database} client configured to interact with the specified database and namespace, allowing for
     *         data manipulation and query operations.
     */
    public Database getDatabase(UUID databaseId, String namespace) {
        Assert.notNull(databaseId, "databaseId");
        Assert.hasLength(namespace, "namespace");
        return new Database(new AstraApiEndpoint(databaseId,
                getAdmin().getDatabaseInfo(databaseId).getRegion(),
                getAstraEnvironment()).getApiEndPoint(),
                this.token, namespace);
    }

    /**
     * Retrieves a client for a specific database, enabling interactions with the Data API. This method allows for
     * operations such as querying, updating, and managing data within the specified database and namespace.
     *
     * @param databaseId The unique identifier of the database to interact with.
     * @param namespace The namespace associated to this database
     * @param region The cloud provider region where the database is deployed.
     *
     * @return
     *      A {@link Database} client configured to interact with the specified database and namespace, allowing for
     *      data manipulation and query operations.
     */
    public Database getDatabase(UUID databaseId, String namespace, String region) {
        Assert.notNull(databaseId, "databaseId");
        Assert.hasLength(namespace, "namespace");
        Assert.hasLength(region, "namespace");
        return new Database(new AstraApiEndpoint(databaseId, region,
                getAstraEnvironment()).getApiEndPoint(),
                this.token, namespace);
    }

    /**
     * Retrieves a client for interacting with a specified database using its unique identifier. This client facilitates
     * direct communication with the Data API, enabling a range of operations such as querying, inserting, updating, and
     * deleting data within the database. This streamlined method provides access to the default namespace or keyspace.
     * <p>
     * The {@code databaseId} serves as a unique identifier for the database within the Astra environment or an on-premise
     * DataStax Enterprise setup, ensuring that all operations through the client are correctly routed to the intended
     * database instance.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * UUID databaseId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
     * DataAPIClient apiClient = new DataAPIClient("yourAuthTokenHere");
     * Database databaseClient = apiClient.getDatabase(databaseId);
     * // Perform data operations using the databaseClient
     * }
     * </pre>
     *
     * @param databaseId The unique identifier of the database for which to retrieve the client.
     * @return A {@link Database} client that provides the ability to perform operations on the specified database.
     */
    public Database getDatabase(UUID databaseId) {
        return getDatabase(databaseId, DEFAULT_NAMESPACE);
    }

    /**
     * Creates and returns a database client configured to interact with the Data API at the specified API endpoint
     * and within the given namespace. This method facilitates operations such as querying, inserting, updating, and
     * deleting data by directly communicating with the Data API, allowing for a wide range of data manipulation
     * tasks in a specified namespace.
     * <p>
     * The {@code apiEndpoint} parameter should be the URL of the API endpoint where the Data API is hosted. This
     * is typically a fully qualified URL that points to the Astra service or an on-premise DataStax Enterprise
     * deployment hosting the Data API.
     * </p>
     * <p>
     * The {@code namespace} parameter specifies the namespace (or keyspace in Cassandra terms) within the database
     * that the client will interact with. Namespaces help organize data within the database and provide a way to
     * isolate and manage access to data.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String apiEndpoint = "https://example-astra-data-api.com";
     * String namespace = "my_keyspace";
     * DataAPIClient apiClient = new DataAPIClient("yourAuthTokenHere");
     * Database databaseClient = apiClient.getDatabase(apiEndpoint, namespace);
     * // Now you can use the databaseClient to perform operations within "my_keyspace"
     * }
     * </pre>
     *
     * @param apiEndpoint The URL of the Data API endpoint. This specifies the location of the API the client will connect to.
     * @param namespace The namespace (or keyspace) within the database that the client will access and perform operations in.
     * @return A {@link Database} client configured for the specified API endpoint and namespace, enabling data manipulation
     *         and query operations within the targeted namespace.
     */
    public Database getDatabase(String apiEndpoint, String namespace) {
        return new Database(apiEndpoint, token, namespace, options);
    }

    /**
     * Retrieves a database client configured to interact with the Data API at the specified API endpoint. This method
     * enables direct communication with the Data API, facilitating a range of data manipulation operations such as querying,
     * inserting, updating, and deleting data. The client accesses the default namespace or keyspace for operations, unless
     * otherwise specified through additional configuration.
     * <p>
     * The {@code apiEndpoint} parameter should be the URL of the Data API endpoint you wish to connect to. This URL
     * points to the location where the Data API is hosted, which could be an Astra cloud service or an on-premise DataStax
     * Enterprise instance.
     * </p>
     * <p>
     * Utilizing this method simplifies the process of connecting to the Data API by focusing on essential configuration,
     * making it particularly useful for scenarios where detailed namespace management is handled separately or not required.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String apiEndpoint = "https://<database_id>-<database_region>.apps.astra.datastax.com";
     * DataAPIClient apiClient = new DataAPIClient("yourAuthTokenHere");
     * Database databaseClient = apiClient.getDatabase(apiEndpoint);
     * // The databaseClient is now ready to perform data operations at the specified API endpoint
     * }
     * </pre>
     *
     * @param apiEndpoint The URL of the Data API endpoint to connect to, specifying the API's location.
     * @return A {@link Database} client tailored for interaction with the Data API at the provided API endpoint,
     *         ready for executing data manipulation tasks.
     */
    public Database getDatabase(String apiEndpoint) {
        return getDatabase(apiEndpoint, DEFAULT_NAMESPACE);
    }
}
