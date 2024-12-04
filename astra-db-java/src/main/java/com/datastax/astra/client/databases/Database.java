package com.datastax.astra.client.databases;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStaxcc
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

import com.datastax.astra.client.admin.AdminOptions;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.collections.commands.options.DropCollectionOptions;
import com.datastax.astra.client.collections.commands.options.ListCollectionOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.definition.DatabaseInfo;
import com.datastax.astra.client.tables.commands.options.ListTablesOptions;
import com.datastax.astra.client.exceptions.InvalidConfigurationException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableOptions;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.datastax.astra.internal.reflection.EntityBeanDefinition.createTableCommand;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Represents a Data API database, providing the primary entry point for database-level operations
 * and interactions. This class enables Data Manipulation Language (DML) operations such as
 * creating and deleting collections, as well as obtaining {@link Collection} objects for further
 * operations on specific collections. It also provides access to operations for managing tables.
 *
 * <p>This class provides a synchronous interface, designed for straightforward and immediate
 * execution of database commands. It is intended for use in scenarios where blocking calls are
 * acceptable or desirable, such as in traditional server-side applications or command-line tools.</p>
 *
 * <p>Each {@code Database} instance is associated with an "API Endpoint," which defines the specific
 * region it connects to. This is particularly important for multi-region databases, where each
 * instance of {@code Database} ensures connectivity to a specific regional endpoint for optimal
 * performance and consistency.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Direct access to database-level operations, including collection management.</li>
 *   <li>Region-specific connectivity for multi-region database configurations.</li>
 *   <li>Built on {@link AbstractCommandRunner}, providing consistent command execution semantics.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Initialize the database object with endpoint and options
 * DataAPIClient client = new DataAPIClient("token");
 * client.getDatabase("https://<id>-<region>.apps.astra.datastax.com");
 *
 * // Perform database-level operations
 * database.createCollection("myCollection");
 * Collection collection = database.getCollection("myCollection");
 * collection.insert(new Document("field1", "value1"));
 * }
 * </pre>
 *
 * @see Collection
 * @see AbstractCommandRunner
 */
@Getter
public class Database extends AbstractCommandRunner<DatabaseOptions> {

    /**
     * This core endpoint could be used for admin operations.
     */
    private final String rootEndpoint;

    /**
     * Database information cached if (getInfo()) is called
     */
    private DatabaseInfo cachedDbInfo;

    /**
     * Initializes a {@link Database} instance with the specified API endpoint and connection options.
     * This constructor configures the database client to interact with the Data API at the provided
     * root endpoint, setting up necessary parameters and constructing the API endpoint based on
     * the deployment environment and options.
     *
     * <p>The API endpoint is automatically adjusted for Astra deployments (e.g., {@code ASTRA},
     * {@code ASTRA_TEST}, {@code ASTRA_DEV}), appending the required JSON API path if the root
     * endpoint ends with {@code ".com"}. For local or on-premise deployments, no adjustments are made
     * to the root endpoint.</p>
     *
     * <p>The constructed API endpoint includes:</p>
     * <ul>
     *   <li>The specified {@code apiVersion}, retrieved from {@link com.datastax.astra.client.core.options.DataAPIClientOptions}.</li>
     *   <li>The {@code keyspace}, defined in the provided {@link DatabaseOptions}.</li>
     * </ul>
     *
     * @param rootEndpoint
     *      The root API endpoint for connecting to the database. This is the base URL that determines
     *      the target deployment environment (e.g., Astra or local).
     * @param options
     *      The {@link DatabaseOptions} containing all attributes required to connect to the database,
     *      including authentication details, keyspace configuration, and client options.
     */
    public Database(String rootEndpoint, DatabaseOptions options) {
        super(rootEndpoint, options);
        this.rootEndpoint = rootEndpoint;
        StringBuilder dbApiEndPointBuilder = new StringBuilder(rootEndpoint);
        switch(options.getDataAPIClientOptions().getDestination()) {
            case ASTRA:
            case ASTRA_TEST:
            case ASTRA_DEV:
                if (rootEndpoint.endsWith(".com")) {
                    dbApiEndPointBuilder.append("/api/json");
                }
                break;
            default:
                // left blank as local deployments does not require any change
                break;
        }
        this.apiEndpoint = dbApiEndPointBuilder
                .append("/")
                .append(options.getDataAPIClientOptions().getApiVersion())
                .append("/")
                .append(options.getKeyspace())
                .toString();
    }

    // ------------------------------------------
    // ----      Core Features               ----
    // ------------------------------------------

    /**
     * Retrieves the name of the currently selected keyspace.
     *
     * @return The name of the keyspace currently in use by this {@code Database} instance.
     */
    public String getKeyspace() {
        return options.getKeyspace();
    }

    /**
     * Retrieves the region of the database if it is deployed in Astra. This method ensures that
     * the database is an Astra deployment before returning the region. If the database is not deployed
     * in Astra, an assertion error is raised.
     *
     * @return The region where the Astra database is deployed.
     * @throws IllegalStateException if the database is not deployed in Astra.
     */
    public String getRegion() {
        assertIsAstra();
        return AstraApiEndpoint.parse(getApiEndpoint()).getDatabaseRegion();
    }

    /**
     * Retrieves the unique database identifier (UUID) of the database if it is deployed in Astra.
     * This method ensures that the database is an Astra deployment before returning the identifier.
     * If the database is not deployed in Astra, an assertion error is raised.
     *
     * @return The unique identifier (UUID) of the Astra database.
     * @throws IllegalStateException if the database is not deployed in Astra.
     */
    public UUID getId() {
        assertIsAstra();
        return AstraApiEndpoint.parse(getApiEndpoint()).getDatabaseId();
    }

    /**
     * Retrieves information about the current database, including metadata and configuration details.
     *
     * <p>This method interacts with the devops API to fetch database information. To optimize
     * performance, the database information is cached after the first retrieval. Subsequent calls to this
     * method return the cached {@link DatabaseInfo} object unless the cache is invalidated externally.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DatabaseInfo info = database.getInfo();
     * System.out.println("Database Name: " + info.getName());
     * System.out.println("Database Version: " + info.getVersion());
     * }
     * </pre>
     *
     * @return A {@link DatabaseInfo} object containing details about the current database.
     * @throws IllegalStateException if the database information cannot be retrieved or if the
     *         database is not properly configured for administration operations.
     */
    public DatabaseInfo getInfo() {
        if (cachedDbInfo == null) {
            cachedDbInfo = getAdmin().getDatabaseInfo(getId());
        }
        return cachedDbInfo;
    }

    /**
     * Retrieves the name of the current database.
     *
     * <p>This method provides a convenient way to access the database name from the {@link DatabaseInfo}
     * object returned by {@link #getInfo()}. It encapsulates the process of fetching and extracting
     * the database name.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String dbName = database.getName();
     * System.out.println("Database Name: " + dbName);
     * }
     * </pre>
     *
     * @return The name of the current database as a {@link String}.
     * @throws IllegalStateException if the database information cannot be retrieved or is unavailable.
     */
    public String getName() {
        return getInfo().getName();
    }

    /**
     * Sets the active keyspace for the database.
     * This method allows switching the current keyspace context used for database operations.
     *
     * @param keyspace
     *      The name of the keyspace to set as the current keyspace.
     *      This must not be null or empty.
     * @return
     *      The database instance with the specified keyspace set as active,
     *      allowing for method chaining.
     * @throws IllegalArgumentException
     *      If the provided keyspace is null or empty.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Database database = new Database();
     * database.useKeyspace("my_keyspace");
     * }
     * </pre>
     */
    public Database useKeyspace(String keyspace) {
        Assert.hasLength(keyspace, "keyspace");
        this.options.keyspace(keyspace);
        return this;
    }

    // ------------------------------------------
    // ----   Astra  Admin                   ----
    // ------------------------------------------

    /**
     * Retrieves an administration client for Astra deployments using detailed administrative options.
     * <p>
     * This method provides fine-grained control over the client configuration by allowing explicit
     * specification of both the token and additional options.
     * </p>
     *
     * @param adminOptions The {@link AdminOptions} object containing authentication and configuration details.
     * @return An {@link AstraDBAdmin} instance configured with the provided administrative options.
     * @throws IllegalStateException if the database is not deployed in Astra.
     */
    public AstraDBAdmin getAdmin(AdminOptions adminOptions) {
        assertIsAstra();
        return new AstraDBAdmin(adminOptions);
    }

    /**
     * Retrieves an administration client specifically for Astra deployments using the default authentication token.
     * <p>
     * This client allows execution of administrative tasks such as creating databases and managing Astra configurations.
     * </p>
     *
     * @return An {@link AstraDBAdmin} instance configured with the default token for administrative operations.
     * @throws IllegalStateException if the database is not deployed in Astra.
     */
    public AstraDBAdmin getAdmin() {
        return getAdmin(options.getToken());
    }

    /**
     * Retrieves an administration client specifically for Astra deployments using a provided super-user token.
     * <p>
     * This method allows overriding the default token with a custom super-user token for enhanced privileges.
     * </p>
     *
     * @param superUserToken A token with elevated privileges for administrative operations.
     * @return An {@link AstraDBAdmin} instance configured with the provided token.
     * @throws IllegalStateException if the database is not deployed in Astra.
     */
    public AstraDBAdmin getAdmin(String superUserToken) {
        return getAdmin(new AdminOptions(superUserToken, options.getDataAPIClientOptions()));
    }

    // ------------------------------------------
    // ----   Database  Admin                 ---
    // ------------------------------------------

    /**
     * Retrieves a database administration client using detailed administrative options.
     * <p>
     * Depending on the deployment type (Astra or non-Astra), this method returns an appropriate implementation of
     * {@link DatabaseAdmin}, either {@link AstraDBDatabaseAdmin} or {@link DataAPIDatabaseAdmin}. The provided
     * {@link AdminOptions} object determines the authentication and configuration used for the client.
     * </p>
     *
     * <p>Key behaviors:</p>
     * <ul>
     *   <li>If no {@code adminOptions} are provided, a default configuration is derived from the current options.</li>
     *   <li>If the deployment is Astra, an {@link AstraDBDatabaseAdmin} instance is returned.</li>
     *   <li>For non-Astra deployments, a {@link DataAPIDatabaseAdmin} instance is returned.</li>
     * </ul>
     *
     * @param adminOptions The {@link AdminOptions} object containing authentication and configuration details.
     * @return A {@link DatabaseAdmin} instance tailored for the current deployment type and configured with the provided options.
     */
    public DatabaseAdmin getDatabaseAdmin(AdminOptions adminOptions) {
        if (adminOptions == null) {
            adminOptions = new AdminOptions(options.getToken(), options.getDataAPIClientOptions().clone());
        } else if (adminOptions.getDataAPIClientOptions() == null) {
            adminOptions.dataAPIClientOptions(options.getDataAPIClientOptions().clone());
        }else if (adminOptions.getToken() == null) {
            adminOptions.token(options.getToken());
        }
        // Pick the right admin client
        if (options.getDataAPIClientOptions().isAstra()) {
            return new AstraDBDatabaseAdmin(this, adminOptions);
        }
        return new DataAPIDatabaseAdmin(this, adminOptions);
    }

    /**
     * Retrieves a database administration client using the default authentication token.
     * <p>
     * The client enables management of database-level configurations, such as keyspaces, collections,
     * and other database settings.
     * </p>
     *
     * @return A {@link DatabaseAdmin} instance configured with the default token for database-level operations.
     */
    public DatabaseAdmin getDatabaseAdmin() {
        return getDatabaseAdmin(options.getToken());
    }

    /**
     * Retrieves a database administration client using a provided super-user token.
     * <p>
     * This method allows overriding the default token with a custom super-user token for privileged database
     * management operations.
     * </p>
     *
     * @param superUserToken A token with elevated privileges for database administration tasks.
     * @return A {@link DatabaseAdmin} instance configured with the provided token.
     */
    public DatabaseAdmin getDatabaseAdmin(String superUserToken) {
        return getDatabaseAdmin(new AdminOptions(superUserToken, options.getDataAPIClientOptions()));
    }

    // ------------------------------------------
    // ----     List Collections             ----
    // ------------------------------------------

    /**
     * Retrieves the names of all the collections present in this database.
     * <p>
     * This method provides a list of collection names, allowing developers to explore or iterate over the
     * available collections in the database. It is particularly useful for dynamic scenarios where the
     * collections within a database might not be predetermined or when you need to inspect the database's
     * current state programmatically.
     * </p>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * Database database = new DataAPIClient("token").getDatabase("endpoint);
     * List<String> collectionNames = database.listCollectionNames();
     * collectionNames.forEach(System.out::println);
     * }
     * </pre>
     *
     * @return A {@link List} containing the names of all collections in this database.
     * @throws com.datastax.astra.client.exceptions.DataAPIException if an error occurs while retrieving the collection names.
     */
    public List<String> listCollectionNames() {
        return listCollectionNames(null);
    }

    /**
     * Retrieves the names of all the collections present in this database, with the ability to customize
     * the listing behavior using the specified {@link ListCollectionOptions}.
     * <p>
     * This method provides a list of collection names, allowing developers to explore or iterate over
     * the collections in the database. The behavior of this operation can be tailored by providing
     * {@link ListCollectionOptions}, enabling filtering or additional configuration as needed.
     * </p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>{@code listCollectionOptions} - The options to customize the collection listing operation,
     *       such as filtering criteria or additional query parameters.</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * // Create list collection options
     * ListCollectionOptions options = new ListCollectionOptions()
     *    .timeout(Duration.ofMillis(1000));
     *
     * // Retrieve collection names based on options
     * Database database = new DataAPIClient("token").getDatabase("endpoint);
     * List<String> collectionNames = database.listCollectionNames(options);
     *
     * // Print the collection names
     * collectionNames.forEach(System.out::println);
     * }
     * </pre>
     *
     * @param listCollectionOptions The {@link ListCollectionOptions} to customize the collection listing behavior.
     * @return A {@link List} containing the names of all collections in this database, filtered or modified
     *         according to the provided options.
     */
    public List<String> listCollectionNames(ListCollectionOptions listCollectionOptions) {
        return runCommand(Command.create("findCollections"), listCollectionOptions)
                .getStatusKeyAsStringStream("collections")
                .toList();
    }

    /**
     * Retrieves all collections in this database along with their definitions.
     * <p>
     * This method returns a list of {@link CollectionDescriptor} objects, providing detailed metadata
     * about each collection, such as its name, schema, or other relevant attributes. It acts as a
     * convenient entry point for obtaining all collection definitions without any filtering or additional options.
     * </p>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * Database database = new DataAPIClient("token").getDatabase("endpoint);
     * List<CollectionDescriptor> collections = database.listCollections();
     * }
     * </pre>
     *
     * @return A {@link List} of {@link CollectionDescriptor} objects representing all collections in this database.
     */
    public List<CollectionDescriptor> listCollections() {
        return listCollections(null);
    }

    /**
     * Retrieves all collections in this database along with their definitions, customized by the
     * specified {@link ListCollectionOptions}.
     * <p>
     * This method allows for more fine-grained control over the collection retrieval process, enabling
     * options such as filtering, limiting the number of results, or specifying additional query parameters.
     * The returned list includes {@link CollectionDescriptor} objects, which provide detailed metadata
     * for each collection that matches the provided options.
     * </p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>{@code listCollectionOptions} - The {@link ListCollectionOptions} to customize the listing behavior,
     *       such as filtering criteria or additional query parameters. If {@code null}, all collections are returned.</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * // Create options for listing collections with a specific prefix
     * ListCollectionOptions options = new ListCollectionOptions()
     *    .timeout(Duration.ofMillis(1000));
     *
     * // Retrieve matching collections
     * Database database = new DataAPIClient("token").getDatabase("endpoint);
     * List<CollectionDescriptor> collections = database.listCollections(options);
     * }
     * </pre>
     *
     * @param listCollectionOptions The {@link ListCollectionOptions} to customize the collection retrieval process.
     *                              If {@code null}, no filtering or additional options are applied.
     * @return A {@link List} of {@link CollectionDescriptor} objects representing the collections that match the criteria.
     */
    public List<CollectionDescriptor> listCollections(ListCollectionOptions listCollectionOptions) {
        Command findCollections = Command.create("findCollections")
                .withOptions(new Document().append("explain", true));
        return runCommand(findCollections, listCollectionOptions)
                .getStatusKeyAsList("collections", CollectionDescriptor.class);
    }

    /**
     * Checks if a specified collection exists in this database.
     * <p>
     * This method evaluates whether a collection with the given name is present in the database.
     * It is useful for verifying the existence of a collection before performing operations such
     * as querying, inserting, or updating data.
     * </p>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * Database database = new DataAPIClient("token").getDatabase("endpoint");
     * boolean exists = database.collectionExists("my_collection");
     * if (exists) {
     *     System.out.println("Collection exists!");
     * } else {
     *     System.out.println("Collection does not exist.");
     * }
     * }
     * </pre>
     *
     * @param collectionName The name of the collection to check.
     * @return {@code true} if the collection exists, {@code false} otherwise.
     * @throws IllegalArgumentException if the collection name is {@code null} or empty.
     */
    public boolean collectionExists(String collectionName) {
        Assert.hasLength(collectionName, "collectionName");
        return listCollectionNames().contains(collectionName);
    }

    // ------------------------------------------
    // ----        Get Collection            ----
    // ------------------------------------------

    /**
     * Retrieves a {@link Collection} object for the specified collection name.
     * <p>
     * This method provides a convenient way to obtain a {@link Collection} instance for a specific
     * collection in the database. The returned object allows for further operations on the collection,
     * such as querying, inserting, or updating documents.
     * </p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>{@code collectionName} - The name of the collection to retrieve. This must not be null or empty.</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * Database database = new DataAPIClient("token").getDatabase("endpoint");
     * Collection collection = database.getCollection("my_collection");
     * }
     * </pre>
     *
     * @param collectionName The name of the collection to retrieve.
     * @return A {@link Collection} object representing the specified collection.
     * @throws IllegalArgumentException if the collection name is {@code null} or empty.
     */
    public Collection<Document> getCollection(String collectionName) {
        return getCollection(collectionName, Document.class);
    }

    /**
     * Retrieves a {@link Collection} object for the specified collection name, with the ability to
     * customize the collection behavior using the specified {@link CollectionOptions}.
     * <p>
     * This method provides a way to obtain a {@link Collection} instance for a specific collection in
     * the database, with additional options for configuring the collection's behavior. The returned object
     * allows for further operations on the collection, such as querying, inserting, or updating documents.
     * </p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>{@code collectionName} - The name of the collection to retrieve. This must not be null or empty.</li>
     *   <li>{@code collectionOptions} - The {@link CollectionOptions} to customize the collection behavior,
     *       such as setting a custom serializer or specifying additional options. If {@code null}, default options are used.</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * // Create custom collection options
     * CollectionOptions options = new CollectionOptions()
     *    .serializer(new MyCustomSerializer());
     *
     * // Retrieve the collection with custom options
     * Database database = new DataAPIClient("token").getDatabase("endpoint");
     * Collection collection = database.getCollection("my_collection", options);
     * }
     * </pre>
     *
     * @param collectionName The name of the collection to retrieve.
     * @return A {@link Collection} object representing the specified collection, configured with the provided options.
     * @throws IllegalArgumentException if the collection name is {@code null} or empty.
     */
    public <T> Collection<T> getCollection(String collectionName, Class<T> documentClass) {
        return getCollection(collectionName, defaultCollectionOptions(), documentClass);
    }

    /**
     * Retrieves a {@link Collection} object for the specified collection name with the ability to specify custom options.
     * <p>
     * This method provides a flexible way to obtain a {@link Collection} instance by allowing
     * the caller to specify {@link CollectionOptions} to customize the behavior of the collection.
     * </p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>{@code collectionName} - The name of the collection to retrieve. This must not be null or empty.</li>
     *   <li>{@code collectionOptions} - A {@link CollectionOptions} object that specifies custom
     *       behaviors for the collection. If {@code null}, default options will be used.</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     *
     * CollectionOptions options = new CollectionOptions()
     *  .timeout(Duration.ofMillis(1000))
     *  .dataAPIClientOptions(new DataAPIClientOptions())
     *  .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"));
     *
     * Database database = new DataAPIClient("token").getDatabase("endpoint");
     * Collection collection = database.getCollection("my_collection", options);
     * }
     * </pre>
     *
     * @param collectionName The name of the collection to retrieve.
     * @param collectionOptions The {@link CollectionOptions} to customize the collection behavior.
     * @return A {@link Collection} object representing the specified collection.
     * @throws IllegalArgumentException if {@code collectionName} is {@code null} or empty.
     */
    public Collection<Document> getCollection(String collectionName,  CollectionOptions collectionOptions) {
        return getCollection(collectionName, collectionOptions, Document.class);
    }

    /**
     * Retrieves a {@link Collection} object for the specified collection name with custom options and document type.
     * <p>
     * This method provides the most flexible way to obtain a {@link Collection} instance, allowing
     * clients to specify custom options and the type of documents in the collection.
     * </p>
     *
     * <p><strong>Parameters:</strong></p>
     * <ul>
     *   <li>{@code collectionName} - The name of the collection to retrieve. This must not be null or empty.</li>
     *   <li>{@code options} - The {@link CollectionOptions} to customize the collection behavior. Must not be null.</li>
     *   <li>{@code documentClass} - The {@link Class} type of the documents stored in the collection.
     *       This enables type safety when working with the collection's documents. Must not be null.</li>
     * </ul>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>
     * {@code
     * CollectionOptions options = new CollectionOptions()
     *  .timeout(Duration.ofMillis(1000))
     *  .dataAPIClientOptions(new DataAPIClientOptions())
     *  .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"));
     * Collection<MyDocument> collection = database.getCollection("my_collection", options, MyDocument.class);
     * }
     * </pre>
     *
     * @param collectionName The name of the collection to retrieve.
     * @param options The {@link CollectionOptions} for customizing the collection behavior.
     * @param documentClass The class type of the documents in the collection.
     * @return A {@link Collection} object representing the specified collection.
     * @throws IllegalArgumentException if {@code collectionName}, {@code options}, or {@code documentClass} is {@code null}.
     */
    public <T> Collection<T> getCollection(String collectionName, CollectionOptions options,  Class<T> documentClass) {
        hasLength(collectionName, "collectionName");
        notNull(options, "options");
        notNull(documentClass, "documentClass");
        return new Collection<>(this, collectionName, options, documentClass);
    }

    // ------------------------------------------
    // ----      Create Collection           ----
    // ------------------------------------------

    /**
     * Creates a new collection in the database.
     *
     * @param <T>                 The type of the documents stored in the collection.
     * @param collectionName      The name of the collection to be created.
     * @param collectionDefinition An optional {@link CollectionDefinition} object defining the schema and other properties of the collection.
     * @param collectionOptions   The {@link CollectionOptions} that include token, client configuration, and serializer for the collection.
     * @param createCollectionOptions Additional options for creating the collection, such as timeouts or retry policies.
     * @param documentClass       The class of the documents stored in the collection.
     * @return The created collection as a {@link Collection} of the specified document type.
     *
     * @throws IllegalArgumentException If any required argument is null or invalid.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection<MyDocument> collection = db.createCollection(
     *     "myCollection",
     *     new CollectionDefinition(),
     *     new CollectionOptions(token, dataAPIClientOptions),
     *     new CreateCollectionOptions(),
     *     MyDocument.class
     * );
     * }
     * </pre>
     */
    public <T> Collection<T> createCollection(String collectionName,
                                              CollectionDefinition collectionDefinition,
                                              Class<T> documentClass,
                                              CreateCollectionOptions createCollectionOptions,
                                              CollectionOptions collectionOptions) {

        hasLength(collectionName, "collectionName");
        notNull(collectionOptions, "collectionOptions");
        notNull(documentClass, "documentClass");
        notNull(collectionOptions.getSerializer(), "serializer");

        Command createCollectionCommand = Command
                .create("createCollection")
                .append("name", collectionName);
        if (collectionDefinition != null) {
            createCollectionCommand.withOptions(collectionOptions
                    .getSerializer()
                    .convertValue(collectionDefinition, Document.class));
        }
        runCommand(createCollectionCommand, createCollectionOptions);
        return getCollection(collectionName, collectionOptions, documentClass);
    }

    /**
     * Creates a new collection with the default document type {@link Document}.
     *
     * @param collectionName The name of the collection to be created.
     * @return The created collection as a {@link Collection} of {@link Document}.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection<Document> collection = db.createCollection("myDefaultCollection");
     * }
     * </pre>
     */
    public Collection<Document> createCollection(String collectionName) {
        return createCollection(collectionName, Document.class);
    }

    /**
     * Creates a new collection with the specified document class.
     *
     * @param <T>            The type of the documents stored in the collection.
     * @param collectionName The name of the collection to be created.
     * @param documentClass  The class of the documents stored in the collection.
     * @return The created collection as a {@link Collection} of the specified document type.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection<MyDocument> collection = db.createCollection("myTypedCollection", MyDocument.class);
     * }
     * </pre>
     */
    public <T> Collection<T> createCollection(String collectionName, Class<T> documentClass) {
        return createCollection(collectionName,
                // no CollectionDefinition as simple
                null,
                documentClass,
                // No create collection options
                null,
                defaultCollectionOptions()
                );
    }

    /**
     * Creates a new collection with a specified definition and the default document type {@link Document}.
     *
     * @param name The name of the collection to be created.
     * @param def  The {@link CollectionDefinition} specifying the schema and other properties of the collection.
     * @return The created collection as a {@link Collection} of {@link Document}.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection<Document> collection = createCollection("myDefinedCollection", new CollectionDefinition());
     * }
     * </pre>
     */
    public Collection<Document> createCollection(String name, CollectionDefinition def) {
        return createCollection(name, def, Document.class);
    }

    /**
     * Creates a new collection with a specified definition and the specified document class.
     *
     * @param <T>        The type of the documents stored in the collection.
     * @param name       The name of the collection to be created.
     * @param def        The {@link CollectionDefinition} specifying the schema and other properties of the collection.
     * @param documentClass The class of the documents stored in the collection.
     * @return The created collection as a {@link Collection} of the specified document type.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection<MyDocument> collection = createCollection("myDefinedCollection",
     *  new CollectionDefinition(), MyDocument.class);
     * }
     * </pre>
     */
    public <T>  Collection<T> createCollection(String name, CollectionDefinition def, Class<T> documentClass) {
        return createCollection(name,
                def,
                documentClass,
                null,
                defaultCollectionOptions());
    }

    /**
     * Creates a new collection with a specified definition, options, and the default document type {@link Document}.
     *
     * @param collectionName       The name of the collection to be created.
     * @param collectionDefinition The {@link CollectionDefinition} specifying the schema and other properties of the collection.
     * @param collectionOptions    The {@link CollectionOptions} that include token, client configuration, and serializer for the collection.
     * @param createCollectionOptions Additional options for creating the collection, such as timeouts or retry policies.
     * @return The created collection as a {@link Collection} of {@link Document}.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection<Document> collection = createCollection(
     *     "myComplexCollection",
     *     new CollectionDefinition(),
     *     new CollectionOptions(token, dataAPIClientOptions),
     *     new CreateCollectionOptions()
     * );
     * }
     * </pre>
     */
    public Collection<Document> createCollection(String collectionName,
        CollectionDefinition collectionDefinition,
        CreateCollectionOptions createCollectionOptions,
        CollectionOptions collectionOptions
        ) {
        return createCollection(
                collectionName,
                collectionDefinition,
                Document.class,
                createCollectionOptions,
                collectionOptions);
    }

    // ------------------------------------------
    // ----      Drop Collection             ----
    // ------------------------------------------

    /**
     * Deletes a collection from the database.
     *
     * @param collectionName       The name of the collection to be deleted. Must not be null or empty.
     * @param dropCollectionOptions Additional options for dropping the collection, such as timeout or retry policies.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * db.dropCollection("myCollection", new DropCollectionOptions().timeout(Duration.ofMillis(1000)));
     * }
     * </pre>
     */
    public void dropCollection(String collectionName, DropCollectionOptions dropCollectionOptions) {
        runCommand(Command
                .create("deleteCollection")
                .append("name", collectionName), dropCollectionOptions);
    }

    /**
     * Deletes a collection from the database with default options.
     *
     * @param collectionName The name of the collection to be deleted. Must not be null or empty.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * dropCollection("myCollection");
     * }
     * </pre>
     */
    public void dropCollection(String collectionName) {
        dropCollection(collectionName, null);
    }

    /**
     * Initialize a TableOption from the current database options.
     *
     * @return
     *      default table options
     */
    private CollectionOptions defaultCollectionOptions() {
        return new CollectionOptions(this.options.getToken(),
                this.options.getDataAPIClientOptions())
                .keyspace(getKeyspace());
    }

    // ------------------------------------------
    // -------    List tables               -----
    // ------------------------------------------

    /**
     * Retrieves the names of all tables in the database with default options.
     *
     * @return A list of all table names in the database.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<String> tableNames = listTableNames();
     * }
     * </pre>
     */
    public List<String> listTableNames() {
        return listTableNames(null);
    }

    /**
     * Retrieves the names of all tables in the database.
     *
     * @param listTablesOptions Options for filtering or configuring the table listing operation.
     * @return A list of all table names in the database.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * ListTablesOptions options = new ListTablesOptions();
     * List<String> tableNames = listTableNames(options);
     * }
     * </pre>
     */
    public List<String> listTableNames(ListTablesOptions listTablesOptions) {
        // Keyspace is part of the database, a new temporary database object is required.
        if (listTablesOptions != null && Utils.hasLength(listTablesOptions.getKeyspace())) {
            String otherKeyspace = listTablesOptions.getKeyspace();
            listTablesOptions.keyspace(null);
            return new Database(
                    this.rootEndpoint,
                    this.options.clone().keyspace(otherKeyspace))
                    .listTableNames(listTablesOptions);
        }
        return runCommand(Command.create("listTables"), listTablesOptions)
                .getStatusKeyAsStringStream("tables")
                .toList();
    }

    /**
     * Retrieves the details of all tables in the database with default options.
     *
     * @return A list of {@link TableDescriptor} objects representing all tables in the database.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<TableDescriptor> tables = listTables();
     * }
     * </pre>
     */
    public List<TableDescriptor> listTables() {
        return listTables(null);
    }

    /**
     * Retrieves the details of all tables in the database.
     *
     * @param listTableOptions Options for filtering or configuring the table listing operation.
     * @return A list of {@link TableDescriptor} objects representing all tables in the database.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * ListTablesOptions options = new ListTablesOptions();
     * List<TableDescriptor> tables = listTables(options);
     * }
     * </pre>
     */
    public List<TableDescriptor> listTables(ListTablesOptions listTableOptions) {
        // Keyspace is part of the database, a new temporary database object is required.
        if (listTableOptions != null && Utils.hasLength(listTableOptions.getKeyspace())) {
            String otherKeyspace = listTableOptions.getKeyspace();
            listTableOptions.keyspace(null);
            return new Database(
                    this.rootEndpoint,
                    this.options.clone().keyspace(otherKeyspace))
                    .listTables(listTableOptions);
        }
        Command findTables = Command
                .create("listTables")
                .withOptions(new Document().append("explain", true));
        return runCommand(findTables, listTableOptions)
                .getStatusKeyAsList("tables", TableDescriptor.class);
    }

    /**
     * Checks if a table exists in the database by its name.
     *
     * @param tableName The name of the table to check. Must not be null or empty.
     * @return {@code true} if the table exists, {@code false} otherwise.
     *
     * @throws IllegalArgumentException if {@code tableName} is null or empty.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * boolean exists = tableExists("myTable");
     * if (exists) {
     *     System.out.println("The table exists.");
     * } else {
     *     System.out.println("The table does not exist.");
     * }
     * }
     * </pre>
     */
    public boolean tableExists(String tableName) {
        Assert.hasLength(tableName, "tableName");
        return listTableNames().contains(tableName);
    }

    // ------------------------------------------
    // ----      Get Table                   ----
    // ------------------------------------------

    /**
     * Retrieves a table representation for the specified table name, table options, and row class type.
     * This is the primary method to obtain a typed table instance.
     *
     * @param <T>         the type of the row objects
     * @param tableName   the name of the table (must not be null or empty)
     * @param tableOptions options used to configure the table (e.g., connection options)
     * @param rowClass    the class representing the type of rows in the table (must not be null)
     * @return a {@code Table<T>} instance for the specified configuration
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Table<MyRowType> table = db.getTable("my_table", new TableOptions(...), MyRowType.class);
     * }
     * </pre>
     */
    public <T> Table<T> getTable(String tableName, Class<T> rowClass, TableOptions tableOptions) {
        hasLength(tableName, "tableName");
        notNull(rowClass, "rowClass");
        Database db2 = new Database(this.rootEndpoint, this.options.clone());
        return new Table<>(db2, tableName, tableOptions, rowClass);
    }

    /**
     * Retrieves a table representation for the specified table name with default {@code TableOptions}.
     *
     * @param tableName the name of the table (must not be null or empty)
     * @return a {@code Table<Row>} instance representing a generic table with {@code Row} type rows
     * @throws IllegalArgumentException if {@code tableName} is null or empty
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Table<Row> table = db.getTable("my_table");
     * }
     * </pre>
     */
    public Table<Row> getTable(String tableName) {
        return getTable(tableName, Row.class);
    }

    /**
     * Retrieves a table representation for the specified table name and row class type with default {@code TableOptions}.
     *
     * @param <T>       the type of the row objects
     * @param tableName the name of the table (must not be null or empty)
     * @param rowClass  the class representing the type of rows in the table (must not be null)
     * @return a {@code Table<T>} instance for the specified configuration
     * @throws IllegalArgumentException if {@code tableName} is null or empty
     * @throws NullPointerException     if {@code rowClass} is null
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Table<MyRowType> table = myFramework.getTable("my_table", MyRowType.class);
     * }
     * </pre>
     */
    public <T> Table<T> getTable(String tableName, Class<T> rowClass) {
        return getTable(tableName, rowClass, defaultTableOptions());
    }

    /**
     * Retrieves a table representation for the specified table name and {@code TableOptions}, defaulting to {@code Row} type rows.
     *
     * @param tableName    the name of the table (must not be null or empty)
     * @param tableOptions options used to configure the table (e.g., connection options)
     * @return a {@code Table<Row>} instance representing a generic table with {@code Row} type rows
     * @throws IllegalArgumentException if {@code tableName} is null or empty
     * @throws NullPointerException     if {@code tableOptions} is null
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Table<Row> table = myFramework.getTable("my_table", new TableOptions(...));
     * }
     * </pre>
     */
    public Table<Row> getTable(String tableName, TableOptions tableOptions) {
        return getTable(tableName,  Row.class, tableOptions);
    }

    /**
     * Retrieves a table representation for a row class annotated with {@link EntityTable}.
     * The table name is inferred from the {@code value} attribute of the {@code EntityTable} annotation.
     *
     * @param <T>      the type of the row objects
     * @param rowClass the class representing the type of rows in the table (must be annotated with {@link EntityTable})
     * @return a {@code Table<T>} instance for the inferred table name and row type
     * @throws InvalidConfigurationException if the provided class is not annotated with {@link EntityTable}
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * @EntityTable("my_table")
     * public class MyRowType { ... }
     *
     * Table<MyRowType> table = myFramework.getTable(MyRowType.class);
     * }
     * </pre>
     */
    public <T> Table<T> getTable(Class<T> rowClass) {
        EntityTable ann = rowClass.getAnnotation(EntityTable.class);
        if (ann == null) {
            InvalidConfigurationException.throwErrorMissingAnnotation(
                    EntityTable.class.getSimpleName(),
                    rowClass.getName(),
                    "getTable(rowClass)");
        }
        return getTable(ann.value(), rowClass);
    }

    // -------------------------------------
    // ----      Create Table           ----
    // -------------------------------------

    /**
     * Creates a table in the system with the specified parameters.
     *
     * @param <T>             the type of the row objects that the table will hold
     * @param tableName       the name of the table to be created; must not be null or empty
     * @param tableDefinition the schema definition of the table; must not be null
     * @param createTableOptions additional options for creating the table; optional, can be null
     * @param rowClass        the class representing the row type; must not be null
     * @return the created table object
     * @throws IllegalArgumentException if any mandatory argument is null or invalid
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * TableDefinition tableDefinition = new TableDefinition()
     *  .addColumnText("match_id")
     *  .addColumnInt("round")
     *  .addColumnVector("m_vector", new ColumnDefinitionVector().dimension(3).metric(COSINE))
     *  .addColumn("score", ColumnTypes.INT)
     *  .addColumn("when", ColumnTypes.TIMESTAMP)
     *  .addColumn("winner", ColumnTypes.TEXT)
     *  .addColumnSet("fighters", ColumnTypes.UUID)
     *  .addPartitionBy("match_id")
     *  .addPartitionSort(Sort.ascending("round"));
     *
     * // Optional
     * CreateTableOptions createTableOptions =
     *      new CreateTableOptions().timeout(Duration.ofMillis(1000));
     *
     * Table<Row> tableSimple2 = db.createTable("TABLE_SIMPLE", tableDefinition,
     *  Row.class, createTableOptions);
     * }
     * </pre>
     */
    public <T> Table<T> createTable(String tableName,
                                    TableDefinition tableDefinition,
                                    Class<T> rowClass,
                                    CreateTableOptions createTableOptions) {
        hasLength(tableName, "tableName");
        notNull(tableDefinition, "tableDefinition");
        notNull(rowClass, "rowClass");

        // We are on a different keyspace, create the table on that keyspace
        if (createTableOptions != null && Utils.hasLength(createTableOptions.getKeyspace())) {
            String otherKeyspace = createTableOptions.getKeyspace();
            createTableOptions.keyspace(null);
            return new Database(
                    this.rootEndpoint,
                    this.options.clone().keyspace(otherKeyspace))
                    .createTable(tableName, tableDefinition, rowClass, createTableOptions);
        }

        Command createTable = Command
                .create("createTable")
                .append("name", tableName)
                .append("definition", tableDefinition);
        if (createTableOptions != null) {
            createTable.append("options", createTableOptions);
        }
        runCommand(createTable, createTableOptions);

        // Spawning a Table inheriting the current database options and the table options
        TableOptions tableOptions = defaultTableOptions();
        if (createTableOptions != null) {
            if (createTableOptions.getDataAPIClientOptions() != null) {
                DataAPIClientOptions options = createTableOptions.getDataAPIClientOptions();
                // Merging db Headers
                Map<String, String> dbHeaders = new HashMap<>();
                dbHeaders.putAll(this.options.getDataAPIClientOptions().getDatabaseAdditionalHeaders());
                dbHeaders.putAll(options.getDatabaseAdditionalHeaders());
                options.databaseAdditionalHeaders(dbHeaders);
                // Merging admin Headers
                Map<String, String> adminHeaders = new HashMap<>();
                adminHeaders.putAll(this.options.getDataAPIClientOptions().getAdminAdditionalHeaders());
                adminHeaders.putAll(options.getAdminAdditionalHeaders());
                options.adminAdditionalHeaders(adminHeaders);
                tableOptions.dataAPIClientOptions(createTableOptions.getDataAPIClientOptions());
                // Merging listeners
                Map<String, CommandObserver> listeners = new HashMap<>();
                listeners.putAll(this.options.getDataAPIClientOptions().getObservers());
                listeners.putAll(options.getObservers());
                options.observers(listeners);
            }
            if (createTableOptions.getToken() != null) {
                tableOptions.token(createTableOptions.getToken());
            }
        }
        return getTable(tableName, rowClass, tableOptions);
    }

    /**
     * Creates a table using default options and runtime configurations.
     *
     * @param <T>             the type of the row objects that the table will hold
     * @param tableName       the name of the table to be created; must not be null or empty
     * @param tableDefinition the schema definition of the table; must not be null
     * @param rowClass        the class representing the row type; must not be null
     * @return the created table object
     */
    public <T> Table<T> createTable(String tableName, TableDefinition tableDefinition, Class<T> rowClass) {
        return createTable(tableName, tableDefinition, rowClass, null);
    }

    /**
     * Creates a table using default options and runtime configurations.
     *
     * @param tableName       the name of the table to be created; must not be null or empty
     * @param tableDefinition the schema definition of the table; must not be null
     * @param options         the option to initialize the class.
     * @return the created table object
     */
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions options) {
        return createTable(tableName, tableDefinition, Row.class, options);
    }

    /**
     * Creates a table with a default row type of {@code Row}.
     *
     * @param tableName       the name of the table to be created; must not be null or empty
     * @param tableDefinition the schema definition of the table; must not be null
     * @return the created table object with rows of type {@code Row}
     */
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition) {
        return createTable(tableName, tableDefinition, Row.class);
    }

    /**
     * Creates a table using default options and the inferred table name from the row class.
     *
     * @param <T>      the type of the row objects that the table will hold
     * @param rowClass the class representing the row type; must not be null
     * @return the created table object
     */
    public <T> Table<T> createTable(Class<T> rowClass) {
        return createTable(rowClass, new CreateTableOptions());
    }

    /**
     * Creates a table using default options and the inferred table name from the row class.
     *
     * @param <T>      the type of the row objects that the table will hold
     * @param rowClass the class representing the row type; must not be null
     * @param createTableOptions additional options for creating the table; optional, can be null
     * @return the created table object
     */
    public <T> Table<T> createTable(Class<T> rowClass, CreateTableOptions createTableOptions) {
        return createTable(getTableName(rowClass), rowClass, createTableOptions);
    }

    /**
     * Creates a table using default options and runtime configurations.
     *
     * @param <T>             the type of the row objects that the table will hold
     * @param rowClass        the class representing the row type; must not be null
     * @param tableName       the name of the table to be created; must not be null or empty
     * @return the created table object
     */
    public <T> Table<T> createTable(String tableName,
        Class<T> rowClass,
        CreateTableOptions createTableOptions) {
        hasLength(tableName, "tableName");
        notNull(rowClass, "rowClass");

        // We are on a different keyspace, create the table on that keyspace
        if (createTableOptions != null && Utils.hasLength(createTableOptions.getKeyspace())) {
            String otherKeyspace = createTableOptions.getKeyspace();
            createTableOptions.keyspace(null);
            return new Database(
                    this.rootEndpoint,
                    this.options.clone().keyspace(otherKeyspace))
                    .createTable(tableName, rowClass, createTableOptions);
        }

        // Building from the command
        Command createTable = new Command("createTable", createTableCommand(tableName, rowClass));
        if (createTableOptions != null) {
            createTable.append("options", createTableOptions);
        }
        runCommand(createTable, createTableOptions);

        // Getting ready for a table
        TableOptions tableOptions = defaultTableOptions();
        if (createTableOptions != null) {
            if (createTableOptions.getDataAPIClientOptions() != null) {
                tableOptions.dataAPIClientOptions(createTableOptions.getDataAPIClientOptions());
            }
            if (createTableOptions.getToken() != null) {
                tableOptions.token(createTableOptions.getToken());
            }
        }
        return getTable(tableName, rowClass, tableOptions);
    }

    /**
     * Creates a table using default options and runtime configurations.
     *
     * @param <T>       the type of the row objects that the table will hold
     * @param rowClass  the class representing the row type; must not be null
     * @return the created table object
     */
    public <T> String getTableName(Class<T> rowClass) {
        notNull(rowClass, "rowClass");
        EntityTable ann = rowClass.getAnnotation(EntityTable.class);
        if (ann == null) {
            throw new IllegalArgumentException("Class " + rowClass.getName() + " is not annotated with @Table");
        }
        if (!Utils.hasLength(ann.value())) {
            throw new IllegalArgumentException("Annotation @Table on class " + rowClass.getName() + " has no name");
        }
        return ann.value();
    }

    /**
     * Initialize a TableOption from the current database options.
     *
     * @return
     *      default table options
     */
    private TableOptions defaultTableOptions() {
        return new TableOptions(this.options.getToken(),
                this.options.getDataAPIClientOptions())
                .keyspace(getKeyspace());
    }

    // -------------------------------------
    // ----       Drop Table            ----
    // -------------------------------------

    /**
     * Deletes a collection (table) from the database.
     * This method delegates to {@link #dropTable(String, DropTableOptions)}
     * with default options.
     *
     * @param tableName
     *        the name of the table to be deleted; must not be null or empty.
     * @throws IllegalArgumentException
     *         if {@code tableName} is null or empty.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * database.dropTable("exampleTable");
     * }
     * </pre>
     */
    public void dropTable(String tableName) {
        dropTable(tableName, null);
    }

    /**
     * Deletes a collection (table) from the database with specific options.
     *
     * @param tableName
     *        the name of the table to be deleted; must not be null or empty.
     * @param dropTableOptions
     *        the options to configure the table deletion operation; can be null.
     * @throws IllegalArgumentException
     *         if {@code tableName} is null or empty.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * DropTableOptions options = new DropTableOptions();
     * database.dropTable("exampleTable", options);
     * }
     * </pre>
     */
    public void dropTable(String tableName, DropTableOptions dropTableOptions) {
        hasLength(tableName, "tableName");

        // We are on a different keyspace, drop the table on a different keyspace
        if (dropTableOptions != null && Utils.hasLength(dropTableOptions.getKeyspace())) {
            String otherKeyspace = dropTableOptions.getKeyspace();
            dropTableOptions.keyspace(null);
            new Database(
                    this.rootEndpoint,
                    this.options.clone().keyspace(otherKeyspace))
                    .dropTable(tableName, dropTableOptions);
        } else {
            // Command on current keyspace
            Command dropTableCmd = Command
                    .create("dropTable")
                    .append("name", tableName);
            if (dropTableOptions != null) {
                dropTableCmd.withOptions(dropTableOptions);
            }
            runCommand(dropTableCmd, dropTableOptions);
        }
    }
    // ------------------------------------------
    // ----   Drop Indexes                    ---
    // ------------------------------------------

    /**
     * Delete an index by name.
     *
     * @param indexName
     *      index name
     */
    public void dropTableIndex(String indexName) {
        dropTableIndex(indexName, null);
    }

    /**
     * Delete an index by name.
     *
     * @param indexName
     *      index name
     * @param dropIndexOptions
     *      flag to drop index
     */
    public void dropTableIndex(String indexName, DropTableIndexOptions dropIndexOptions) {
        Assert.hasLength(indexName, "indexName");

        // We are on a different keyspace, drop the table on a different keyspace
        if (dropIndexOptions != null && Utils.hasLength(dropIndexOptions.getKeyspace())) {
            String otherKeyspace = dropIndexOptions.getKeyspace();
            dropIndexOptions.keyspace(null);
            new Database(
                    this.rootEndpoint,
                    this.options.clone().keyspace(otherKeyspace))
                    .dropTableIndex(indexName, dropIndexOptions);
        } else {
            // Command on current keyspace
            Command dropIndexCommand = Command
                    .create("dropIndex")
                    .append("name", indexName);
            if (dropIndexOptions != null) {
                dropIndexCommand.withOptions(dropIndexOptions);
            }
            runCommand(dropIndexCommand, dropIndexOptions);
        }
    }

    // ------------------------------------------
    // ----    Generation Information        ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public String getApiEndpoint() {
       return this.apiEndpoint;
    }

}
