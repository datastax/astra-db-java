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

import com.datastax.astra.client.collections.CollectionDefinition;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.commands.BaseOptions;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.paging.TableCursor;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.exception.DataAPIException;
import com.datastax.astra.client.tables.options.CountRowsOptions;
import com.datastax.astra.client.tables.options.EstimatedCountRowsOptions;
import com.datastax.astra.client.tables.options.TableDeleteManyOptions;
import com.datastax.astra.client.tables.options.TableDeleteOneOptions;
import com.datastax.astra.client.tables.options.TableFindOneOptions;
import com.datastax.astra.client.tables.options.TableFindOptions;
import com.datastax.astra.client.tables.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.results.TableInsertManyResult;
import com.datastax.astra.client.tables.options.TableInsertOneOptions;
import com.datastax.astra.client.tables.results.TableInsertOneResult;
import com.datastax.astra.client.tables.options.TableUpdateOneOptions;
import com.datastax.astra.client.tables.ddl.AlterTableOperation;
import com.datastax.astra.client.tables.ddl.AlterTableOptions;
import com.datastax.astra.client.tables.ddl.CreateIndexOptions;
import com.datastax.astra.client.tables.ddl.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.exceptions.TooManyRowsToCountException;
import com.datastax.astra.client.tables.index.TableIndexDefinition;
import com.datastax.astra.client.tables.index.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.mapping.EntityBeanDefinition;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.results.TableUpdateResult;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.client.tables.row.TableUpdate;
import com.datastax.astra.internal.api.DataAPIData;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.api.DataAPIStatus;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
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

import static com.datastax.astra.client.core.options.DataAPIClientOptions.MAX_CHUNK_SIZE;
import static com.datastax.astra.client.core.options.DataAPIClientOptions.MAX_COUNT;
import static com.datastax.astra.client.core.types.DataAPIKeywords.SORT_VECTOR;
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

    public final void alter(AlterTableOperation operation) {
        alter(operation, null);
    }

    public final void alter(AlterTableOperation operation, AlterTableOptions options) {
        notNull(operation, "operation");
        Command alterTable = Command.create("alterTable")
                .append("operation", new Document().append(operation.getOperationName(), operation));
        if (options != null) {
            alterTable.append("options", options);
        }
        runCommand(alterTable, this.options);
    }

    public final <R> Table<R> alter(AlterTableOperation operation, AlterTableOptions options, Class<R> clazz) {
        alter(operation, options);
        return new Table<>(database, tableName, this.options, clazz);
    }

    // --------------------------
    // ---    createIndex    ----
    // --------------------------

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
     * @param idxDefinition
     *      definition of the index
     */
    public void createVectorIndex(String idxName, TableVectorIndexDefinition idxDefinition) {
        createVectorIndex(idxName, idxDefinition, null, options);
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
    public void createVectorIndex(String idxName, TableVectorIndexDefinition idxDefinition, CreateVectorIndexOptions options) {
        createVectorIndex(idxName, idxDefinition, options, this.options);
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
    public void createVectorIndex(String idxName, TableVectorIndexDefinition idxDefinition, CreateVectorIndexOptions idxOptions, BaseOptions<?> cmd) {
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
        return insertOneDelegate(mapAsRow(row), null);
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

    public Optional<T> findOne(Filter filter) {
        return findOne(filter, null);
    }

    public Optional<T> findOne(Filter filter, TableFindOneOptions findOneOptions) {
        Command findOne = Command.create("findOne").withFilter(filter);
        if (findOneOptions != null) {
            findOne.withSort(findOneOptions.getSortArray())
                   .withProjection(findOneOptions.getProjectionArray())
                    .withOptions(new Document()
                        .appendIfNotNull(INPUT_INCLUDE_SIMILARITY, findOneOptions.includeSimilarity())
                        .appendIfNotNull(INPUT_INCLUDE_SORT_VECTOR, findOneOptions.includeSortVector())
            );
        }
        DataAPIData data = runCommand(findOne, findOneOptions).getData();
        if (data.getDocument() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.getDocument().map(getRowClass()));
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
    public TableCursor<T> find(Filter filter, TableFindOptions options) {
        return new TableCursor<>(this, filter, options);
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
    public TableCursor<T> findAll() {
        return find(null, new TableFindOptions());
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
                .withFilter(filter)
                .withSort(options.getSortArray())
                .withProjection(options.getProjectionArray())
                .withOptions(new Document()
                        .appendIfNotNull("skip", options.skip())
                        .appendIfNotNull("limit", options.limit())
                        .appendIfNotNull(INPUT_PAGE_STATE, options.pageState())
                        .appendIfNotNull(INPUT_INCLUDE_SORT_VECTOR, options.includeSortVector())
                        .appendIfNotNull(INPUT_INCLUDE_SIMILARITY, options.includeSimilarity()));
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

    /**
     * Gets the distinct values of the specified field name.
     * The iteration is performed at CLIENT-SIDE and will exhaust all the table elements.
     *
     * @param fieldName
     *      the field name
     * @param resultClass
     *      the class to cast any distinct items into.
     * @param <F>
     *      the target type of the iterable.
     * @return
     *      an iterable of distinct values

    public <F> CollectionDistinctIterable<T, F> distinct(String fieldName, Class<F> resultClass) {
        return distinct(fieldName, null, resultClass);
    }

    /**
     * Gets the distinct values of the specified field name.
     *
     * @param fieldName
     *      the field name
     * @param filter
     *      the query filter
     * @param resultClass
     *      the class to cast any distinct items into.
     * @param <F>
     *      the target type of the iterable.
     * @return
     *      an iterable of distinct values

    public <F> CollectionDistinctIterable<T, F> distinct(String fieldName, Filter filter, Class<F> resultClass) {
        return new CollectionDistinctIterable<>(this, fieldName, filter, resultClass);
    }
*/
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
     * @return
     *      the result of the update one operation
     */
    public TableUpdateResult updateOne(Filter filter, TableUpdate update) {
        return updateOne(filter, update, new TableUpdateOneOptions());
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
     * @return
     *      the result of the update one operation
     */
    public TableUpdateResult updateOne(Filter filter, TableUpdate update, TableUpdateOneOptions updateOptions) {
        notNull(update, ARG_UPDATE);
        notNull(updateOptions, ARG_OPTIONS);
        Command cmd = Command
                .create("updateOne")
                .withFilter(filter)
                .withUpdate(update)
                .withSort(updateOptions.getSortArray())
                .withOptions(new Document()
                        .appendIfNotNull(INPUT_UPSERT, updateOptions.upsert())
                );
        return getUpdateResult(runCommand(cmd, updateOptions));
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param apiResponse
     *       response for the API
     * @return
     *      the result of the update many operation
     */
    private static TableUpdateResult getUpdateResult(DataAPIResponse apiResponse) {
        TableUpdateResult result = new TableUpdateResult();
        DataAPIStatus status = apiResponse.getStatus();
        if (status != null) {
            if (status.containsKey(RESULT_MATCHED_COUNT)) {
                result.setMatchedCount(status.getInteger(RESULT_MATCHED_COUNT));
            }
            if (status.containsKey(RESULT_MODIFIED_COUNT)) {
                result.setModifiedCount(status.getInteger(RESULT_MODIFIED_COUNT));
            }
            if (status.containsKey(RESULT_UPSERTED_ID)) {
                result.setMatchedCount(status.getInteger(RESULT_UPSERTED_ID));
            }
        }
        return result;
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

    // --------------------------
    // ---   Utilities       ----
    // --------------------------

    /**
     * Map any object as a Row
     *
     * @param input
     *      input object
     * @return
     *      a row
     */
    public Row mapAsRow(T input) {
        if (input == null || input instanceof Row) {
            return (Row) input;
        }
        EntityTable annTable = input.getClass().getAnnotation(EntityTable.class);
        // Custom Serialization with annotations
        if (annTable != null) {
            if (Utils.hasLength(annTable.value()) && !annTable.value().equals(tableName)) {
                throw new IllegalArgumentException("Table name mismatch, expected '" + tableName + "' but got '" + annTable.value() + "'");
            }
            EntityBeanDefinition<?> bean = new EntityBeanDefinition<>(input.getClass());
            Row row = new Row();
            bean.getFields().forEach((name, field) -> {
                try {
                    row.put(field.getColumnName(), field.getGetter().invoke(input));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
            return row;
        } else {
            // Defaults mapping as a Row
            return getSerializer().convertValue(input, Row.class);
        }
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

    /**
     * Register a listener to execute commands on the table. Please now use {@link BaseOptions}.
     *
     * @param name
     *      name for the observer
     */
    public void deleteListener(String name) {
        this.options.unregisterObserver(name);
    }

}
