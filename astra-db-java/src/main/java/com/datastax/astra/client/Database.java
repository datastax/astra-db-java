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

import com.datastax.astra.client.DataAPIOptions.DataAPIDestination;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.CollectionInfo;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.HttpClientOptions;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.stream.Stream;

import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * A Data API database. This is the entry-point object for doing database-level
 * DML, such as creating/deleting collections, and for obtaining Collection
 * objects themselves. This class has a synchronous interface.
 * <p>
 * A Database comes with an "API Endpoint", which implies a Database object
 * instance reaches a specific region (relevant point in case of multi-region
 * databases).
 * </p>
 */
@Slf4j
public class Database extends AbstractCommandRunner {

    /** Current Namespace information.*/
    @Getter
    private final String namespaceName;

    /** Token to be used with the Database. */
    private final String token;

    /** Api Endpoint for the API. */
    private final String apiEndpoint;

    /** Options to set up the client. */
    @Getter
    private final DataAPIOptions options;

    /**
     * This core endpoint could be used for admin operations.
     */
    private final String databaseAdminEndpoint;

    /**
     * Initialization with endpoint and apikey.
     *
     * @param token
     *      api token
     * @param apiEndpoint
     *      api endpoint
     */
    public Database(String apiEndpoint, String token) {
        this(apiEndpoint, token, AstraDBAdmin.DEFAULT_NAMESPACE, DataAPIOptions.builder().build());
    }

    /**
     * Initialization with endpoint and apikey.
     *
     * @param token
     *      api token
     * @param apiEndpoint
     *      api endpoint
     * @param namespace
     *      namespace
     */
    public Database(String apiEndpoint, String token, String namespace) {
        this(apiEndpoint, token, namespace,  DataAPIOptions.builder().build());
    }

    /**
     * Initialization with endpoint and apikey.
     *
     * @param apiEndpoint
     *      api endpoint
     *  @param token
     *      api token
     * @param namespace
     *      namespace
     * @param options
     *      setup of the clients with options
     */
    public Database(String apiEndpoint, String token, String namespace, DataAPIOptions options) {
        hasLength(apiEndpoint, "endpoint");
        hasLength(token,     "token");
        hasLength(namespace, "namespace");
        notNull(options, "options");
        this.namespaceName = namespace;
        this.token         = token;
        this.options       = options;

        // Adding version number if needed
        this.databaseAdminEndpoint = apiEndpoint.endsWith(options.getApiVersion()) ?
                apiEndpoint :
                apiEndpoint + "/" + options.getApiVersion();
        StringBuilder dbApiEndPointBuilder = new StringBuilder(apiEndpoint);
        // Adding /api/json if needed for Astra.
        switch(options.destination) {
            case ASTRA:
            case ASTRA_TEST:
            case ASTRA_DEV:
                if (apiEndpoint.endsWith(".com")) {
                    dbApiEndPointBuilder.append("/api/json");
                }
            break;
            default:
                // left blank as local deployments does not require any change
            break;
        }
        this.apiEndpoint = dbApiEndPointBuilder
                .append("/")
                .append(options.getApiVersion())
                .append("/")
                .append(namespaceName)
                .toString();
        // Build Command
        this.commandOptions = new CommandOptions<>()
                .token(token)
                .embeddingAPIKey(options.getEmbeddingAPIKey())
                .httpClientOptions(options.getHttpClientOptions());
        options.getObservers().forEach(this.commandOptions::registerObserver);
    }

    // ------------------------------------------
    // ----   Access Database Admin          ----
    // ------------------------------------------

    /**
     * Access a database Admin client from the database
     * @return
     *      database admin
     */
    public DatabaseAdmin getDatabaseAdmin() {
        return getDatabaseAdmin(this.token);
    }

    /**
     * Gets the name of the database.
     *
     * @param superUserToken
     *      provide a token with a super-user role
     * @return the database name
     */
    public DatabaseAdmin getDatabaseAdmin(String superUserToken) {
        if (Objects.requireNonNull(options.getDestination()) == DataAPIDestination.ASTRA) {
            AstraApiEndpoint endpoint = AstraApiEndpoint.parse(apiEndpoint);
            return new AstraDBDatabaseAdmin(superUserToken, endpoint.getDatabaseId(), endpoint.getEnv(), options);
        }
        return new DataAPIDatabaseAdmin(databaseAdminEndpoint, token, options);
    }

    // ------------------------------------------
    // ----     Collection CRUD              ----
    // ------------------------------------------

    /**
     * Gets the names of all the collections in this database.
     *
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    public Stream<String> listCollectionNames() {

        Command findCollections = Command.create("findCollections");

        return runCommand(findCollections)
                .getStatusKeyAsList("collections", String.class)
                .stream();
    }

    /**
     * Finds all the collections in this database.
     *
     * @return
     *  list of collection definitions
     */
    public Stream<CollectionInfo> listCollections() {
        Command findCollections = Command
                .create("findCollections")
                .withOptions(new Document().append("explain", true));

        return runCommand(findCollections)
                .getStatusKeyAsList("collections", CollectionInfo.class)
                .stream();
    }

    /**
     * Evaluate if a collection exists.
     *
     * @param collection
     *      namespace name.
     * @return
     *      if namespace exists
     */
    public boolean collectionExists(String collection) {
        return listCollectionNames().anyMatch(collection::equals);
    }

    /**
     * Gets a collection.
     *
     * @param collectionName
     *      the name of the collection to return
     * @return
     *      the collection
     * @throws IllegalArgumentException
     *      if collectionName is invalid
     */
    public Collection<Document> getCollection(String collectionName) {
        return getCollection(collectionName, Document.class);
    }

    /**
     * Gets a collection, with a specific default document class.
     *
     * @param collectionName
     *      the name of the collection to return
     * @param documentClass
     *      the default class to cast any documents returned from the database into.
     * @param <T>
     *      the type of the class to use instead of {@code Document}.
     * @return
     *      the collection
     */
    public <T> Collection<T> getCollection(String collectionName, @NonNull Class<T> documentClass) {
        return getCollection(collectionName, null, documentClass);
    }

    /**
     * Gets a collection, with a specific default document class.
     *
     * @param collectionName
     *      the name of the collection to return
     * @param documentClass
     *      the default class to cast any documents returned from the database into.
     * @param commandOptions
     *      options to use when using this collection
     * @param <T>
     *      the type of the class to use instead of {@code Document}.
     * @return
     *      the collection
     */
    public <T> Collection<T> getCollection(String collectionName, CommandOptions<?> commandOptions, @NonNull Class<T> documentClass) {
        hasLength(collectionName, "collectionName");
        notNull(documentClass, "documentClass");
        CommandOptions<?> collectionCollectionOptions = new CommandOptions<>()
                .token(token)
                .embeddingAPIKey(options.getEmbeddingAPIKey())
                .httpClientOptions(options.getHttpClientOptions());
        this.commandOptions.getObservers().forEach(collectionCollectionOptions::registerObserver);

        // Override with the commandOptions if any for a collection
        if (commandOptions != null) {
            commandOptions.getToken().ifPresent(collectionCollectionOptions::token);
            commandOptions.getEmbeddingAPIKey().ifPresent(collectionCollectionOptions::embeddingAPIKey);
            commandOptions.getHttpClientOptions().ifPresent(collectionCollectionOptions::httpClientOptions);
            commandOptions.getObservers().forEach(collectionCollectionOptions::registerObserver);
        }
        return new Collection<>(this, collectionName, collectionCollectionOptions, documentClass);
    }

    /**
     * Drops this namespace
     */
    public void drop() {
        getDatabaseAdmin().dropNamespace(getNamespaceName());
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     * @return
     *      the instance of collection
     */
    public Collection<Document> createCollection(String collectionName) {
        return createCollection(collectionName, null, null, Document.class);
    }

    /**
     * Create a default new collection for vector.
     * @param collectionName
     *      collection name
     * @param dimension
     *      vector dimension
     * @param metric
     *      vector metric
     * @return
     *      the instance of collection
     */
    public Collection<Document> createCollection(String collectionName, int dimension, SimilarityMetric metric) {
        return createCollection(collectionName, dimension, metric, Document.class);
    }

    /**
     * Create a default new collection for vector.
     * @param collectionName
     *      collection name
     * @param dimension
     *      vector dimension
     * @param metric
     *      vector metric
     * @param documentClass
     *      class of document to return
     * @param <T>
     *          working class for the document
     * @return
     *      the instance of collection
     */
    public <T> Collection<T> createCollection(String collectionName, int dimension, SimilarityMetric metric, Class<T> documentClass) {
            return createCollection(collectionName, CollectionOptions.builder()
                    .vectorDimension(dimension)
                    .vectorSimilarity(metric)
                    .build(), commandOptions, documentClass);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param documentClass
     *      class of document to return
     * @param <T>
     *          working class for the document
     * @return the collection
     */
    public <T> Collection<T> createCollection(String collectionName, Class<T> documentClass) {
        return createCollection(collectionName, null, commandOptions, documentClass);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param collectionOptions
     *      various options for creating the collection
     * @return the collection
     */
    public Collection<Document> createCollection(String collectionName, CollectionOptions collectionOptions) {
        return createCollection(collectionName, collectionOptions, commandOptions, Document.class);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      collection name
     * @param collectionOptions
     *      collection options
     * @param documentClass
     *      document class
     * @return
     *      the collection created
     * @param <T>
     *      working object for the document
     */
    public <T> Collection<T> createCollection(String collectionName, CollectionOptions collectionOptions,  Class<T> documentClass) {
        return createCollection(collectionName, collectionOptions, commandOptions, documentClass);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param collectionOptions
     *      various options for creating the collection
     * @param commandOptions
     *      options to use when using this collection
     * @return the collection
     */
    public Collection<Document> createCollection(String collectionName, CollectionOptions collectionOptions, CommandOptions<?> commandOptions) {
        return createCollection(collectionName, collectionOptions, commandOptions, Document.class);
    }

    /**
     * Create a new collection with the selected options
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param collectionOptions
     *      various options for creating the collection
     * @param documentClass
     *     the default class to cast any documents returned from the database into.
     * @param commandOptions
     *      options to use when using this collection
     * @param <T>
     *          working class for the document
     * @return the collection
     */
    public <T> Collection<T> createCollection(String collectionName, CollectionOptions collectionOptions, CommandOptions<?> commandOptions, Class<T> documentClass) {
        hasLength(collectionName, "collectionName");
        notNull(documentClass, "documentClass");
        Command createCollection = Command
                .create("createCollection")
                .append("name", collectionName);
        if (collectionOptions != null) {
            createCollection.withOptions(JsonUtils.convertValue(collectionOptions, Document.class));
        }
        runCommand(createCollection, commandOptions);
        log.info("Collection  '" + green("{}") + "' has been created", collectionName);
        return getCollection(collectionName, commandOptions, documentClass);
    }

    /**
     * Delete a collection.
     *
     * @param collectionName
     *      collection name
     */
    public void dropCollection(String collectionName) {
        runCommand(Command
                .create("deleteCollection")
                .append("name", collectionName), commandOptions);
        log.info("Collection  '" + green("{}") + "' has been deleted", collectionName);
    }

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return apiEndpoint;
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link CommandOptions}.
     *
     * @param logger
     *      name for the logger
     * @param commandObserver
     *      class for the logger
     */
    @Deprecated
    public void registerListener(String logger, CommandObserver commandObserver) {
        this.commandOptions.registerObserver(logger, commandObserver);
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link CommandOptions}.
     *
     * @param name
     *      name for the observer
     */
    @Deprecated
    public void deleteListener(String name) {
        this.commandOptions.unregisterObserver(name);
    }
}
