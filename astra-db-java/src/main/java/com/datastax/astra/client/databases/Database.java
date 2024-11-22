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
import com.datastax.astra.client.core.commands.BaseOptions;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.databases.options.CreateCollectionOptions;
import com.datastax.astra.client.databases.options.DropCollectionOptions;
import com.datastax.astra.client.databases.options.ListCollectionOptions;
import com.datastax.astra.client.databases.options.ListTablesOptions;
import com.datastax.astra.client.exception.InvalidConfigurationException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableDescriptor;
import com.datastax.astra.client.tables.TableOptions;
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
import com.datastax.astra.internal.serdes.DatabaseSerializer;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class Database extends AbstractCommandRunner<DatabaseOptions> {

    public static final DataAPISerializer DEFAULT_DATABASE_SERIALIZER = new DatabaseSerializer();

    /**
     * This core endpoint could be used for admin operations.
     */
    @Getter
    private final String rootEndpoint;

    /**
     * Initialization with endpoint and apikey.
     *
     * @param rootEndpoint
     *      api endpoint
     *  @param options
     *      options with all attributes to connect to the database
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
        this.apiEndpoint  = dbApiEndPointBuilder
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
     * Get the region of the database if deployed in Astra.
     *
     * @return
     *      the region
     */
    public String getKeyspace() {
        return options.getKeyspace();
    }

    /**
     * Get the region of the database if deployed in Astra.
     *
     * @return
     *      the region
     */
    public String getRegion() {
        assertIsAstra();
        return AstraApiEndpoint.parse(getApiEndpoint()).getDatabaseRegion();
    }

    /**
     * Get the region of the database if deployed in Astra.
     *
     * @return
     *      the region
     */
    public UUID getId() {
        assertIsAstra();
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
        this.options.keyspace(keyspace);
        return this;
    }

    // ------------------------------------------
    // ----   Access Database Admin          ----
    // ------------------------------------------

    public AstraDBAdmin getAdmin() {
        return getAdmin(options.getToken());
    }

    public AstraDBAdmin getAdmin(String superUserToken) {
        return getAdmin(new AdminOptions(superUserToken, options.getDataAPIClientOptions()));
    }

    public AstraDBAdmin getAdmin(AdminOptions adminOptions) {
        assertIsAstra();
        return new AstraDBAdmin(adminOptions);
    }

    public DatabaseAdmin getDatabaseAdmin() {
        return getDatabaseAdmin(options.getToken());
    }

    public DatabaseAdmin getDatabaseAdmin(String superUserToken) {
        return getDatabaseAdmin(new AdminOptions(superUserToken, options.getDataAPIClientOptions()));
    }

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

    // ------------------------------------------
    // ----     Collection CRUD              ----
    // ------------------------------------------

    /**
     * Gets the names of all the collections in this database.
     *
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    public List<String> listCollectionNames() {
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
    public List<String> listCollectionNames(ListCollectionOptions listCollectionOptions) {
        Command findCollections = Command.create("findCollections");
        return runCommand(findCollections, listCollectionOptions)
                .getStatusKeyAsStringStream("collections")
                .toList();
    }

    /**
     * Finds all the collections in this database.
     *
     * @return
     *  list of collection definitions
     */
    public List<CollectionDescriptor> listCollections() {
        return listCollections(null);
    }

    public List<CollectionDescriptor> listCollections(ListCollectionOptions listCollectionOptions) {
        Command findCollections = Command
                .create("findCollections")
                .withOptions(new Document().append("explain", true));
        return runCommand(findCollections, listCollectionOptions)
                .getStatusKeyAsList("collections", CollectionDescriptor.class);
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
        return listCollectionNames().contains(collection);
    }

    // ------------------------------------------
    // ----        Get Collection            ----
    // ------------------------------------------

    public Collection<Document> getCollection(String collectionName) {
        return getCollection(collectionName, Document.class);
    }

    public <T> Collection<T> getCollection(String collectionName, @NonNull Class<T> documentClass) {
        return getCollection(collectionName, new CollectionOptions(
                options.getToken(),
                options.getDataAPIClientOptions()), documentClass);
    }

    public Collection<Document> getCollection(String collectionName,  CollectionOptions collectionOptions) {
        return getCollection(collectionName, collectionOptions, Document.class);
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
                new CollectionOptions(options.getToken(), options.getDataAPIClientOptions()),
                null, // No create collection options
                documentClass);
    }

    public Collection<Document> createCollection(String name, CollectionDefinition def) {
        return createCollection(name, def, Document.class);
    }

    public <T>  Collection<T> createCollection(String name, CollectionDefinition def, Class<T> documentClass) {
        return createCollection(name,
                def,
                new CollectionOptions(options.getToken(), options.getDataAPIClientOptions()),
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
        dropCollection(collectionName, null);
    }

    /**
     * Delete a collection.
     *
     * @param collectionName
     *      collection name
     */
    public void dropCollection(String collectionName, DropCollectionOptions dropCollectionOptions) {
        runCommand(Command
                .create("deleteCollection")
                .append("name", collectionName), dropCollectionOptions);
        log.info("Collection  '" + green("{}") + "' has been deleted", collectionName);
    }

    // ------------------------------------------
    // -------    List tables               -----
    // ------------------------------------------

    public List<String> listTableNames() {
        return listTableNames(new ListTablesOptions());
    }

    /**
     * Gets the names of all the tables in this database.
     *
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    public List<String> listTableNames(ListTablesOptions options) {
        return runCommand(Command.create("listTables"), options)
                .getStatusKeyAsStringStream("tables")
                .toList();
    }

    public List<TableDescriptor> listTables() {
        return listTables(new ListTablesOptions());
    }

    /**
     * Finds all the tables in this database.
     *
     * @return
     *      list of table definitions
     */
    public List<TableDescriptor> listTables(ListTablesOptions options) {
        Command findTables = Command
                .create("listTables")
                .withOptions(new Document().append("explain", true));
        return runCommand(findTables, options)
                .getStatusKeyAsList("tables", TableDescriptor.class);
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
        return listTableNames().contains(tableName);
    }

    // ------------------------------------------
    // ----      Get Table                   ----
    // ------------------------------------------

    public Table<Row> getTable(String tableName) {
        return getTable(tableName, Row.class);
    }

    public <T> Table<T> getTable(String tableName, @NonNull Class<T> rowClass) {
        return getTable(tableName, new TableOptions(
                options.getToken(),
                options.getDataAPIClientOptions()), rowClass);

    }

    public Table<Row> getTable(String tableName,  TableOptions tableOptions) {
        return getTable(tableName, tableOptions, Row.class);
    }

    public <T> Table<T> getTable(String tableName, TableOptions tableOptions, @NonNull Class<T> rowClass) {
        hasLength(tableName, "tableName");
        notNull(rowClass, "rowClass");
        return new Table<>(this, tableName, tableOptions, rowClass);
    }

    public <T> Table<T> getTable(@NonNull Class<T> rowClass) {
        EntityTable ann = rowClass.getAnnotation(EntityTable.class);
        if (ann == null) {
            InvalidConfigurationException.throwErrorMissingAnnotation(
                    EntityTable.class.getSimpleName(),
                    rowClass.getName(),
                    "getTable(rowClass)");
        }
        return getTable(ann.value(), rowClass);
    }

    // ------------------------------------------
    // ----      Create Table           ----
    // ------------------------------------------

    // definition with a proposed bean
    public <T> Table<T> createTable(String tableName, TableDefinition tableDefinition, Class<T> rowClass) {
        return createTable(tableName, tableDefinition, new CreateTableOptions(), rowClass, new TableOptions());
    }

    // definition with default rowClass
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition) {
        return createTable(tableName, tableDefinition, Row.class);
    }

    // definition with a proposed bean
    public <T> Table<T> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions createTableOptions, Class<T> rowClass) {
        return createTable(tableName, tableDefinition,  createTableOptions, rowClass,  new TableOptions());
    }
    public Table<Row> createTable(String tableName, TableDefinition tableDefinition, CreateTableOptions tableOptions) {
        return createTable(tableName, tableDefinition, tableOptions, Row.class);
    }

    // definition from the bean
    public <T> Table<T> createTable(Class<T> rowClass) {
        return createTable(rowClass, new CreateTableOptions());
    }

    public <T> Table<T> createTable(Class<T> rowClass, CreateTableOptions createTableOptions) {
        return createTable(getTableName(rowClass), createTableOptions, rowClass, new TableOptions());
    }

    /**
     * As no table definition is provided on is created from the annotations of the bean
     *
     * @param tableName
     * @param tableOptions
     * @param createTableOptions
     * @param rowClass
     *      entity ANNOTATED
     * @return
     * @param <T>
     */
    public <T> Table<T> createTable(String tableName,
       CreateTableOptions createTableOptions,
       Class<T> rowClass,
       TableOptions tableOptions) {
        hasLength(tableName, "tableName");
        notNull(rowClass, "rowClass");
        // FIX ME INVESTIGATING TO CREATE A TABLE DEFINITION OBJECT
        Command createTable = new Command("createTable", createTableCommand(tableName, rowClass));
        if (createTableOptions != null) {
            createTable.append("options", createTableOptions);
        }
        runCommand(createTable, createTableOptions);
        log.info("Table  '" + green("{}") + "' has been created", tableName);
        return getTable(tableName, tableOptions, rowClass);
    }

    /**
     * Create a new table with the selected options
     *
     * @param tableName
     *      the definition for the new table to create
     * @param tableDefinition
     *      the definition for the new table to create
     * @param creatTableOptions
     *      various options for creating the table
     * @param rowClass
     *     the default class to cast any row returned from the database into.
     * @param tableOptions
     *      options to use when using this collection
     * @param <T>
     *          working class for the document
     * @return the collection
     */
    public <T> Table<T> createTable(String tableName,
              TableDefinition tableDefinition,
              CreateTableOptions creatTableOptions,
              Class<T> rowClass,
              TableOptions tableOptions) {
        hasLength(tableName, "tableName");
        notNull(tableDefinition, "tableDefinition");
        notNull(rowClass, "rowClass");
        Command createTable = Command
                .create("createTable")
                .append("name", tableName)
                .append("definition", tableDefinition);
        if (creatTableOptions != null) {
            createTable.append("options", creatTableOptions);
        }
        runCommand(createTable, tableOptions);
        log.info("Table  '" + green("{}") + "' has been created", tableName);
        return getTable(tableName, tableOptions, rowClass);
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
        dropTable(tableName, new DropTableOptions());
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
        runCommand(dropTableCmd, dropTableOptions);
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
        runCommand(dropIndexCommand, dropIndexOptions);
        log.info("Index  '" + green("{}") + "' has been dropped", indexName);
    }

    // ------------------------------------------
    // ----    Generation Information        ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public String getApiEndpoint() {
       return this.apiEndpoint;
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
        this.options.registerObserver(logger, commandObserver);
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link BaseOptions}.
     *
     * @param name
     *      name for the observer
     */
    public void deleteListener(String name) {
        this.options.unregisterObserver(name);
    }

}
