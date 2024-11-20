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

import com.datastax.astra.client.admin.AdminOptions;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionDefinition;
import com.datastax.astra.client.collections.CollectionDescriptor;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.commands.BaseOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.options.CreateCollectionOptions;
import com.datastax.astra.client.databases.options.ListCollectionOptions;
import com.datastax.astra.client.exception.InvalidConfigurationException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableDescriptor;
import com.datastax.astra.client.tables.ddl.CreateTableOptions;
import com.datastax.astra.client.tables.ddl.DropTableIndexOptions;
import com.datastax.astra.client.tables.ddl.DropTableOptions;
import com.datastax.astra.client.tables.index.TableIndexDefinition;
import com.datastax.astra.client.tables.index.TableIndexDescriptor;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.stream.Stream;

import static com.datastax.astra.client.core.commands.CommandType.TABLE_ADMIN;
import static com.datastax.astra.client.exception.InvalidEnvironmentException.throwErrorRestrictedAstra;
import static com.datastax.astra.client.tables.mapping.EntityBeanDefinition.createTableCommand;
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

    /** Api Endpoint for the API. */
    private final String apiEndpoint;

    /**
     * This core endpoint could be used for admin operations.
     */
    private final String databaseAdminEndpoint;

    /**
     * Options used to initialize the database
     */
    @Getter
    private final DatabaseOptions databaseOptions;

    /**
     * Initialization with endpoint and apikey.
     *
     * @param apiEndpoint
     *      api endpoint
     *  @param databaseOptions
     *      options with all attributes to connect to the database
     */
    public Database(String apiEndpoint, DatabaseOptions databaseOptions) {
        hasLength(apiEndpoint, "endpoint");
        notNull(databaseOptions, "options");
        this.databaseOptions = databaseOptions;
        this.apiEndpoint     = apiEndpoint;

        // Command Options initialized with DataAPIOptions
        this.baseOptions = new BaseOptions<>(
                databaseOptions.getToken(),
                TABLE_ADMIN,
                databaseOptions.getDataAPIClientOptions());

        // Endpoint for admin operation (out of db endpoint)
        this.databaseAdminEndpoint = apiEndpoint
                .endsWith(databaseOptions.getDataAPIClientOptions().getApiVersion()) ?
                    apiEndpoint :
                    apiEndpoint + "/" + databaseOptions.getDataAPIClientOptions().getApiVersion();
    }

    // ------------------------------------------
    // ----      Core Features               ----
    // ------------------------------------------

    /**
     * Get the region of the database if deployed in Astra.
     *
     * @return
     *      the region
     */
    public String getKeyspace() {
        return databaseOptions.getKeyspace();
    }

    /**
     * Get the region of the database if deployed in Astra.
     *
     * @return
     *      the region
     */
    public String getRegion() {
        if (!databaseOptions.getDataAPIClientOptions().isAstra()) {
            throwErrorRestrictedAstra("getRegion", databaseOptions.getDataAPIClientOptions().getDestination());
        }
        return AstraApiEndpoint.parse(getApiEndpoint()).getDatabaseRegion();
    }

    /**
     * Get the region of the database if deployed in Astra.
     *
     * @return
     *      the region
     */
    public UUID getId() {
        if (!databaseOptions.getDataAPIClientOptions().isAstra()) {
            throwErrorRestrictedAstra("getRegion", databaseOptions.getDataAPIClientOptions().getDestination());
        }
        return AstraApiEndpoint.parse(getApiEndpoint()).getDatabaseId();
    }

    /**
     * Get Information about the current database.
     *
     * @return
     *      information regarding current database.
     */
    public DatabaseInfo getInfo() {
        return getAdmin().getDatabaseInfo(getId());
    }

    /**
     * This mutates the keyspace to be used.
     *
     * @param keyspace
     *      current keyspace
     * @return
     *      the database
     */
    public Database useKeyspace(String keyspace) {
        this.databaseOptions.keyspace(keyspace);
        return this;
    }

    // ------------------------------------------
    // ----   Access Database Admin          ----
    // ------------------------------------------

    /**
     * Access a database Admin client from the database
     *
     * @return
     *      access AstraDBAdmin if the client is Astra
     */
    public AstraDBAdmin getAdmin() {
        return getAdmin(databaseOptions.getToken());
    }

    /**
     * Access a database Admin client from the database
     *
     * @param superUserToken
     *     provide a token with a super-user role
     * @return
     *      access AstraDBAdmin if the client is Astra
     */
    public AstraDBAdmin getAdmin(String superUserToken) {
        return getAdmin(new AdminOptions(superUserToken, databaseOptions.getDataAPIClientOptions()));
    }

    public AstraDBAdmin getAdmin(AdminOptions adminOptions) {
        if (!databaseOptions.getDataAPIClientOptions().isAstra()) {
            throwErrorRestrictedAstra("getAdmin", databaseOptions
                    .getDataAPIClientOptions()
                    .getDestination());
        }
        return new AstraDBAdmin(adminOptions);
    }

    /**
     * Access a database Admin client from the database
     * @return
     *      database admin
     */
    public DatabaseAdmin getDatabaseAdmin() {
        return getDatabaseAdmin(databaseOptions.getToken());
    }

    public DatabaseAdmin getDatabaseAdmin(String superUserToken) {
        return getDatabaseAdmin(new AdminOptions(superUserToken, databaseOptions.getDataAPIClientOptions()));
    }

    /**
     * Gets the name of the database.
     *
     * @param adminOptions
     *      admin token
     * @return the database name
     */
    public DatabaseAdmin getDatabaseAdmin(AdminOptions adminOptions) {
        if (databaseOptions.getDataAPIClientOptions().isAstra()) {
            AstraApiEndpoint endpoint = AstraApiEndpoint.parse(getApiEndpoint());
            DataAPIClientOptions options = databaseOptions.getDataAPIClientOptions().clone();
            return new AstraDBDatabaseAdmin(adminOptions.getToken(), endpoint.getDatabaseId(), options);
        }
        DatabaseOptions dbOption = databaseOptions.clone();
        if (adminOptions.getDataAPIClientOptions() != null) {
            dbOption.dataAPIClientOptions(adminOptions.getDataAPIClientOptions());
        }
        if (adminOptions.getToken() != null) {
            dbOption.token(adminOptions.getToken());
        }
        return new DataAPIDatabaseAdmin(databaseAdminEndpoint, dbOption);
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
        return listCollectionNames(null);
    }

    /**
     * Gets the names of all the collections in this database.
     *
     * @param listCollectionOptions
     *      options to the list collection
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    public Stream<String> listCollectionNames(ListCollectionOptions listCollectionOptions) {
        Command findCollections = Command.create("findCollections");
        return runCommand(findCollections, listCollectionOptions)
                .getStatusKeyAsList("collections", String.class)
                .stream();
    }

    /**
     * Finds all the collections in this database.
     *
     * @return
     *  list of collection definitions
     */
    public Stream<CollectionDescriptor> listCollections() {
        return listCollections(null);
    }

    public Stream<CollectionDescriptor> listCollections(ListCollectionOptions listCollectionOptions) {
        Command findCollections = Command
                .create("findCollections")
                .withOptions(new Document().append("explain", true));
        return runCommand(findCollections, listCollectionOptions)
                .getStatusKeyAsList("collections", CollectionDescriptor.class)
                .stream();
    }

    /**
     * Evaluate if a collection exists.
     *
     * @param collection
     *      collections name.
     * @return
     *      if collections exists
     */
    public boolean collectionExists(String collection) {
        return listCollectionNames().anyMatch(collection::equals);
    }

    // ------------------------------------------
    // ----        Get Collection            ----
    // ------------------------------------------

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
        return getCollection(collectionName, new CollectionOptions(
                databaseOptions.getToken(),
                databaseOptions.getDataAPIClientOptions()), documentClass);
    }

    public <T> Collection<T> getCollection(String collectionName, CollectionOptions options,  @NonNull Class<T> documentClass) {
        hasLength(collectionName, "collectionName");
        notNull(options, "options");
        notNull(documentClass, "documentClass");
        return new Collection<>(this, collectionName, options, documentClass);
    }

    // ------------------------------------------
    // ----      Create Collection           ----
    // ------------------------------------------

    public Collection<Document> createCollection(String collectionName) {
        return createCollection(collectionName, Document.class);
    }

    public <T> Collection<T> createCollection(String collectionName, Class<T> documentClass) {
        return createCollection(collectionName,
                null, // no CollectionDefinitionOptions as simple
                new CollectionOptions(databaseOptions.getToken(), databaseOptions.getDataAPIClientOptions()),
                null, // No create collection options
                documentClass);
    }

    public Collection<Document> createCollection(String name, CollectionDefinition def) {
        return createCollection(name, def, Document.class);
    }

    public <T>  Collection<T> createCollection(String name, CollectionDefinition def, Class<T> documentClass) {
        return createCollection(name,
                def,
                new CollectionOptions(getDatabaseOptions().getToken(), databaseOptions.getDataAPIClientOptions()),
                null,
                documentClass);
    }

    public Collection<Document> createCollection(String collectionName,
        CollectionDefinition collectionDefinition,
        CollectionOptions collectionOptions,
        CreateCollectionOptions createCollectionOptions) {
        return createCollection(
                collectionName, collectionDefinition,
                collectionOptions, createCollectionOptions,
                Document.class);
    }

    /**
     * Create a new collection with the selected options
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param collectionDefinition
     *      definition describing the structure of the collection object
     * @param collectionOptions
     *     settings for spawning the collection object
     * @param createCollectionOptions
     *      options to specialized the creation of the collection (timeouts...)
     * @param <T>
     *     working class for the document
     * @return
     *    the initialized collection
     */
    public <T> Collection<T> createCollection(String collectionName,
        CollectionDefinition collectionDefinition,
        CollectionOptions collectionOptions,
        CreateCollectionOptions createCollectionOptions,
        Class<T> documentClass) {
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
        log.info("Collection  '" + green("{}") + "' has been created", collectionName);
        return getCollection(collectionName, collectionOptions, documentClass);
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
                .append("name", collectionName), this.baseOptions);
        log.info("Collection  '" + green("{}") + "' has been deleted", collectionName);
    }

    // ------------------------------------------
    // -------     TABLES CRUD              -----
    // ------------------------------------------

    /**
     * Gets the names of all the tables in this database.
     *
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    public Stream<String> listTableNames() {
        return runCommand(Command.create("listTables"))
                .getStatusKeyAsList("tables", String.class)
                .stream();
    }

    /**
     * Finds all the tables in this database.
     *
     * @return
     *      list of table definitions
     */
    public Stream<TableDescriptor> listTables() {
        Command findTables = Command
                .create("listTables")
                .withOptions(new Document().append("explain", true));
        return runCommand(findTables)
                .getStatusKeyAsList("tables", TableDescriptor.class)
                .stream();
    }

    /**
     * Evaluate if a collection exists.
     *
     * @param tableName
     *      table name.
     * @return
     *      if collections exists
     */
    public boolean tableExists(String tableName) {
        return listTableNames().anyMatch(tableName::equals);
    }

    /**
     * Gets a collection.
     *
     * @param tableName
     *      the name of the table to return
     * @return
     *      the collection
     * @throws IllegalArgumentException
     *      if collectionName is invalid
     */
    public Table<Row> getTable(String tableName) {
        return getTable(tableName, this.baseOptions, Row.class);
    }

    /**
     * Gets a table, with a specific default document class.
     *
     * @param tableName
     *      the name of the collection to return
     * @param rowClass
     *      the default class to cast any row returned from the database into.
     * @param <T>
     *      the type of the class to use instead of {@code Document}.
     * @return
     *      the collection
     */
    public <T> Table<T> getTable(String tableName, @NonNull Class<T> rowClass) {
        return getTable(tableName, this.baseOptions, rowClass);
    }

    public <T> Table<T> getTable(@NonNull Class<T> rowClass) {
        EntityTable ann = rowClass.getAnnotation(EntityTable.class);
        if (ann == null) {
            InvalidConfigurationException.throwErrorMissingAnnotation(
                    EntityTable.class.getSimpleName(),
                    rowClass.getName(),
                    "getTable(rowClass)");
        }
        return getTable(ann.value(), this.baseOptions, rowClass);
    }

    /**
     * Gets a table with a specific default document class.
     *
     * @param tableName
     *      the name of the table to
     * @param rowClass
     *      the default class to cast any row returned from the database into.
     * @param baseOptions
     *      options to use when using this table
     * @param <T>
     *      the type of the class to use instead of {@code Row}.
     * @return
     *      the table
     */
    public <T> Table<T> getTable(String tableName, BaseOptions<?> baseOptions, @NonNull Class<T> rowClass) {
        hasLength(tableName, "tableName");
        notNull(rowClass, "rowClass");
        return new Table<>(this, tableName, baseOptions, rowClass);
    }

    /**
     * Create a new table with the given description.
     *
     * @param tableName
     *      the name of the table to create
     * @param tableDefinition
     *      table definition
     */
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition) {
        return createTable(tableName, tableDefinition, null, this.baseOptions, Row.class);
    }

    /**
     * Create a new table with the given description.
     *
     * @param tableName
     *      the name for the new table to create
     * @param tableDefinition
     *      table definition
     * @param options
     *      collection options
     */
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions options) {
        return createTable(tableName, tableDefinition, options, baseOptions, Row.class);
    }

    /**
     * Create a new table with the given description.
     *
     * @param tableName
     *      the table name
     * @param tableDefinition
     *      table definition
     * @param options
     *      collection options
     * @param documentClass
     *      document class
     * @return
     *      the collection created
     * @param <T>
     *      working object for the document
     */
    public <T> Table<T> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions options, Class<T> documentClass) {
        return createTable(tableName, tableDefinition, options, baseOptions, documentClass);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param tableName
     *      the definition for the new table to create
     * @param tableDefinition
     *      the definition for the new table to create
     * @param tableOptions
     *      various options for creating the table
     * @param baseOptions
     *      options to use when using this collection
     * @return the collection
     */
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions tableOptions, BaseOptions<?> baseOptions) {
        return createTable(tableName, tableDefinition, tableOptions, baseOptions, Row.class);
    }

    public <T> Table<T> createTable(Class<T> rowClass) {
        return createTable(getTableName(rowClass), rowClass);
    }

    public <T> Table<T> createTable(@NonNull Class<T> rowClass, CreateTableOptions tableOptions) {
        return createTable(getTableName(rowClass), rowClass, tableOptions);
    }

    public <T> Table<T> createTable(String tableName, @NonNull Class<T> rowClass) {
        return createTable(tableName, rowClass, null);
    }

    public <T> Table<T> createTable(String tableName, @NonNull Class<T> rowClass, CreateTableOptions tableOptions) {
        hasLength(tableName, "tableName");
        notNull(rowClass, "rowClass");
        Command createTable = new Command("createTable", createTableCommand(tableName, rowClass));
        if (tableOptions != null) {
            createTable.append("options", tableOptions);
        }
        runCommand(createTable, baseOptions);
        log.info("Table  '" + green("{}") + "' has been created", tableName);
        return getTable(tableName, baseOptions, rowClass);
    }

    /**
     * Create a new table with the selected options
     *
     * @param tableName
     *      the definition for the new table to create
     * @param tableDefinition
     *      the definition for the new table to create
     * @param tableOptions
     *      various options for creating the table
     * @param rowClass
     *     the default class to cast any row returned from the database into.
     * @param baseOptions
     *      options to use when using this collection
     * @param <T>
     *          working class for the document
     * @return the collection
     */
    public <T> Table<T> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions tableOptions, BaseOptions<?> baseOptions, Class<T> rowClass) {
        hasLength(tableName, "tableName");
        notNull(tableDefinition, "tableDefinition");
        notNull(rowClass, "rowClass");
        Command createTable = Command
                .create("createTable")
                .append("name", tableName)
                .append("definition", tableDefinition);
        if (tableOptions != null) {
            createTable.append("options", tableOptions);
        }
        runCommand(createTable, baseOptions);
        log.info("Table  '" + green("{}") + "' has been created", tableName);
        return getTable(tableName, baseOptions, rowClass);
    }

    private <T> String getTableName(Class<T> rowClass) {
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
     * Delete a collection.
     *
     * @param tableName
     *      table name
     */
    public void dropTable(String tableName) {
        dropTable(tableName, null);
    }

    /**
     * Delete a collection.
     *
     * @param tableName
     *      table name
     */
    public void dropTable(String tableName, DropTableOptions dropTableOptions) {
        hasLength(tableName, "tableName");
        Command dropTableCmd = Command
                .create("dropTable")
                .append("name", tableName);
        if (dropTableOptions != null) {
            dropTableCmd.withOptions(dropTableOptions);
        }
        runCommand(dropTableCmd, baseOptions);
        log.info("Table  '" + green("{}") + "' has been deleted", tableName);
    }

    // ------------------------------------------
    // ----     Indexes CRUD                  ---
    // ------------------------------------------

    /**
     * Gets the names of indices in the selected keyspace.
     *
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    public Stream<String> listIndexesNames() {
        return runCommand(Command.create("listIndexes"))
                .getStatusKeyAsList("indexes", String.class)
                .stream();
    }

    /**
     * Finds all the indices in the selected keyspace.
     *
     * @return
     *      list of table definitions
     */
    public Stream<TableIndexDefinition> listIndexes() {
        Command findTables = Command
                .create("listIndexes")
                .withOptions(new Document().append("explain", true));
        return runCommand(findTables)
                .getStatusKeyAsList("indexes", TableIndexDescriptor.class)
                .stream().map(TableIndexDescriptor::getDefinition);
    }

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
        Command dropIndexCommand = Command.create("dropIndex").append("name", indexName);
        if (dropIndexOptions != null) {
            dropIndexCommand.withOptions(dropIndexOptions);
        }
        runCommand(dropIndexCommand, baseOptions);
        log.info("Index  '" + green("{}") + "' has been dropped", indexName);
    }

    // ------------------------------------------
    // ----    Generation Information        ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected DataAPISerializer getSerializer() {
        return this.baseOptions.getSerializer();
    }

    /** {@inheritDoc} */
    @Override
    public String getApiEndpoint() {
        StringBuilder dbApiEndPointBuilder = new StringBuilder(apiEndpoint);
        // Adding /api/json if needed for Astra.
        switch(databaseOptions.getDataAPIClientOptions().getDestination()) {
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
        return dbApiEndPointBuilder
                .append("/")
                .append(databaseOptions.getDataAPIClientOptions().getApiVersion())
                .append("/")
                .append(databaseOptions.getKeyspace())
                .toString();
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link BaseOptions}.
     *
     * @param logger
     *      name for the logger
     * @param commandObserver
     *      class for the logger
     */
    public void registerListener(String logger, CommandObserver commandObserver) {
        this.baseOptions.registerObserver(logger, commandObserver);
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link BaseOptions}.
     *
     * @param name
     *      name for the observer
     */
    public void deleteListener(String name) {
        this.baseOptions.unregisterObserver(name);
    }

}
