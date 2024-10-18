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

import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.InsertOneOptions;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.collections.Document;
import com.datastax.astra.client.model.command.Command;
import com.datastax.astra.client.model.command.CommandOptions;
import com.datastax.astra.client.model.tables.TableDefinition;
import com.datastax.astra.client.model.tables.TableDescriptor;
import com.datastax.astra.client.model.tables.TableOptions;
import com.datastax.astra.client.model.tables.index.IndexDefinition;
import com.datastax.astra.client.model.tables.index.IndexDescriptor;
import com.datastax.astra.client.model.tables.index.IndexOptions;
import com.datastax.astra.client.model.tables.index.VectorIndexDescriptor;
import com.datastax.astra.client.model.tables.index.VectorIndexOptions;
import com.datastax.astra.client.model.tables.row.Row;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Execute commands against tables
 */
@Slf4j
public class Table<T>  extends AbstractCommandRunner {

    /** parameters names. */
    private static final String ARG_DATABASE = "database";
    /** parameters names. */
    private static final String ARG_OPTIONS = "options";
    /** parameters names. */
    private static final String ARG_CLAZZ = "working class 'clazz'";
    /** parameters names. */
    private static final String ARG_TABLE_NAME = "collectionName";
    /** Collection identifier. */
    @Getter
    private final String tableName;

    /** Working class representing documents of the collection. The default value is {@link Document}. */
    @Getter
    protected final Class<T> rowClass;

    /** Parent Database reference.  */
    @Getter
    private final Database database;

    /** Get global Settings for the client. */
    @Getter
    private final DataAPIOptions dataAPIOptions;

    /** Api Endpoint for the Database, if using an astra environment it will contain the database id and the database region.  */
    private final String apiEndpoint;


    /**
     * Constructs an instance of a table within the specified database. This constructor
     * initializes the table with a given name and associates it with a specific class type
     * that represents the schema of documents within the table. This setup is designed for
     * CRUD (Create, Read, Update, Delete) operations.
     *
     * @param db The {@code Database} instance representing the client's keyspace for HTTP
     *           communication with the database. It encapsulates the configuration and management
     *           of the database connection, ensuring that operations on this collection are
     *           executed within the context of this database.
     * @param tableName A {@code String} that uniquely identifies the table within the
     *                       database. This name is used to route operations to the correct
     *                       table and should adhere to the database's naming conventions.
     * @param clazz The {@code Class<DOC>} object that represents the model for rows within
     *              this table. This class is used for serialization and deserialization of
     *              rows to and from the database. It ensures type safety and facilitates
     *              the mapping of database documents to Java objects.
     * @param commandOptions the options to apply to the command operation. If left blank the default table
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a client
     * DataAPIClient client = new DataAPIClient("token");
     * // Given a database
     * Database myDb = client.getDatabase("myDb");
     * // Initialize a collection with a working class
     * Table<MyDocumentClass> myTable = new Table<>(myDb, "myTableName", MyDocumentClass.class);
     * }
     * </pre>
     */
    protected Table(Database db, String tableName, CommandOptions<?> commandOptions, Class<T> clazz) {
        notNull(db, ARG_DATABASE);
        notNull(clazz, ARG_CLAZZ);
        hasLength(tableName, ARG_TABLE_NAME);
        this.tableName      = tableName;
        this.database       = db;
        this.dataAPIOptions = db.getOptions();
        this.rowClass       = clazz;
        this.commandOptions = commandOptions;
        this.apiEndpoint    = db.getApiEndpoint() + "/" + tableName;
    }

    // ----------------------------
    // --- Global Information ----
    // ----------------------------

    /**
     * Retrieves the name of the parent keyspace associated with this collection. A keyspace in
     * this context typically refers to a higher-level categorization or grouping mechanism within
     * the database that encompasses one or more collections. This method allows for identifying
     * the broader context in which this collection exists, which can be useful for operations
     * requiring knowledge of the database structure or for dynamic database interaction patterns.
     *
     * @return A {@code String} representing the name of the parent keyspace of the current
     *         collection. This name serves as an identifier for the keyspace and can be used
     *         to navigate or query the database structure.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection myCollection = ... // assume myCollection is already initialized
     * String keyspaceName = myCollection.getKeyspaceName();
     * System.out.println("The collection belongs to the keyspace: " + namespaceName);
     * }
     * </pre>
     */
    public String getKeyspaceName() {
        return getDatabase().getKeyspaceName();
    }

    /**
     * Retrieves the full definition of the collection, encompassing both its name and its configuration options.
     * This comprehensive information is encapsulated in a {@code CollectionInfo} object, providing access to the
     * collection's metadata and settings.
     *
     * <p>The returned {@code CollectionInfo} includes details such as the collection's name, which serves as its
     * unique identifier within the database, and a set of options that describe its configuration. These options
     * may cover aspects like indexing preferences, read/write permissions, and other customizable settings that
     * were specified at the time of collection creation.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a collection
     * Table<Row> table;
     * // Access its Definition
     * TableDefinition definition = table.getDefinition();
     * System.out.println("Name=" + definition.getName());
     * if (options != null) {
     *   // Operations based on collection options
     * }
     * }
     * </pre>
     *
     * @return A {@code CollectionInfo} object containing the full definition of the collection, including its name
     *         and configuration options. This object provides a comprehensive view of the collection's settings
     *         and identity within the database.
     */
    public TableDescriptor getDefinition() {
        return database
                .listTables()
                .filter(col -> col.getName().equals(tableName))
                .findFirst()
                .orElseThrow(() -> new DataApiException("[TABLE_NOT_EXIST] - Table does not exist, " +
                        "table name: '" + tableName + "'", "TABLE_NOT_EXIST", null));
    }

    /**
     * Retrieves the name of the table. This name serves as a unique identifier within the database and is
     * used to reference the collection in database operations such as queries, updates, and deletions. The table
     * name is defined at the time of table creation and is immutable.
     *
     * @return A {@code String} representing the name of the table. This is the same name that was specified
     *         when the table was created or initialized.
     */
    public String getName() {
        return tableName;
    }

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return apiEndpoint;
    }

    // --------------------------
    // ---       INDICES     ----
    // --------------------------

    /**
     * Create a new index with the given description.
     *
     * @param idxDescriptor
     *      definition of the index
     */
    public void createIndex(IndexDescriptor idxDescriptor) {
        createIndex(idxDescriptor, null, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxDescriptor
     *      definition of the index
     * @param options
     *      index options
     */
    public void createIndex(IndexDescriptor idxDescriptor, IndexOptions options) {
        createIndex(idxDescriptor, options, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxDescriptor
     *      definition of the index
     * @param idxOptions
     *      index options
     * @param cmd
     *      override the default command options
     */
    public void createIndex(IndexDescriptor idxDescriptor, IndexOptions idxOptions, CommandOptions<?> cmd) {
        notNull(idxDescriptor, "idxDescriptor");
        notNull(idxDescriptor.getDefinition(), "indexDefinition");
        hasLength(idxDescriptor.getName(), "indexName");
        Command createIndexCommand = Command
                .create("createIndex")
                .append("name", idxDescriptor.getName())
                .append("definition", idxDescriptor.getDefinition())
                .append("options", idxOptions);
        runCommand(createIndexCommand, commandOptions);
        log.info("Index  '" + green("{}") + "' has been created", idxDescriptor.getName());
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxDescriptor
     *      definition of the index
     */
    public void createVectorIndex(VectorIndexDescriptor idxDescriptor) {
        createVectorIndex(idxDescriptor, null, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxDescriptor
     *      definition of the index
     * @param options
     *      index options
     */
    public void createVectorIndex(VectorIndexDescriptor idxDescriptor, VectorIndexOptions options) {
        createVectorIndex(idxDescriptor, options, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxDescriptor
     *      definition of the index
     * @param idxOptions
     *      index options
     * @param cmd
     *      override the default command options
     */
    public void createVectorIndex(VectorIndexDescriptor idxDescriptor, VectorIndexOptions idxOptions, CommandOptions<?> cmd) {
        notNull(idxDescriptor, "idxDescriptor");
        notNull(idxDescriptor.getDefinition(), "indexDefinition");
        hasLength(idxDescriptor.getName(), "indexName");
        Command createIndexCommand = Command
                .create("createVectorIndex")
                .append("name", idxDescriptor.getName())
                .append("definition", idxDescriptor.getDefinition())
                .append("options", idxOptions);
        runCommand(createIndexCommand, commandOptions);
        log.info("Vector Index '" + green("{}") + "' has been created", idxDescriptor.getName());
    }


    /**
     * Delete an index by name.
     *
     * @param indexName
     *      index name
     */
    public void dropIndex(String indexName) {
        Command dropIndexCommand = Command.create("dropIndex").append("indexName", indexName);
        runCommand(dropIndexCommand, commandOptions);
        log.info("Index  '" + green("{}") + "' has been dropped", indexName);
    }

    // --------------------------
    // ---   Insert*         ----
    // --------------------------

    //FIXME: Add the InsertOneResult
    public final InsertOneResult insertOne(T row) {
        return null;
    }

    //FIXME: Add the InsertOneResult
    public final InsertOneResult insertOne(T row, InsertOneOptions insertOneOptions) {
        return null;
    }

    // --------------------------
    // ---   Listeners       ----
    // --------------------------

    /**
     * Register a listener to execute commands on the collection. Please now use {@link CommandOptions}.
     *
     * @param logger
     *      name for the logger
     * @param commandObserver
     *      class for the logger
     */
    public void registerListener(String logger, CommandObserver commandObserver) {
        this.commandOptions.registerObserver(logger, commandObserver);
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link CommandOptions}.
     *
     * @param name
     *      name for the observer
     */
    public void deleteListener(String name) {
        this.commandOptions.unregisterObserver(name);
    }

}
