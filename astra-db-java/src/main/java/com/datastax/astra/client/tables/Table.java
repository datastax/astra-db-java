package com.datastax.astra.client.tables;

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

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.collections.commands.FindOneOptions;
import com.datastax.astra.client.collections.commands.InsertManyOptions;
import com.datastax.astra.client.collections.commands.InsertManyResult;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.commands.CommandOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.exception.DataAPIException;
import com.datastax.astra.client.tables.commands.TableDeleteOneOptions;
import com.datastax.astra.client.tables.commands.TableDeleteResult;
import com.datastax.astra.client.tables.commands.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.TableInsertManyResult;
import com.datastax.astra.client.tables.commands.TableInsertOneOptions;
import com.datastax.astra.client.tables.commands.TableInsertOneResult;
import com.datastax.astra.client.tables.index.IndexDefinition;
import com.datastax.astra.client.tables.index.IndexOptions;
import com.datastax.astra.client.tables.index.VectorIndexDefinition;
import com.datastax.astra.client.tables.index.VectorIndexOptions;
import com.datastax.astra.client.tables.row.PrimaryKey;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.datastax.astra.client.exception.DataAPIException.ERROR_CODE_INTERRUPTED;
import static com.datastax.astra.client.exception.DataAPIException.ERROR_CODE_TIMEOUT;
import static com.datastax.astra.internal.utils.AnsiUtils.cyan;
import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.AnsiUtils.magenta;
import static com.datastax.astra.internal.utils.AnsiUtils.yellow;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Execute commands against tables
 */
@Slf4j
public class Table<T>  extends AbstractCommandRunner {

    /** parameters names. */
    private static final String ARG_TABLE_NAME = "tableName";

    /** parameters names. */
    private static final String ROW = "row";

    /** Serializer for the table. */
    private static final RowSerializer SERIALIZER = new RowSerializer();

    // -- Json Outputs

    /** parameters names. */
    private static final String RESULT_INSERTED_IDS = "insertedIds";

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
    public Table(Database db, String tableName, CommandOptions<?> commandOptions, Class<T> clazz) {
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
    public TableDefinition getDefinition() {
        return database
                .listTables()
                .filter(col -> col.getName().equals(tableName))
                .findFirst()
                .map(TableDescriptor::getDefinition)
                .orElseThrow(() -> new DataAPIException("[TABLE_NOT_EXIST] - Table does not exist, " +
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
    protected DataAPISerializer getSerializer() {
        return SERIALIZER;
    }

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return apiEndpoint;
    }

    // --------------------------
    // ---       INDEX       ----
    // --------------------------

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      name of the index
     * @param idxDefinition
     *      definition of the index
     */
    public void createIndex(String idxName, IndexDefinition idxDefinition) {
        createIndex(idxName, idxDefinition, null, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      name of the index
     * @param idxDefinition
     *      definition of the index
     * @param options
     *      index options
     */
    public void createIndex(String idxName, IndexDefinition idxDefinition, IndexOptions options) {
        createIndex(idxName, idxDefinition, options, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      name of the index
     * @param idxDefinition
     *      definition of the index
     * @param idxOptions
     *      index options
     * @param cmd
     *      override the default command options
     */
    public void createIndex(String idxName, IndexDefinition idxDefinition, IndexOptions idxOptions, CommandOptions<?> cmd) {
        hasLength(idxName, "indexName");
        notNull(idxDefinition, "idxDefinition");
        Command createIndexCommand = Command
                .create("createIndex")
                .append("name", idxName)
                .append("definition", idxDefinition);
        if (idxOptions != null) {
            createIndexCommand.append("options", idxOptions);
        }
        runCommand(createIndexCommand, commandOptions);
        log.info("Index  '" + green("{}") + "' has been created", idxName);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      name of the index
     * @param idxDefinition
     *      definition of the index
     */
    public void createVectorIndex(String idxName, VectorIndexDefinition idxDefinition) {
        createVectorIndex(idxName, idxDefinition, null, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      index name
     * @param idxDefinition
     *      definition of the index
     * @param options
     *      index options
     */
    public void createVectorIndex(String idxName, VectorIndexDefinition idxDefinition, VectorIndexOptions options) {
        createVectorIndex(idxName, idxDefinition, options, commandOptions);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      index name
     * @param idxDefinition
     *      definition of the index
     * @param idxOptions
     *      index options
     * @param cmd
     *      override the default command options
     */
    public void createVectorIndex(String idxName, VectorIndexDefinition idxDefinition, VectorIndexOptions idxOptions, CommandOptions<?> cmd) {
        hasLength(idxName, "indexName");
        notNull(idxDefinition, "idxDefinition");
        Command createIndexCommand = Command
                .create("createVectorIndex")
                .append("name", idxName)
                .append("definition", idxDefinition);
        if (idxOptions != null) {
            createIndexCommand.append("options", idxOptions);
        }
        runCommand(createIndexCommand, commandOptions);
        log.info("Vector Index '" + green("{}") + "' has been created",idxName);
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
    // ---   insertOne       ----
    // --------------------------

    public final TableInsertOneResult insertOne(T row) {
        Row targetRow;
        if (row instanceof Row) {
            targetRow = (Row) row;
        } else {
            // Mapping as a Row
            targetRow = SERIALIZER.convertValue(row, Row.class);
        }
        return insertOneDelegate(targetRow, (TableInsertOneOptions) null);
    }

    public final TableInsertOneResult insertOne(T row, TableInsertOneOptions insertOneOptions) {
        notNull(row, ROW);
        Command insertOne = Command.create("insertOne").withDocument(row);
        return runCommand(insertOne, insertOneOptions).getStatus(TableInsertOneResult.class);
    }

    public final CompletableFuture<TableInsertOneResult> insertOneAsync(T row) {
        return CompletableFuture.supplyAsync(() -> insertOne(row));
    }

    public final CompletableFuture<TableInsertOneResult> insertOneAsync(T row, TableInsertOneOptions options) {
        return CompletableFuture.supplyAsync(() -> insertOne(row, options));
    }

    private TableInsertOneResult insertOneDelegate(Row row, TableInsertOneOptions insertOneOptions) {
        notNull(row, ROW);
        Command insertOne = Command
                .create("insertOne")
                .withDocument(row);
        TableInsertManyResult result = runCommand(insertOne, insertOneOptions).getStatus(TableInsertManyResult.class);
        return new TableInsertOneResult(result.getInsertedIds().get(0), result.getPrimaryKeySchema());
    }

    // --------------------------
    // ---   insertMany      ----
    // --------------------------

    public TableInsertManyResult insertMany(List<? extends T> rows) {
        return insertMany(rows, new TableInsertManyOptions());
    }

    public TableInsertManyResult insertMany(List<? extends T> rows, TableInsertManyOptions options) {
        Assert.isTrue(rows != null && !rows.isEmpty(), "rows list cannot be null or empty");
        Assert.notNull(options, "insertMany options cannot be null");
        if (options.getConcurrency() > 1 && options.isOrdered()) {
            throw new IllegalArgumentException("Cannot run ordered insert_many concurrently.");
        }
        if (options.getChunkSize() > dataAPIOptions.getMaxDocumentsInInsert()) {
            throw new IllegalArgumentException("Cannot insert more than " + dataAPIOptions.getMaxDocumentsInInsert() + " at a time.");
        }
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
        List<Future<TableInsertManyResult>> futures = new ArrayList<>();
        for (int i = 0; i < rows.size(); i += options.getChunkSize()) {
            futures.add(executor.submit(getInsertManyResultCallable(rows, options, i)));
        }
        executor.shutdown();

        // Grouping All Insert ids in the same list.
        TableInsertManyResult finalResult = new TableInsertManyResult();
        try {
            for (Future<TableInsertManyResult> future : futures) {
                finalResult.getInsertedIds().addAll(future.get().getInsertedIds());
            }

            if (executor.awaitTermination(options.getTimeout(), TimeUnit.MILLISECONDS)) {
                log.debug(magenta(".[total insertMany.responseTime]") + "=" + yellow("{}") + " millis.",
                        System.currentTimeMillis() - start);
            } else {
                throw new DataAPIException(ERROR_CODE_TIMEOUT, "Request did not complete withing ");
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof DataAPIException) {
                throw (DataAPIException) e.getCause();
            }
            Thread.currentThread().interrupt();
            throw new DataAPIException(ERROR_CODE_INTERRUPTED, "Thread was interrupted while waiting", e);
        }
        return finalResult;
    }

    @SafeVarargs
    public final TableInsertManyResult insertMany(T... rows) {
        return insertMany(Arrays.asList(rows), new TableInsertManyOptions());
    }

    public CompletableFuture<TableInsertManyResult > insertManyAsync(List<? extends T> rows) {
        return CompletableFuture.supplyAsync(() -> insertMany(rows));
    }

    /**
     * Execute a 1 for 1 call to the Data API.
     *
     * @param rows
     *      list of rows to be inserted
     * @param insertManyOptions
     *      options for insert many (chunk size and insertion order).
     * @param start
     *      offset in global list
     * @return
     *      insert many result for a paged call
     */
    private Callable<TableInsertManyResult> getInsertManyResultCallable(List<? extends T> rows, TableInsertManyOptions insertManyOptions, int start) {
        int end = Math.min(start + insertManyOptions.getChunkSize(), rows.size());
        return () -> {
            log.debug("Insert block (" + cyan("size={}") + ") in table {}", end - start, green(getTableName()));
            Command insertMany = new Command("insertMany")
                    .withDocuments(rows.subList(start, end))
                    .withOptions(new Document().append(INPUT_ORDERED, insertManyOptions.isOrdered()));
            return runCommand(insertMany, insertManyOptions).getStatus(TableInsertManyResult.class);
        };
    }

    // --------------------------
    // ---   findOne         ----
    // --------------------------

    public Optional<T> findOne(Filter filter) {
        return findOne(filter, new TableFindOneOptions());
    }

    public Optional<T> findOne(Filter filter, TableFindOneOptions findOneOptions) {
        notNull(findOneOptions, ARG_OPTIONS);
        Command findOne = Command
                .create("findOne")
                .withFilter(filter)
                .withSort(findOneOptions.getSort())
                .withProjection(findOneOptions.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull(INPUT_INCLUDE_SIMILARITY, findOneOptions.getIncludeSimilarity())
                        .appendIfNotNull(INPUT_INCLUDE_SORT_VECTOR, findOneOptions.getIncludeSortVector())
                );

        return Optional.ofNullable(
                runCommand(findOne, findOneOptions)
                        .getData().getDocument()
                        .map(getRowClass()));
    }

    public Optional<T> findOne(TableFindOneOptions findOneOptions) {
        return findOne(null, findOneOptions);
    }

    public CompletableFuture<Optional<T>> findOneASync(Filter filter) {
        return CompletableFuture.supplyAsync(() -> findOne(filter));
    }

    public CompletableFuture<Optional<T>> findOneASync(Filter filter, TableFindOneOptions findOneOptions) {
        return CompletableFuture.supplyAsync(() -> findOne(filter, findOneOptions));
    }

    public Optional<T> findByPrimaryKey(PrimaryKey id) {
        return findOne(Filters.eq(id));
    }

    // -------------------------
    // ---   DeleteOne*     ----
    // -------------------------

    /**
     * Removes at most one document from the collection that matches the given filter.
     * If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @return
     *      the result of the remove one operation
     *
     */
    public TableDeleteResult deleteOne(Filter filter) {
        return deleteOne(filter, new TableDeleteOneOptions());
    }

    /**
     * Removes at most one document from the collection that matches the given filter.
     * If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @param deleteOneOptions
     *      the option to driver the deletes (here sort)
     * @return
     *      the result of the remove one operation
     *
     */
    public TableDeleteResult deleteOne(Filter filter, TableDeleteOneOptions deleteOneOptions) {
        Command deleteOne = Command
                .create("deleteOne")
                .withFilter(filter)
                .withSort(deleteOneOptions.getSort());

        ApiResponse apiResponse = runCommand(deleteOne, deleteOneOptions);
        int deletedCount = apiResponse.getStatus().getInteger(RESULT_DELETED_COUNT);
        return new TableDeleteResult(deletedCount);
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
