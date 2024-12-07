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

import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.tables.commands.AlterTableOperation;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.AlterTableOptions;
import com.datastax.astra.client.tables.commands.options.CountRowsOptions;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.EstimatedCountRowsOptions;
import com.datastax.astra.client.tables.commands.options.ListIndexesOptions;
import com.datastax.astra.client.tables.commands.options.TableDeleteManyOptions;
import com.datastax.astra.client.tables.commands.options.TableDeleteOneOptions;
import com.datastax.astra.client.tables.commands.options.TableDistinctOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.commands.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.options.TableInsertOneOptions;
import com.datastax.astra.client.tables.commands.options.TableUpdateOneOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.commands.results.TableInsertOneResult;
import com.datastax.astra.client.tables.cursor.TableCursor;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDescriptor;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.exceptions.TooManyRowsToCountException;
import com.datastax.astra.internal.api.DataAPIData;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.tables.RowMapper;
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
import java.util.stream.Collectors;

import static com.datastax.astra.client.core.DataAPIKeywords.SORT_VECTOR;
import static com.datastax.astra.client.core.options.DataAPIClientOptions.MAX_CHUNK_SIZE;
import static com.datastax.astra.client.core.options.DataAPIClientOptions.MAX_COUNT;
import static com.datastax.astra.client.exceptions.DataAPIException.ERROR_CODE_INTERRUPTED;
import static com.datastax.astra.client.exceptions.DataAPIException.ERROR_CODE_TIMEOUT;
import static com.datastax.astra.internal.utils.AnsiUtils.cyan;
import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.AnsiUtils.magenta;
import static com.datastax.astra.internal.utils.AnsiUtils.yellow;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Executes commands and operations on tables.
 *
 * <p>The {@code Table} class is designed to work with table entities of type {@code T}, where
 * {@code T} represents the data model or schema associated with the table.</p>
 *
 * @param <T> the type of the table entity, representing the data model or schema
 */
@Slf4j
public class Table<T>  extends AbstractCommandRunner<TableOptions> {

    /** Avoid duplicating for each operation if not override. */
    public static final DataAPISerializer DEFAULT_TABLE_SERIALIZER = new RowSerializer();

    /** table identifier. */
    @Getter
    private final String tableName;

    /** Working class representing rows of the table. The default value is {@link Row}. */
    @Getter
    protected final Class<T> rowClass;

    /** Parent Database reference.  */
    @Getter
    private final Database database;

    /**
     * Collection definition loaded once.
     */
    private CollectionDefinition collectionDefinition;

    /**
     * Constructs an instance of a table within the specified database. This constructor
     * initializes the table with a given name and associates it with a specific class type
     * that represents the schema of rows within the table. This setup is designed for
     * CRUD (Create, Read, Update, Delete) operations.
     *
     * @param db The {@code Database} instance representing the client's keyspace for HTTP
     *           communication with the database. It encapsulates the configuration and management
     *           of the database connection, ensuring that operations on this table are
     *           executed within the context of this database.
     * @param tableName A {@code String} that uniquely identifies the table within the
     *                       database. This name is used to route operations to the correct
     *                       table and should adhere to the database's naming conventions.
     * @param rowClass The {@code Class<DOC>} object that represents the model for rows within
     *              this table. This class is used for serialization and deserialization of
     *              rows to and from the database. It ensures type safety and facilitates
     *              the mapping of database rows to Java objects.
     * @param tableOptions the options to apply to the command operation. If left blank the default table
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a client
     * DataAPIClient client = new DataAPIClient("token");
     * // Given a database
     * Database myDb = client.getDatabase("myDb");
     * // Initialize a table with a working class
     * Table<MyRowClass> myTable = new Table<>(myDb, "myTableName", MyRowClass.class);
     * }
     * </pre>
     */
    public Table(Database db, String tableName, TableOptions tableOptions, Class<T> rowClass) {
        super(db.getApiEndpoint() + "/" + tableName, tableOptions);
        hasLength(tableName, "collection name");
        notNull(rowClass, "rowClass");
        notNull(tableOptions, "table options");
        this.tableName = tableName;
        this.database  = db;
        this.rowClass  = rowClass;
        this.options.serializer(DEFAULT_TABLE_SERIALIZER);
        if (tableOptions.getToken() == null) {
            this.options.token(db.getOptions().getToken());
        }
        if (tableOptions.getDataAPIClientOptions() == null) {
            this.options.dataAPIClientOptions(db.getOptions().getDataAPIClientOptions()).clone();
        }
        if (tableOptions.getKeyspace() != null) {
            this.database.useKeyspace(tableOptions.getKeyspace());
        }
    }

    // ----------------------------
    // --- Global Information ----
    // ----------------------------

    /**
     * Retrieves the name of the parent keyspace associated with this table. A keyspace in
     * this context typically refers to a higher-level categorization or grouping mechanism within
     * the database that encompasses one or more tables. This method allows for identifying
     * the broader context in which this table exists, which can be useful for operations
     * requiring knowledge of the database structure or for dynamic database interaction patterns.
     *
     * @return A {@code String} representing the name of the parent keyspace of the current
     *         table. This name serves as an identifier for the keyspace and can be used
     *         to navigate or query the database structure.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Table myTable = ... // assume myTable is already initialized
     * String keyspaceName = myTable.getKeyspaceName();
     * System.out.println("The table belongs to the keyspace: " + keyspaceName);
     * }
     * </pre>
     */
    public String getKeyspaceName() {
        return getDatabase().getKeyspace();
    }

    /**
     * Retrieves the full definition of the table, encompassing both its name and its configuration options.
     * This comprehensive information is encapsulated in a {@code TableDefinition} object, providing access to the
     * table's metadata and settings.
     *
     * <p>The returned {@code TableDefinition} includes details such as the table's name, which serves as its
     * unique identifier within the database, and a set of options that describe its configuration. These options
     * may cover aspects like indexing preferences, read/write permissions, and other customizable settings that
     * were specified at the time of table creation.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a table
     * Table<Row> table;
     * // Access its Definition
     * TableDefinition definition = table.getDefinition();
     * System.out.println("Name=" + definition.getName());
     * if (options != null) {
     *   // Operations based on table options
     * }
     * }
     * </pre>
     *
     * @return A {@code TableDefinition} object containing the full definition of the table, including its name
     *         and configuration options. This object provides a comprehensive view of the table's settings
     *         and identity within the database.
     */
    public TableDefinition getDefinition() {
        return database
                .listTables().stream()
                .filter(col -> col.getName().equals(tableName))
                .findFirst()
                .map(TableDescriptor::getDefinition)
                .orElseThrow(() -> new DataAPIException("[TABLE_NOT_EXIST] - Table does not exist, " +
                        "table name: '" + tableName + "'", "TABLE_NOT_EXIST", null));
    }

    /**
     * Retrieves the name of the table. This name serves as a unique identifier within the database and is
     * used to reference the table in database operations such as queries, updates, and deletions. The table
     * name is defined at the time of table creation and is immutable.
     *
     * @return A {@code String} representing the name of the table. This is the same name that was specified
     *         when the table was created or initialized.
     */
    public String getName() {
        return tableName;
    }

    // --------------------------
    // ---    alterTable     ----
    // --------------------------

    /**
     * Performs an alteration operation on the table with default options.
     *
     * <p>This method delegates to {@link #alter(AlterTableOperation, AlterTableOptions)}
     * with {@code options} set to {@code null}.
     *
     * @param operation the alteration operation to be performed; must not be {@code null}.
     * @return a new {@link Table} instance representing the altered table.
     */
    public final Table<T> alter(AlterTableOperation operation) {
        return alter(operation, null);
    }

    /**
     * Performs an alteration operation on the table with the specified options.
     *
     * <p>This method delegates to {@link #alter(AlterTableOperation, AlterTableOptions, Class)}
     * using the row class of the current table.
     *
     * @param operation the alteration operation to be performed; must not be {@code null}.
     * @param options   the options for the alteration operation; may be {@code null}.
     * @return a new {@link Table} instance representing the altered table.
     */
    public final Table<T> alter(AlterTableOperation operation, AlterTableOptions options) {
        return alter(operation, options, getRowClass());
    }

    /**
     * Performs an alteration operation on the table with the specified options and row class.
     *
     * <p>This is the most granular method for altering a table. It builds and executes the command
     * to perform the specified alteration operation, with optional parameters and custom row class.
     *
     * @param operation the alteration operation to be performed; must not be {@code null}.
     * @param options   the options for the alteration operation; may be {@code null}.
     * @param clazz     the class representing the row type for the altered table; must not be {@code null}.
     * @param <R>       the type of the rows in the altered table.
     * @return a new {@link Table} instance of the specified row class representing the altered table.
     * @throws NullPointerException if {@code operation} or {@code clazz} is {@code null}.
     */
    public final <R> Table<R> alter(AlterTableOperation operation, AlterTableOptions options, Class<R> clazz) {
        notNull(operation, "operation");
        Command alterTable = Command.create("alterTable")
                .append("operation", new Document().append(operation.getOperationName(), operation));
        if (options != null) {
            alterTable.append("options", options);
        }
        runCommand(alterTable, this.options);
        return new Table<>(database, tableName, this.options, clazz);
    }

    // --------------------------
    // ---    createIndex    ----
    // --------------------------

    /**
     * Create a simple index on the given column with no special options
     *
     * @param idxName
     *      name of the index
     * @param columnName
     *      column on which is the index
     */
    public void createIndex(String idxName, String columnName) {
        createIndex(idxName, new TableIndexDefinition().column(columnName), null);
    }
    /**
     * Create a simple index on the given column with no special options
     *
     * @param idxName
     *      name of the index
     * @param columnName
     *      column on which is the index
     * @param idxOptions
     *      index options
     */
    public void createIndex(String idxName, String columnName, CreateIndexOptions idxOptions) {
       createIndex(idxName, new TableIndexDefinition().column(columnName), idxOptions);
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
     */
    public void createIndex(String idxName, TableIndexDefinition idxDefinition, CreateIndexOptions idxOptions) {
        hasLength(idxName, "indexName");
        notNull(idxDefinition, "idxDefinition");
        Command createIndexCommand = Command
                .create("createIndex")
                .append("name", idxName)
                .append("definition", idxDefinition);
        if (idxOptions != null) {
            createIndexCommand.append("options", idxOptions);
        }
        runCommand(createIndexCommand, idxOptions);
        log.info("Index  '" + green("{}") + "' has been created", idxName);
    }

    // --------------------------
    // --- createVectorIndex ----
    // --------------------------

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      name of the index
     * @param columnName
     *      name of the column
     */
    public void createVectorIndex(String idxName, String columnName) {
        Assert.hasLength(idxName, "indexName");
        Assert.hasLength(columnName, "columnName");
        createVectorIndex(idxName, new TableVectorIndexDefinition().column(columnName), null);
    }

    /**
     * Create a new index with the given description.
     *
     * @param idxName
     *      name of the index
     * @param idxDefinition
     *      definition of the index
     */
    public void createVectorIndex(String idxName, TableVectorIndexDefinition idxDefinition) {
        createVectorIndex(idxName, idxDefinition, null);
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
     */
    public void createVectorIndex(String idxName, TableVectorIndexDefinition idxDefinition, CreateVectorIndexOptions idxOptions) {
        hasLength(idxName, "indexName");
        notNull(idxDefinition, "idxDefinition");
        Command createIndexCommand = Command
                .create("createVectorIndex")
                .append("name", idxName)
                .append("definition", idxDefinition);
        if (idxOptions != null) {
            createIndexCommand.append("options", idxOptions);
        }
        runCommand(createIndexCommand, options);
        log.info("Vector Index '" + green("{}") + "' has been created",idxName);
    }

    // --------------------------
    // ---   insertOne       ----
    // --------------------------

    public final TableInsertOneResult insertOne(T row) {
        return insertOneDelegate(RowMapper.mapAsRow(row), null);
    }

    public final TableInsertOneResult insertOne(T row, TableInsertOneOptions insertOneOptions) {
        notNull(row, "row");
        Command insertOne = Command
                .create("insertOne")
                .withDocument(row);
        TableInsertManyResult result = runCommand(insertOne, insertOneOptions).getStatus(TableInsertManyResult.class);
        return new TableInsertOneResult(result.getInsertedIds().get(0), result.getPrimaryKeySchema());
    }

    public final CompletableFuture<TableInsertOneResult> insertOneAsync(T row) {
        return CompletableFuture.supplyAsync(() -> insertOne(row));
    }

    public final CompletableFuture<TableInsertOneResult> insertOneAsync(T row, TableInsertOneOptions options) {
        return CompletableFuture.supplyAsync(() -> insertOne(row, options));
    }

    private TableInsertOneResult insertOneDelegate(Row row, TableInsertOneOptions insertOneOptions) {
        notNull(row, "row");
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

    public TableInsertManyResult insertMany(List<? extends T> rows, TableInsertManyOptions insertManyOptions) {
        Assert.isTrue(rows != null && !rows.isEmpty(), "rows list cannot be null or empty");
        Assert.notNull(insertManyOptions, "insertMany options cannot be null");
        if (insertManyOptions.concurrency() > 1 && insertManyOptions.ordered()) {
            throw new IllegalArgumentException("Cannot run ordered insert_many concurrently.");
        }
        if (insertManyOptions.chunkSize() > MAX_CHUNK_SIZE) {
            throw new IllegalArgumentException("Cannot insert more than " + MAX_CHUNK_SIZE + " at a time.");
        }
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(insertManyOptions.concurrency());
        List<Future<TableInsertManyResult>> futures = new ArrayList<>();
        for (int i = 0; i < rows.size(); i += insertManyOptions.chunkSize()) {
            futures.add(executor.submit(getInsertManyResultCallable(rows, insertManyOptions, i)));
        }
        executor.shutdown();

        // Grouping All Insert ids in the same list.
        TableInsertManyResult finalResult = new TableInsertManyResult();
        try {
            boolean first = true;
            for (Future<TableInsertManyResult> future : futures) {
                TableInsertManyResult res = future.get();
                if (first) {
                    finalResult.setPrimaryKeySchema(res.getPrimaryKeySchema());
                    first = false;
                }
                if (!res.getInsertedIds().isEmpty()) {
                    finalResult.getInsertedIds().addAll(res.getInsertedIds());
                }
                if (!res.getDocumentResponses().isEmpty()) {
                    finalResult.getDocumentResponses().addAll(res.getDocumentResponses());
                }
            }

            long totalTimeout = this.options.getTimeout();
            if (options.getDataAPIClientOptions() != null
                    && options.getDataAPIClientOptions().getTimeoutOptions() != null) {
                totalTimeout = options.getTimeout();
            }
            if (executor.awaitTermination(totalTimeout, TimeUnit.MILLISECONDS)) {
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

    public TableInsertManyOptions insertManyOptions() {
        TableInsertManyOptions options = new TableInsertManyOptions();
        options.dataAPIClientOptions(this.options.getDataAPIClientOptions().clone());
        return options;
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
        int end = Math.min(start + insertManyOptions.chunkSize(), rows.size());
        return () -> {
            log.debug("Insert block (" + cyan("size={}") + ") in table {}", end - start, green(getTableName()));
            Command insertMany = new Command("insertMany")
                    .withDocuments(rows.subList(start, end))
                    .withOptions(new Document()
                            .append(INPUT_ORDERED, insertManyOptions.ordered())
                            .append(INPUT_RETURN_DOCUMENT_RESPONSES, insertManyOptions.returnDocumentResponses()));
            return runCommand(insertMany, insertManyOptions)
                    .getStatus(TableInsertManyResult.class);
        };
    }

    // --------------------------
    // ---   findOne         ----
    // --------------------------

    public <R> Optional<R> findOne(Filter filter, TableFindOneOptions findOneOptions, Class<R> newRowClass) {
        Command findOne = Command.create("findOne").withFilter(filter);
        if (findOneOptions != null) {
            findOne.withSort(findOneOptions.getSortArray())
                    .withProjection(findOneOptions.getProjectionArray())
                    .withOptions(new Document()
                                    .appendIfNotNull(INPUT_INCLUDE_SIMILARITY, findOneOptions.includeSimilarity())
                            // not exposed in FindOne
                            //.appendIfNotNull(INPUT_INCLUDE_SORT_VECTOR, findOneOptions.includeSortVector())
                    );
        }

        DataAPIData data = runCommand(findOne, findOneOptions).getData();

        return Optional
                .ofNullable(data.getDocument()
                        .map(Row.class))
                .map(row -> RowMapper.mapFromRow(row, getSerializer(), newRowClass));
    }

    public <R> Optional<R> findOne(Filter filter, Class<R> newRowClass) {
        return findOne(filter, null, newRowClass);
    }

    public Optional<T> findOne(Filter filter) {
        return findOne(filter, null, getRowClass());
    }

    public Optional<T> findOne(Filter filter, TableFindOneOptions findOneOptions) {
        return findOne(filter, findOneOptions, getRowClass());
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

    // -------------------------
    // ---   find           ----
    // -------------------------

    /**
     * Finds all rows in the table.
     *
     * @param filter
     *      the query filter
     * @param options
     *      options of find one
     * @return
     *      the Cursor to iterate over the results
     */
    public TableCursor<T, T> find(Filter filter, TableFindOptions options) {
        return new TableCursor<>(this, filter, options, getRowClass());
    }

    /**
     * Finds all rows in the table.
     *
     * @param filter
     *      the query filter
     * @param options
     *      options of find one
     * @return
     *      the Cursor to iterate over the results
     */
    public <R> TableCursor<T, R> find(Filter filter, TableFindOptions options, Class<R> newRowType) {
        return new TableCursor<>(this, filter, options, newRowType);
    }

    /**
     * Finds all rows in the table.
     *
     * @param filter
     *      the query filter
     * @return
     *      the Cursor to iterate over the results
     */
    public TableCursor<T, T> find(Filter filter) {
        return new TableCursor<>(this, filter, null, getRowClass());
    }

    /**
     * Finds all rows in the table.
     *
     * @param options
     *      options of find one
     * @return
     *      the Cursor to iterate over the results
     */
    public TableCursor<T, T> find(TableFindOptions options) {
        return new TableCursor<>(this, null, options, getRowClass());
    }

    /**
     * Retrieves all rows in the table.
     * <p>
     * This method returns an iterable interface that allows iterating over all rows in the table,
     * without applying any filters. It leverages the default {@link TableFindOptions} for query execution.
     * </p>
     *
     * @return A {@link TableCursor} for iterating over all rows in the table.
     */
    public TableCursor<T, T> findAll() {
        return find(null, null);
    }

    /**
     * Executes a paginated 'find' query on the table using the specified filter and find options.
     * <p>
     * This method constructs and executes a command to fetch a specific page of rows from the table that match
     * the provided filter criteria. It allows for detailed control over the query through {@code FindOptions}, such as sorting,
     * projection, pagination, and more. The result is wrapped in a {@link Page} object, which includes the rows found,
     * the page size, and the state for fetching subsequent pages.
     * </p>
     * <p>
     * Pagination is facilitated by the {@code skip}, {@code limit}, and {@code pageState} parameters within {@code FindOptions},
     * enabling efficient data retrieval in scenarios where the total dataset is too large to be fetched in a single request.
     * Optionally, similarity scoring can be included if {@code includeSimilarity} is set, which is useful for vector-based search queries.
     * </p>
     * <p>
     * The method processes the command's response, mapping each rows to the specified row class and collecting them into a list.
     * This list, along with the maximum page size and the next page state, is used to construct the {@link Page} object returned by the method.
     * </p>
     *
     * @param filter The filter criteria used to select rows from the table.
     * @param options The {@link CollectionFindOptions} providing additional query parameters, such as sorting and pagination.
     * @return A {@link Page} object containing the rows that match the query, along with pagination information.
     */
    public Page<T> findPage(Filter filter, TableFindOptions options) {
        Command findCommand = Command
                .create("find")
                .withFilter(filter);
        if (options != null) {
            findCommand
                    .withSort(options.getSortArray())
                    .withProjection(options.getProjectionArray())
                    .withOptions(new Document()
                            .appendIfNotNull("skip", options.skip())
                            .appendIfNotNull("limit", options.limit())
                            .appendIfNotNull(INPUT_PAGE_STATE, options.pageState())
                            .appendIfNotNull(INPUT_INCLUDE_SORT_VECTOR, options.includeSortVector())
                            .appendIfNotNull(INPUT_INCLUDE_SIMILARITY, options.includeSimilarity()));
        }
        DataAPIResponse apiResponse = runCommand(findCommand, options);

        // load sortVector if available
        float[] sortVector = null;
        if (options.includeSortVector() != null &&
                apiResponse.getStatus() != null &&
                apiResponse.getStatus().get(SORT_VECTOR.getKeyword()) != null) {
            sortVector = apiResponse.getStatus().get(SORT_VECTOR.getKeyword(), float[].class);
        }

        return new Page<>(
                apiResponse.getData().getNextPageState(),
                apiResponse.getData().getDocuments()
                        .stream()
                        .map(d -> d.map(getRowClass()))
                        .collect(Collectors.toList()), sortVector);
    }

    // -------------------------
    // ---   distinct       ----
    // -------------------------

    public <R> List<R> distinct(String fieldName, Filter filter, Class<R> resultClass) {
        return distinct(fieldName, filter, resultClass, null);
    }

    /**
     * Return a list of distinct values for the given field name.
     *
     * @param fieldName
     *      name of the field
     * @param filter
     *      filter to apply
     * @param resultClass
     *      class of the result
     * @return
     *     list of distinct values
     * @param <R>
     *     type of the result
     */
    public <R> List<R> distinct(String fieldName, Filter filter, Class<R> resultClass, TableDistinctOptions options) {
        Assert.hasLength(fieldName, "fieldName");
        Assert.notNull(resultClass, "resultClass");
        // Building a convenient find options
        TableFindOptions findOptions =  new TableFindOptions()
                .projection(Projection.include(fieldName));
        // Overriding options
        if (options != null && options.getDataAPIClientOptions() != null) {
            findOptions.dataAPIClientOptions(options.getDataAPIClientOptions());
        }
        // Exhausting the list of distinct values
        return find(filter, findOptions, Row.class).toList().stream()
                .map(row -> row.get(fieldName, resultClass))
                .distinct()
                .toList();
    }

    // -------------------------
    // ---   updateOne      ----
    // -------------------------

    /**
     * Update a single row in the table according to the specified arguments.
     *
     * @param filter
     *      a row describing the query filter, which may not be null.
     * @param update
     *      a row describing the update, which may not be null. The update to apply must include at least one update operator.
     */
    public void updateOne(Filter filter, TableUpdateOperation update) {
        updateOne(filter, update, new TableUpdateOneOptions());
    }

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include at least one update operator.
     * @param updateOptions
     *      the options to apply to the update operation
     */
    public void updateOne(Filter filter, TableUpdateOperation update, TableUpdateOneOptions updateOptions) {
        notNull(update, ARG_UPDATE);
        notNull(updateOptions, ARG_OPTIONS);
        Command cmd = Command
                .create("updateOne")
                .withFilter(filter)
                .withUpdate(update);
         runCommand(cmd, updateOptions);
    }

    // -------------------------
    // ---   deleteOne      ----
    // -------------------------

    /**
     * Removes at most one rows from the table that matches the given filter.
     * If no rows match, the table is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     */
    public void deleteOne(Filter filter) {
        deleteOne(filter, new TableDeleteOneOptions());
    }

    /**
     * Removes at most one row from the table that matches the given filter.
     * If no rows match, the table is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @param deleteOneOptions
     *      the option to driver the deletes (here sort)
     */
    public void deleteOne(Filter filter, TableDeleteOneOptions deleteOneOptions) {
        Command deleteOne = Command
                .create("deleteOne")
                .withFilter(filter)
                .withSort(deleteOneOptions.getSortArray());
        runCommand(deleteOne, deleteOneOptions);
        /*
        DataAPIResponse apiResponse = runCommand(deleteOne, deleteOneOptions);
        int deletedCount = apiResponse.getStatus().getInteger(RESULT_DELETED_COUNT);
        return new TableDeleteResult(deletedCount);
         */
    }

    // -------------------------
    // ---   deleteMany     ----
    // -------------------------

    /**
     * Removes all rows from the tables that match the given query filter. If no rows match, the table is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @param options
     *      the options to apply to the operation
     */
    public void deleteMany(Filter filter, TableDeleteManyOptions options) {
        Command deleteMany = Command
                .create("deleteMany")
                .withFilter(filter);
        runCommand(deleteMany, options);
    }

    /**
     * Removes all rows from the table that match the given query filter. If no rows match, the table is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     */
    public void deleteMany(Filter filter) {
        deleteMany(filter, null);
    }

    /**
     * Removes all rows from the table that match the given query filter. If no rows match, the table is not modified.
     */
    public void deleteAll() {
        deleteMany(new Filter());
    }

    /**
     * Delete the current table
     */
    public void drop() {
        getDatabase().dropTable(tableName);
    }

    // --------------------------------
    // --- estimatedDocumentCount  ----
    // --------------------------------

    /**
     * Calling an estimatedRowCount with default options. @see {@link #estimatedRowCount(EstimatedCountRowsOptions)}
     *
     * @return the estimated number of rows in the table.
     */
    public long estimatedRowCount() {
        return estimatedRowCount(new EstimatedCountRowsOptions());
    }

    /**
     * Executes the "estimatedRowCount" command to estimate the number of rowse
     * in a table.
     * <p>
     * This method sends a command to estimate the row count and parses the count
     * from the command's response. It handles the execution of the command and the extraction
     * of the row count from the response.
     * </p>
     * @param options
     *     the options to apply to the operation
     * @return the estimated number of rows in the table.
     */
    public long estimatedRowCount(EstimatedCountRowsOptions options) {
        Command command = new Command("estimatedDocumentCount");
        // Run command
        DataAPIResponse response = runCommand(command, options);
        // Build Result
        return response.getStatus().getInteger(RESULT_COUNT);
    }

    // ----------------------------
    // ---   countDocuments    ----
    // ----------------------------

    /**
     * Counts the number of rows in the table.
     *
     * <p>
     * Takes in a `upperBound` option which dictates the maximum number of rows that may be present before a
     * {@link com.datastax.astra.client.tables.exceptions.TooManyRowsToCountException} is thrown. If the limit is higher than the highest limit accepted by the
     * Data API, a {@link com.datastax.astra.client.tables.exceptions.TooManyRowsToCountException} will be thrown anyway (i.e. `1000`).
     * </p>
     * <p>
     * Count operations are expensive: for this reason, the best practice is to provide a reasonable `upperBound`
     * according to the caller expectations. Moreover, indiscriminate usage of count operations for sizeable amounts
     * of rows (i.e. in the thousands and more) is discouraged in favor of alternative application-specific
     * solutions. Keep in mind that the Data API has a hard upper limit on the amount of rows it will count,
     * and that an exception will be thrown by this method if this limit is encountered.
     * </p>
     *
     * @param upperBound
     *      The maximum number of rows to count.
     * @return
     *      The number of rows in the tables.
     * @throws com.datastax.astra.client.tables.exceptions.TooManyRowsToCountException
     *      If the number of rows counted exceeds the provided limit.
     */
    public int countRows(int upperBound) throws TooManyRowsToCountException {
        return countRows(null, upperBound);
    }

    /**
     * Counts the number of rows in the table with a filter.
     *
     * <p>
     * Takes in a `upperBound` option which dictates the maximum number of rows that may be present before a
     * {@link TooManyRowsToCountException} is thrown. If the limit is higher than the highest limit accepted by the
     * Data API, a {@link TooManyRowsToCountException} will be thrown anyway (i.e. `1000`).
     * </p>
     * <p>
     * Count operations are expensive: for this reason, the best practice is to provide a reasonable `upperBound`
     * according to the caller expectations. Moreover, indiscriminate usage of count operations for sizeable amounts
     * of rows (i.e. in the thousands and more) is discouraged in favor of alternative application-specific
     * solutions. Keep in mind that the Data API has a hard upper limit on the amount of rows it will count,
     * and that an exception will be thrown by this method if this limit is encountered.
     * </p>
     *
     * @param filter
     *      A filter to select the row to count. If not provided, all rows will be counted.
     * @param upperBound
     *      The maximum number of rows to count.
     * @param options
     *      overriding options for the count operation.
     * @return
     *      The number of rows in the table.
     * @throws TooManyRowsToCountException
     *      If the number of rows counted exceeds the provided limit.
     */
    public int countRows(Filter filter, int upperBound, CountRowsOptions options)
            throws TooManyRowsToCountException {
        // Argument Validation
        if (upperBound < 1 || upperBound > MAX_COUNT) {
            throw new IllegalArgumentException("UpperBound limit should be in between 1 and " + MAX_COUNT);
        }
        // Build command
        Command command = new Command("countDocuments").withFilter(filter);
        // Run command
        DataAPIResponse response = runCommand(command, options);
        // Build Result
        Boolean moreData = response.getStatus().getBoolean(RESULT_MORE_DATA);
        Integer count    = response.getStatus().getInteger(RESULT_COUNT);
        if (moreData != null && moreData) {
            throw new TooManyRowsToCountException();
        } else if (count > upperBound) {
            throw new TooManyRowsToCountException(upperBound);
        }
        return count;
    }

    /**
     * Implementation of the @see {@link #countRows(Filter, int, CountRowsOptions)} method with default options.
     * @param filter
     *      filter to count
     * @param upperBound
     *      The maximum number of rows to count. It must be lower than the maximum limit accepted by the Data API.
     * @return
     *      The number of rows in the table.
     * @throws TooManyRowsToCountException
     *      If the number of rows counted exceeds the provided limit.
     */
    public int countRows(Filter filter, int upperBound)
            throws TooManyRowsToCountException {
        return countRows(filter, upperBound, new CountRowsOptions());
    }

    // ------------------------------------------
    // ----   List Indexes                    ---
    // ------------------------------------------

    /**
     * Retrieves the names of all indices in the keyspace with default options.
     *
     * @return A list of all indices names in the database.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * List<String> indicesNames = listIndexesNames();
     * }
     * </pre>
     */
    public List<String> listIndexesNames() {
        return listIndexesNames(null);
    }

    /**
     * Retrieves the names of all indices in the keyspace with default options.
     *
     * @param listIndexesOptions Options for filtering or configuring the indices listing operation.
     * @return A list of all indices names in the database.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * ListIndexesOptions options = new ListIndexesOptions();
     * List<String> indicesNames = listIndexesNames(options);
     * }
     * </pre>
     */
    public List<String> listIndexesNames(ListIndexesOptions listIndexesOptions) {
        return runCommand(Command.create("listIndexes"), listIndexesOptions)
                .getStatusKeyAsList("indexes", String.class);
    }

    /**
     * Finds all the indices in the selected keyspace.
     *
     * @return
     *      list of table definitions
     */
    public List<TableIndexDescriptor> listIndexes() {
        return listIndexes(null);
    }

    /**
     * Finds all the indices in the selected keyspace.
     *
     * @return
     *      list of table definitions
     */
    public List<TableIndexDescriptor> listIndexes(ListIndexesOptions listIndexesOptions) {
        Command findTables = Command
                .create("listIndexes")
                .withOptions(new Document().append("explain", true));
        return runCommand(findTables, listIndexesOptions)
                .getStatusKeyAsList("indexes", TableIndexDescriptor.class);
    }

    // --------------------------
    // ---   Listeners       ----
    // --------------------------

    /**
     * Register a listener to execute commands on the table. Please now use {@link BaseOptions}.
     *
     * @param logger
     *      name for the logger
     * @param commandObserver
     *      class for the logger
     */
    public void registerListener(String logger, CommandObserver commandObserver) {
        this.options.registerObserver(logger, commandObserver);
    }

}
