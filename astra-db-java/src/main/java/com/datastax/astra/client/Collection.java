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
import com.datastax.astra.client.exception.DataApiFaultyResponseException;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.BulkWriteOptions;
import com.datastax.astra.client.model.BulkWriteResult;
import com.datastax.astra.client.model.CollectionInfo;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.DeleteResult;
import com.datastax.astra.client.model.DistinctIterable;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.FindOneAndDeleteOptions;
import com.datastax.astra.client.model.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.FindOneAndReplaceResult;
import com.datastax.astra.client.model.FindOneAndUpdateOptions;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.InsertManyOptions;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.Page;
import com.datastax.astra.client.model.ReplaceOneOptions;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateOneOptions;
import com.datastax.astra.client.model.UpdateResult;
import com.datastax.astra.internal.AbstractCommandRunner;
import com.datastax.astra.internal.ApiResponse;
import com.datastax.astra.internal.utils.Assert;
import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.datastax.astra.internal.utils.AnsiUtils.cyan;
import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.AnsiUtils.magenta;
import static com.datastax.astra.internal.utils.AnsiUtils.yellow;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * A Data API collection, the main object to interact with the Data API, especially for DDL operations.
 * <p>
 * A Collection is spawned from a Database object, from which it inherits the details on how to reach the API server
 * (endpoint, authentication). A Collection has a name, which is its unique identifier for a namespace and
 * options to specialize the usage as vector collections or advanced indexing parameters.
 * </p>
 * <p>
 * A Collection is typed object designed to work both with default @{@link Document} (wrapper for a Map) and application
 * plain old java objects (pojo). The serialization is performed with Jackson and application beans can be annotated.
 * </p>
 * <p>
 * All features are provided in synnchronous and asynchronous flavore
 * </p>
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Given a DataApisNamespace
 * DataApiNamespace nameSpace = DataApiClients.create().getNamespace("demo");
 *
 * // Initialization with no POJO
 * DataApiCollection<Document> collection = namespace.getCollection("collection1");
 *
 * // Initialization with POJO
 * DataApiCollection<MyBean> collection = namespace.getCollection("collection1", MyBean.class);
 * }
 * </pre>
 *
 * @param <DOC>
 *     Java bean to unmarshall documents for collection.
 */
@Slf4j
public class Collection<DOC> extends AbstractCommandRunner {

    /** Collection identifier. */
    @Getter
    private final String collectionName;

    /** Keep ref to the generic. */
    protected final Class<DOC> documentClass;

    /** keep reference to namespace client. */
    @Getter
    private final Database database;

    /** Api Endpoint for the Database. */
    private final String apiEndpoint;

    /**
     * Full constructor.
     *
     * @param db
     *      client namespace http
     * @param collectionName
     *      collection identifier
     * @param clazz
     *      working object for document in the collection
     */
    protected Collection(Database db, String collectionName, Class<DOC> clazz) {
        notNull(db, "database");
        notNull(clazz, "working classe");
        hasLength(collectionName, "collectionName");
        this.collectionName        = collectionName;
        this.database              = db;
        this.documentClass         = clazz;
        this.apiEndpoint = db.getApiEndpoint() + "/" + collectionName;
    }

    // ----------------------------
    // --- Global Information ----
    // ----------------------------

    /**
     * Access the parent namespace associated to the collection
     * @return
     *      the name of the parent namespace for current collection.
     */
    public String getNamespaceName() {
        return getDatabase().getNamespaceName();
    }

    /**
     * Retrieves the full definition of the collection with its name and options.
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a collection
     * DataApiCollection<Document> collection;
     * // Access its Definition
     * CollectionDefinition definition = collection.getDefinition();
     * System.out.println("Name=" + definition.getName());
     * CreateCollectionOptions options = definition.getOptions();
     * i f (options != null) {
     *   // omitte
     * }
     * }
     * </pre>
     *
     * @return the full collection definition
     */
    public CollectionInfo getDefinition() {
        return database
                .listCollections()
                .filter(col -> col.getName().equals(collectionName))
                .findFirst()
                .orElseThrow(() -> new DataApiException("[COLLECTION_NOT_EXIST] - Collection does not exist, " +
                        "collection name: '" + collectionName + "'", "COLLECTION_NOT_EXIST", null));
    }

    /**
     * Retrieves the configuration options for the collection, including vector and indexing settings.
     * These options specify how the collection should be created and managed, potentially affecting
     * performance, search capabilities, and data organization.
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a collection
     * DataApiCollection<Document> collection;
     * // Access its Options
     * CreateCollectionOptions options = collection.getOptions();
     * if (null != c.getVector()) {
     *   System.out.println(c.getVector().getDimension());
     *   System.out.println(c.getVector().getMetric());
     * }
     * if (null != c.getIndexing()) {
     *   System.out.println(c.getIndexing().getAllow());
     *   System.out.println(c.getIndexing().getDeny());
     * }
     * }
     * </pre>
     *
     * @return An instance of {@link CollectionOptions} containing the collection's configuration settings,
     *         such as vector and indexing options. Returns {@code null} if no options are set or applicable.
     */
    public CollectionOptions getOptions() {
        return Optional
                .ofNullable(getDefinition()
                        .getOptions())
                .orElse(new CollectionOptions());
    }

    /**
     * Retrieves the class type of the POJO (Plain Old Java Object) used for unmarshalling documents
     * within the collection. This class type is crucial for converting the raw data from the collection
     * into more manageable, object-oriented representations. By default, this method returns the
     * {@link Document} class, which serves as the standard container
     * for document data. Custom implementations can override this default to utilize a different POJO
     * that better suits their data structure and requirements.
     *
     * @return The {@code Class<DOC>} type representing the POJO class used for unmarshalling documents
     *         from the collection. This class facilitates the conversion from stored document formats
     *         to Java object instances, allowing for more intuitive data manipulation and access within
     *         the application.
     */
    public Class<DOC> getDocumentClass() {
        return documentClass;
    }

    /**
     * Retrieves the name of the collection.
     *
     * @return The name of the collection
     */
    public String getName() {
        return collectionName;
    }

    // --------------------------
    // ---   Insert*         ----
    // --------------------------

    /**
     * Insert a single document in the collection in an atomic operation.
     *
     * <blockquote><b>Note:</b>If an `_id` is explicitly provided, which corresponds to a document
     * that exists already in the collection, an error is raised and the insertion fails.
     * Inserts the provided document. If the document is missing an identifier, the server will generate one.
     * </blockquote>
     *
     * @param document
     *     the document expressing the document to insert. The `_id` field of the document can be left out, in which case it will be created automatically.
     * @return
     *       an InsertOneResult object.
     */
    public final InsertOneResult insertOne(DOC document) {
        Assert.notNull(document, "document");
        return _insertOne(JsonUtils.convertValueForDataApi(document, Document.class));
    }

    /**
     * Insert a single document in the collection in an atomic operation.
     *
     * <blockquote><b>Note:</b>If an `_id` is explicitly provided, which corresponds to a document
     * that exists already in the collection, an error is raised and the insertion fails.
     * Inserts the provided document. If the document is missing an identifier, the server will generate one.
     * </blockquote>
     *
     * @param document
     *     the document expressing the document to insert. The `_id` field of the document can be left out, in which case it will be created automatically.
     * @param embeddings
     *      the vector corresponding to the embeddings.
     * @return
     *       an InsertOneResult object.
     */
    public final InsertOneResult insertOne(DOC document, float[] embeddings) {
        Assert.notNull(document, "document");
        Assert.notNull(embeddings, "vectorize");
        return _insertOne(JsonUtils.convertValueForDataApi(document, Document.class).vector(embeddings));
    }

    /**
     * Insert a single document in the collection in an atomic operation.
     *
     * <blockquote><b>Note:</b>If an `_id` is explicitly provided, which corresponds to a document
     * that exists already in the collection, an error is raised and the insertion fails.
     * Inserts the provided document. If the document is missing an identifier, the server will generate one.
     * </blockquote>
     *
     * @param document
     *     the document expressing the document to insert. The `_id` field of the document can be left out, in which case it will be created automatically.
     * @param vectorize
     *      the expression that will be translated as a vector of embeddings
     * @return
     *       an InsertOneResult object.
     */
    public final InsertOneResult insertOne(DOC document, String vectorize) {
        Assert.notNull(document, "document");
        Assert.hasLength(vectorize, "vectorize");
        return _insertOne(JsonUtils.convertValueForDataApi(document, Document.class).vectorize(vectorize));
    }

    /**
     * Insert a single document in the collection in an atomic operation.
     *
     * @param document
     *      the document expressing the document to insert. The `_id` field of the document can be left out, in which case it will be created automatically.
     * @return
     *      an InsertOneResult object.
     */
    private InsertOneResult _insertOne(Document document) {
        Assert.notNull(document, "document");
        Command insertOne = Command
                .create("insertOne")
                .withDocument(document);

        ApiResponse res = runCommand(insertOne);
        // Object
        return new InsertOneResult(res.getStatusKeyAsList("insertedIds", Object.class).get(0));
    }

    /**
     * Inserts one or more documents.
     *
     * @param documents
     *      the documents to insert
     * @return
     *      the insert many result
     * @throws IllegalArgumentException
     *      if the documents list is null or empty, or any of the documents in the list are null
     */
    public InsertManyResult insertMany(List<? extends DOC> documents) {
        return insertMany(documents, new InsertManyOptions());
    }

    /**
     * Inserts one or more documents.
     *
     * @param documents
     *      the documents to insert
     * @param options
     *      detailed options for the insert many
     * @return
     *      the insert many result
     * @throws IllegalArgumentException
     *      if the documents list is null or empty, or any of the documents in the list are null
     */
    public InsertManyResult insertMany(List<? extends DOC> documents, InsertManyOptions options) {
        if (options.getConcurrency() > 1 && options.isOrdered()) {
            throw new IllegalArgumentException("Cannot run ordered insert_many concurrently.");
        }
        if (options.getChunkSize() > DataAPIOptions.getMaxDocumentsInInsert()) {
            throw new IllegalArgumentException("Cannot insert more than " + DataAPIOptions.getMaxDocumentsInInsert() + " at a time.");
        }
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
        List<Future<InsertManyResult>> futures = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += options.getChunkSize()) {
            futures.add(executor.submit(getInsertManyResultCallable(documents, options, i)));
        }
        executor.shutdown();

        // Grouping All Insert ids in the same list.
        InsertManyResult finalResult = new InsertManyResult();
        try {
            for (Future<InsertManyResult> future : futures) {
                finalResult.getInsertedIds().addAll(future.get().getInsertedIds());
            }

            if (executor.awaitTermination(options.getTimeout(), TimeUnit.MILLISECONDS)) {
                log.debug(magenta(".[total insertMany.responseTime]") + "=" + yellow("{}") + " millis.",
                        System.currentTimeMillis() - start);
            } else {
                throw new TimeoutException("Request did not complete withing ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot merge call results into a InsertManyResult", e);
        }
        return finalResult;
    }

    /**
     * Execute a 1 for 1 call to the Data API.
     *
     * @param documents
     *      list of documents to be inserted
     * @param options
     *      options for insert many (chunk size and insertion order).
     * @param start
     *      offset in global list
     * @return
     *      insert many result for a paged call
     */
    private Callable<InsertManyResult> getInsertManyResultCallable(List<? extends DOC> documents, InsertManyOptions options, int start) {
        int end = Math.min(start + options.getChunkSize(), documents.size());
        return () -> {
            log.debug("Insert block (" + cyan("size={}") + ") in collection {}", end - start, green(getCollectionName()));
            Command insertMany = new Command("insertMany")
                    .withDocuments(documents.subList(start, end))
                    .withOptions(new Document().append("ordered", options.isOrdered()));
            return new InsertManyResult(runCommand(insertMany).getStatusKeyAsList("insertedIds", Object.class));
        };
    }

    // --------------------------
    // ---   Find*           ----
    // --------------------------

    /**
     * Attempts to find a single document within the collection that matches the given filter criteria.
     * This method is designed to return the first document that satisfies the filter conditions,
     * making it particularly useful for retrieving specific documents when unique identifiers or
     * specific criteria are known. If no document matches the filter, the method will return an empty
     * {@link java.util.Optional}, indicating the absence of a matching document. This approach
     * avoids throwing exceptions for non-existent documents, thereby facilitating cleaner and more
     * robust error handling in client code.
     *
     * @param filter The {@link Filter} instance containing the criteria used to identify the desired document.
     *               It specifies the conditions that a document must meet to be considered a match.
     * @return An {@link java.util.Optional<DOC>} that contains the found document if one exists that matches
     *         the filter criteria. Returns an empty {@link java.util.Optional} if no matching document is found,
     *         enabling safe retrieval operations without the risk of {@link java.util.NoSuchElementException}.
     */
    public Optional<DOC> findOne(Filter filter) {
        return findOne(filter, new FindOneOptions());
    }

    /**
     * Attempts to find a single document within the collection that matches the given filter criteria.
     * This method is designed to return the first document that satisfies the filter conditions,
     * making it particularly useful for retrieving specific documents when unique identifiers or
     * specific criteria are known. If no document matches the filter, the method will return an empty
     * {@link Optional}, indicating the absence of a matching document. This approach
     * avoids throwing exceptions for non-existent documents, thereby facilitating cleaner and more
     * robust error handling in client code.
     *
     * @param filter The {@link Filter} instance containing the criteria used to identify the desired document.
     *               It specifies the conditions that a document must meet to be considered a match.
     * @param options The {@link FindOneOptions} instance containing additional options for the find operation,
     * @return An {@link Optional<DOC>} that contains the found document if one exists that matches
     *         the filter criteria. Returns an empty {@link Optional} if no matching document is found,
     *         enabling safe retrieval operations without the risk of {@link java.util.NoSuchElementException}.
     */
    public Optional<DOC> findOne(Filter filter, FindOneOptions options) {
        notNull(options, "options");
        Command findOne = Command
                .create("findOne")
                .withFilter(filter)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull("includeSimilarity", options.getIncludeSimilarity()));
        return Optional.ofNullable(
                runCommand(findOne)
                        .getData().getDocument()
                        .map(getDocumentClass()));
    }

    /**
     * Initiates an asynchronous search to find a single document that matches the given filter criteria.
     * This method leverages the functionality of  to perform the
     * search, but it does so asynchronously, returning a {@link CompletableFuture}. This approach allows
     * the calling thread to remain responsive and perform other tasks while the search operation completes.
     * The result of the operation is wrapped in a {@link CompletableFuture} that, upon completion, will
     * contain an {@link Optional} instance. This instance either holds the document that matches the filter
     * criteria or is empty if no such document exists.
     *
     * @param filter The {@link Filter} specifying the conditions that the document must meet to be considered
     *               a match. This parameter determines how the search is conducted and what criteria the
     *               document must satisfy to be retrieved.
     * @return CompletableFuture that, when completed, will contain the result of
     *         the search operation. If a matching document is found, the {@link Optional} is non-empty;
     *         otherwise, it is empty to indicate the absence of a matching document. This future allows for
     *         non-blocking operations and facilitates the integration of asynchronous programming patterns.
     */
    public CompletableFuture<Optional<DOC>> findOneASync(Filter filter) {
        return CompletableFuture.supplyAsync(() -> findOne(filter));
    }
    /**
     * Retrieves a document by its identifier from the collection.
     * <p>
     * This method searches for a document with the specified {@code id}. If a matching document is found,
     * it is returned wrapped in an {@link Optional}, otherwise, an empty {@link Optional} is returned.
     * This approach provides a null-safe way to handle the presence or absence of a document.
     * </p>
     *
     * @param id The identifier of the document to find.
     * @return An {@link Optional} containing the found document, or an empty {@link Optional} if no document
     *         matches the provided {@code id}.
     */
    public Optional<DOC> findById(Object id) {
        return findOne(Filters.eq(id));
    }

    /**
     * Finds all documents in the collection.
     *
     * @param filter
     *      the query filter
     * @param options
     *      options of find one
     * @return
     *      the find iterable interface
     */
    public FindIterable<DOC> find(Filter filter, FindOptions options) {
        return new FindIterable<>(this, filter, options);
    }

    /**
     * Retrieves all documents in the collection.
     * <p>
     * This method returns an iterable interface that allows iterating over all documents in the collection,
     * without applying any filters. It leverages the default {@link FindOptions} for query execution.
     * </p>
     *
     * @return A {@link FindIterable} for iterating over all documents in the collection.
     */
    public FindIterable<DOC> find() {
        return find(null, new FindOptions());
    }

    /**
     * Retrieves documents in the collection that match the specified filter.
     * <p>
     * This method returns an iterable interface for documents that meet the criteria defined by the {@code filter}.
     * It uses default {@link FindOptions} for query execution, allowing for customization of the query if needed.
     * </p>
     *
     * @param filter The query filter to apply when retrieving documents.
     * @return A {@link FindIterable} for iterating over the documents that match the filter.
     */
    public FindIterable<DOC> find(Filter filter) {
        return find(filter, new FindOptions());
    }

    /**
     * Finds documents in the collection that match the specified filter and sorts them based on their similarity
     * to a provided vector, limiting the number of results returned.
     * <p>
     * This method is particularly useful for vector-based search operations where documents are ranked according
     * to their distance from a reference vector. The {@code limit} parameter controls the maximum number of documents
     * to return, allowing for efficient retrieval of the most relevant documents.
     * </p>
     *
     * @param filter The query filter to apply when retrieving documents.
     * @param vector A float array representing the vector used to sort the documents.
     * @param limit The maximum number of documents to return.
     * @return A {@link FindIterable} for iterating over the sorted and limited documents.
     */
    public FindIterable<DOC> find(Filter filter, float[] vector, int limit) {
        return find(filter, FindOptions.builder()
                .withVector(vector)
                .limit(limit)
                .build());
    }

    /**
     * Finds documents in the collection that match the specified filter and sorts them based on their similarity
     * to a provided vector, limiting the number of results returned.
     * <p>
     * This method leverage the 'vectorization' to compute the embeddings on the fly in order to execute the search.
     * </p>
     *
     * @param filter The query filter to apply when retrieving documents.
     * @param vectorize A float array representing the vector used to sort the documents.
     * @param limit The maximum number of documents to return.
     * @return A {@link FindIterable} for iterating over the sorted and limited documents.
     */
    public FindIterable<DOC> find(Filter filter, String vectorize, int limit) {
        return find(filter, FindOptions.builder()
                .withVectorize(vectorize)
                .limit(limit)
                .build());
    }

    /**
     * Finds all documents in the collection, applying the specified find options.
     * <p>
     * This method allows for detailed control over the query execution through {@link FindOptions}, which can
     * specify sorting, projection, limits, and other query parameters. If no filter is applied, all documents
     * in the collection are considered.
     * </p>
     *
     * @param options The {@link FindOptions} to apply when executing the find operation.
     * @return A {@link FindIterable} for iterating over the documents according to the specified options.
     */
    public FindIterable<DOC> find(FindOptions options) {
        return find(null, options);
    }

    /**
     * Executes a paginated 'find' query on the collection using the specified filter and find options.
     * <p>
     * This method constructs and executes a command to fetch a specific page of documents from the collection that match
     * the provided filter criteria. It allows for detailed control over the query through {@code FindOptions}, such as sorting,
     * projection, pagination, and more. The result is wrapped in a {@link Page} object, which includes the documents found,
     * the page size, and the state for fetching subsequent pages.
     * </p>
     * <p>
     * Pagination is facilitated by the {@code skip}, {@code limit}, and {@code pageState} parameters within {@code FindOptions},
     * enabling efficient data retrieval in scenarios where the total dataset is too large to be fetched in a single request.
     * Optionally, similarity scoring can be included if {@code includeSimilarity} is set, which is useful for vector-based search queries.
     * </p>
     * <p>
     * The method processes the command's response, mapping each document to the specified document class and collecting them into a list.
     * This list, along with the maximum page size and the next page state, is used to construct the {@link Page} object returned by the method.
     * </p>
     *
     * @param filter The filter criteria used to select documents from the collection.
     * @param options The {@link FindOptions} providing additional query parameters, such as sorting and pagination.
     * @return A {@link Page} object containing the documents that match the query, along with pagination information.
     */
    public Page<DOC> findPage(Filter filter, FindOptions options) {
        Command findCommand = Command
                .create("find")
                .withFilter(filter)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull("skip", options.getSkip())
                        .appendIfNotNull("limit", options.getLimit())
                        .appendIfNotNull("pageState", options.getPageState())
                        .appendIfNotNull("includeSimilarity", options.getIncludeSimilarity()));

        ApiResponse apiResponse = runCommand(findCommand);

        return new Page<>(DataAPIOptions.getMaxPageSize(),
                apiResponse.getData().getNextPageState(),
                apiResponse.getData().getDocuments()
                        .stream()
                        .map(d -> d.map(getDocumentClass()))
                        .collect(Collectors.toList()));
    }

    // --------------------------
    // ---   Distinct        ----
    // --------------------------

    /**
     * Gets the distinct values of the specified field name.
     * The iteration is performed at CLIENT-SIDE and will exhaust all the collections elements.
     *
     * @param fieldName
     *      the field name
     * @param resultClass
     *      the class to cast any distinct items into.
     * @param <FIELD>
     *      the target type of the iterable.
     * @return
     *      an iterable of distinct values
     */
    public <FIELD> DistinctIterable<DOC, FIELD> distinct(String fieldName, Class<FIELD> resultClass) {
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
     * @param <FIELD>
     *      the target type of the iterable.
     * @return
     *      an iterable of distinct values
     */
    public <FIELD> DistinctIterable<DOC, FIELD> distinct(String fieldName, Filter filter, Class<FIELD> resultClass) {
        return new DistinctIterable<>(this, fieldName, filter, resultClass);
    }

    // ----------------------------
    // ---   Count Document    ----
    // ----------------------------

    /**
     * Counts the number of documents in the collection.
     *
     * <p>
     * Takes in a `upperBound` option which dictates the maximum number of documents that may be present before a
     * {@link TooManyDocumentsToCountException} is thrown. If the limit is higher than the highest limit accepted by the
     * Data API, a {@link TooManyDocumentsToCountException} will be thrown anyway (i.e. `1000`).
     * </p>
     * <p>
     * Count operations are expensive: for this reason, the best practice is to provide a reasonable `upperBound`
     * according to the caller expectations. Moreover, indiscriminate usage of count operations for sizeable amounts
     * of documents (i.e. in the thousands and more) is discouraged in favor of alternative application-specific
     * solutions. Keep in mind that the Data API has a hard upper limit on the amount of documents it will count,
     * and that an exception will be thrown by this method if this limit is encountered.
     * </p>
     *
     * @param upperBound
     *      The maximum number of documents to count.
     * @return
     *      The number of documents in the collection.
     * @throws TooManyDocumentsToCountException
     *      If the number of documents counted exceeds the provided limit.
     */
    public int countDocuments(int upperBound) throws TooManyDocumentsToCountException {
        return countDocuments(null, upperBound);
    }

    /**
     * Counts the number of documents in the collection with a filter.
     *
     * <p>
     * Takes in a `upperBound` option which dictates the maximum number of documents that may be present before a
     * {@link TooManyDocumentsToCountException} is thrown. If the limit is higher than the highest limit accepted by the
     * Data API, a {@link TooManyDocumentsToCountException} will be thrown anyway (i.e. `1000`).
     * </p>
     * <p>
     * Count operations are expensive: for this reason, the best practice is to provide a reasonable `upperBound`
     * according to the caller expectations. Moreover, indiscriminate usage of count operations for sizeable amounts
     * of documents (i.e. in the thousands and more) is discouraged in favor of alternative application-specific
     * solutions. Keep in mind that the Data API has a hard upper limit on the amount of documents it will count,
     * and that an exception will be thrown by this method if this limit is encountered.
     * </p>
     *
     * @param filter
     *      A filter to select the documents to count. If not provided, all documents will be counted.
     * @param upperBound
     *      The maximum number of documents to count.
     * @return
     *      The number of documents in the collection.
     * @throws TooManyDocumentsToCountException
     *      If the number of documents counted exceeds the provided limit.
     */
    public int countDocuments(Filter filter, int upperBound) throws TooManyDocumentsToCountException {
        // Argument Validation
        if (upperBound<1 || upperBound> DataAPIOptions.getMaxDocumentCount()) {
            throw new IllegalArgumentException("UpperBound limit should be in between 1 and " + DataAPIOptions.getMaxDocumentCount());
        }
        // Build command
        Command command = new Command("countDocuments").withFilter(filter);
        // Run command
        ApiResponse response = runCommand(command);
        // Build Result
        Boolean moreData = response.getStatus().getBoolean("moreData");
        Integer count    = response.getStatus().getInteger("count");
        if (moreData != null && moreData) {
            throw new TooManyDocumentsToCountException();
        } else if (count > upperBound) {
            throw new TooManyDocumentsToCountException(upperBound);
        }
        return count;
    }

    // ----------------------------
    // ---   Delete            ----
    // ----------------------------

    /**
     * attribute for the delete count
     */
    public static final String DELETED_COUNT = "deletedCount";

    /**
     * attribute for the matched count
     */
    public static final String MATCHED_COUNT = "matchedCount";

    /**
     * attribute for the modified count
     */
    public static final String MODIFIED_COUNT = "modifiedCount";

    /**
     * attribute for upserted Id
     */
    public static final String UPSERTED_ID = "upsertedId";

    /**
     * attribute for the moreData
     */
    public static final String MORE_DATA = "moreData";

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
    public DeleteResult deleteOne(Filter filter) {
        return deleteOne(filter, new DeleteOneOptions());
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
    public DeleteResult deleteOne(Filter filter, DeleteOneOptions deleteOneOptions) {
        Command deleteOne = Command
                .create("deleteOne")
                .withFilter(filter)
                .withSort(deleteOneOptions.getSort());

        ApiResponse apiResponse = runCommand(deleteOne);
        int deletedCount = apiResponse.getStatus().getInteger(DELETED_COUNT);
        return new DeleteResult(deletedCount);
    }

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @return
     *      the result of the remove many operation
     */
    public DeleteResult deleteMany(Filter filter) {
        Assert.notNull(filter, "filter");
        AtomicInteger totalCount = new AtomicInteger(0);
        boolean moreData = false;
        do {
            Command deleteMany = Command
                    .create("deleteMany")
                    .withFilter(filter);

            ApiResponse apiResponse = runCommand(deleteMany);
            Document status = apiResponse.getStatus();
            if (status != null) {
                if (status.containsKey(DELETED_COUNT)) {
                    totalCount.addAndGet(status.getInteger(DELETED_COUNT));
                }
                if (status.containsKey(MORE_DATA)) {
                    moreData = status.getBoolean(MORE_DATA);
                }
            }
        } while(moreData);
        return new DeleteResult(totalCount.get());
    }

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @return
     *      the result of the remove many operation
     */
    public DeleteResult deleteAll() {
        return deleteMany(new Filter());
    }

    /**
     * Checks if the specified collection exists within the current namespace.
     *
     * <p>
     * This method delegates the existence check to the {@code existCollection} method of the associated
     * namespace, evaluates the existence based on the collection's name, as retrieved by getName().
     * </p>
     *
     * @return {@code true} if the collection exists within the namespace, {@code false} otherwise.
     */
    public boolean exists() {
        return getDatabase().collectionExists(getName());
    }

    /**
     * Delete the current collection and all documents that its contains.
     */
    public void drop() {
        getDatabase().dropCollection(collectionName);
    }

    // ----------------------------
    // ---  Update             ----
    // ----------------------------

    /**
     * Atomically find a document and replace it.
     *
     * @param filter
     *      the query filter to apply the replace operation
     * @param replacement
     *      the replacement document
     * @return
     *      the document that was replaced.  Depending on the value of the {@code returnOriginal} property, this will either be the document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be returned
     */
    public Optional<DOC> findOneAndReplace(Filter filter, DOC replacement) {
        return findOneAndReplace(filter, replacement, new FindOneAndReplaceOptions());
    }

    /**
     * Atomically find a document and replace it.
     *
     * <p>Note: Supports retryable writes on MongoDB server versions 3.6 or higher when the retryWrites setting is enabled.</p>
     * @param filter
     *      the query filter to apply the replace operation
     * @param replacement
     *      the replacement document
     * @param options
     *      the options to apply to the operation
     * @return
     *      the document that was replaced.  Depending on the value of the {@code returnOriginal} property, this will either be the
     * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
     * returned
     */
    public Optional<DOC> findOneAndReplace(Filter filter, DOC replacement, FindOneAndReplaceOptions options) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull("upsert", options.getUpsert())
                        .appendIfNotNull("returnDocument", options.getReturnDocument().name())
                );

        ApiResponse res = runCommand(findOneAndReplace);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }

    /**
     * Replace a single document on the collection with a new one,
     * optionally inserting a new document if no match is found.
     *
     * @param filter
     *      the query filter to apply the replace operation
     * @param replacement
     *      the replacement document
     * @return
     *      result of the replace one operation
     */
    public UpdateResult replaceOne(Filter filter, DOC replacement) {
        return replaceOne(filter, replacement, new ReplaceOneOptions());
    }

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param filter
     *      the query filter to apply the replace operation
     * @param replacement
     *      the replacement document
     * @param replaceOneOptions
     *      the options to apply to the replace operation
     * @return
     *      the result of the replace one operation
     */
    public UpdateResult replaceOne(Filter filter, DOC replacement, ReplaceOneOptions replaceOneOptions) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withOptions(new Document()
                        .appendIfNotNull("upsert", replaceOneOptions.getUpsert())
                        .append("returnDocument", FindOneAndReplaceOptions.ReturnDocument.before.name())
                );

        // Execute the `findOneAndReplace`
        FindOneAndReplaceResult<DOC> res = executeFindOneAndReplace(findOneAndReplace);

        // Parse the result for a replace one
        UpdateResult result = new UpdateResult();
        result.setMatchedCount(res.getMatchedCount());
        result.setModifiedCount(res.getModifiedCount());
        if (res.getDocument() != null) {
            Document doc = JsonUtils.convertValueForDataApi(res.getDocument(), Document.class);
            if (doc.getId(Object.class) != null) {
                result.setUpsertedId(doc.getId(Object.class));
            }
        }
        return result;
    }

    /**
     * Mutualisation of the code for replaceOne() and findOneAndReplaceOne().
     *
     * @param cmd
     *      command
     * @return
     *      command result
     */
    private FindOneAndReplaceResult<DOC> executeFindOneAndReplace(Command cmd) {
        // Run Command
        ApiResponse apiResponse = runCommand(cmd);
        // Parse Command Result
        FindOneAndReplaceResult<DOC> result = new FindOneAndReplaceResult<>();
        if (apiResponse.getData() == null) {
            throw new DataApiFaultyResponseException(cmd, apiResponse,"Faulty response from find_one_and_replace API command.");
        }
        if (apiResponse.getData().getDocument() != null) {
            result.setDocument(apiResponse
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        Document status = apiResponse.getStatus();
        if (status != null) {
            if (status.containsKey(MATCHED_COUNT)) {
                result.setMatchedCount(status.getInteger(MATCHED_COUNT));
            }
            if (status.containsKey(MODIFIED_COUNT)) {
                result.setModifiedCount(status.getInteger(MODIFIED_COUNT));
            }
        }
        return result;
    }

    /**
     * Atomically find a document and update it.
     *
     * <p>Note: Supports retryable writes on MongoDB server versions 3.6 or higher when the retryWrites setting is enabled.</p>
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include at least one update operator.
     * @return the document that was updated before the update was applied.  If no documents matched the query filter, then null will be
     * returned
     */
    public Optional<DOC> findOneAndUpdate(Filter filter, Update update) {
        return findOneAndUpdate(filter, update, new FindOneAndUpdateOptions());
    }

    /**
     * Atomically find a document and update it.
     *
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include at least one update
     *               operator.
     * @param options
     *      the options to apply to the operation
     * @return
     *      the document that was updated.  Depending on the value of the {@code returnOriginal} property, this will either be the
     * document as it was before the update or as it is after the update.  If no documents matched the query filter, then null will be
     * returned
     */
    public Optional<DOC> findOneAndUpdate(Filter filter, Update update, FindOneAndUpdateOptions options) {
        notNull(update, "update");
        notNull(options, "options");
        Command cmd = Command
                .create("findOneAndUpdate")
                .withFilter(filter)
                .withUpdate(update)
                .withSort(options.getSort())
                .withProjection(options.getProjection())
                .withOptions(new Document()
                        .appendIfNotNull("upsert", options.getUpsert())
                        .append("returnDocument", options.getReturnDocument().name())
                );

        ApiResponse res = runCommand(cmd);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }

    /**
     * Update a single document in the collection according to the specified arguments.
     *
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include at least one update operator.
     * @return
     *      the result of the update one operation
     */
    public UpdateResult updateOne(Filter filter, Update update) {
        return updateOne(filter, update, new UpdateOneOptions());
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
    public UpdateResult updateOne(Filter filter, Update update, UpdateOneOptions updateOptions) {
        notNull(update, "update");
        notNull(updateOptions, "options");
        Command cmd = Command
                .create("updateOne")
                .withFilter(filter)
                .withUpdate(update)
                .withSort(updateOptions.getSort())
                .withOptions(new Document()
                        .appendIfNotNull("upsert", updateOptions.getUpsert())
                );
        return getUpdateResult(runCommand(cmd));
    }

    private static UpdateResult getUpdateResult(ApiResponse apiResponse) {
        UpdateResult result = new UpdateResult();
        Document status = apiResponse.getStatus();
        if (status != null) {
            if (status.containsKey(MATCHED_COUNT)) {
                result.setMatchedCount(status.getInteger(MATCHED_COUNT));
            }
            if (status.containsKey(MODIFIED_COUNT)) {
                result.setModifiedCount(status.getInteger(MODIFIED_COUNT));
            }
            if (status.containsKey(UPSERTED_ID)) {
                result.setMatchedCount(status.getInteger(UPSERTED_ID));
            }
        }
        return result;
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include only update operators.
     * @return
     *      the result of the update many operation
     */
    public UpdateResult updateMany(Filter filter, Update update) {
        return updateMany(filter, update, new UpdateOneOptions());
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param options
     *      the options to apply to the update operation
     * @return
     *      the result of the update many operation
     */
    public UpdateResult updateMany(Filter filter, Update update, UpdateOneOptions options) {
        notNull(update, "update");
        notNull(options, "options");
        boolean moreData = true;
        String nextPageState = null;
        UpdateResult result = new UpdateResult();
        result.setMatchedCount(0);
        result.setModifiedCount(0);
        do {
            Command cmd = Command
                    .create("updateMany")
                    .withFilter(filter)
                    .withUpdate(update)
                    .withOptions(new Document()
                            .appendIfNotNull("upsert", options.getUpsert())
                            .appendIfNotNull("pageState", nextPageState));
            ApiResponse res = runCommand(cmd);
            // Data
            if (res.getData() != null) {
                nextPageState = res.getData().getNextPageState();
            }
            // Status
            Document status = res.getStatus();
            if (status.containsKey(MATCHED_COUNT)) {
                result.setMatchedCount(result.getMatchedCount() + status.getInteger(MATCHED_COUNT));
            }
            if (status.containsKey(MODIFIED_COUNT)) {
                result.setModifiedCount(result.getModifiedCount() + status.getInteger(MODIFIED_COUNT));
            }
            if (status.containsKey(UPSERTED_ID)) {
                result.setUpsertedId(status.getInteger(UPSERTED_ID));
            }
        } while(nextPageState != null);
        return result;
    }

    /**
     * Atomically find a document and remove it.
     *
     * @param filter
     *      the query filter to find the document with
     * @return
     *      the document that was removed.  If no documents matched the query filter, then null will be returned
     */
    public Optional<DOC> findOneAndDelete(Filter filter) {
        return findOneAndDelete(filter, new FindOneAndDeleteOptions());
    }

    /**
     * Atomically find a document and remove it.
     *
     * @param filter
     *      the query filter to find the document with
     * @param options
     *      the options to apply to the operation
     * @return
     *      the document that was removed.  If no documents matched the query filter, then null will be returned
     */
    public Optional<DOC> findOneAndDelete(Filter filter, FindOneAndDeleteOptions options) {
        Command findOneAndReplace = Command
                .create("findOneAndDelete")
                .withFilter(filter)
                .withSort(options.getSort())
                .withProjection(options.getProjection());

        ApiResponse res = runCommand(findOneAndReplace);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }

    // ----------------------------
    // ---   Bulk Write        ----
    // ----------------------------

    /**
     * Executes a mix of inserts, updates, replaces, and deletes.
     *
     * @param commands
     *      list of commands to run
     * @return
     *      the result of the bulk write
     */
    public BulkWriteResult bulkWrite(List<Command> commands) {
        return bulkWrite(commands, new BulkWriteOptions());
    }

    /**
     * Executes a mix of inserts, updates, replaces, and deletes.
     *
     * @param options
     *      if requests must be ordered or not
     * @param commands
     *      list of commands to run
     * @return
     *      the result of the bulk write
     */
    public BulkWriteResult bulkWrite(List<Command> commands, BulkWriteOptions options) {
        notNull(commands, "commands");
        notNull(options, "options");
        if (options.getConcurrency() > 1 && options.isOrdered()) {
            throw new IllegalArgumentException("Cannot run ordered bulk_write concurrently.");
        }
        BulkWriteResult result = new BulkWriteResult(0);
        result = new BulkWriteResult(commands.size());
        if (options.isOrdered()) {
            result.setResponses(commands.stream().map(this::runCommand).collect(Collectors.toList()));
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
            List<Future<ApiResponse>> futures = new ArrayList<>();
            commands.forEach(req -> futures.add(executor.submit(() -> runCommand(req))));
            executor.shutdown();
            try {
                for (Future<ApiResponse> future : futures) {
                    result.getResponses().add(future.get());
                }
            } catch(Exception ex) {
                throw new IllegalStateException("Cannot access command results", ex);
            }
        }
        return result;
    }

    // --- Required for the Command Runner ---

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return apiEndpoint;
    }

    /** {@inheritDoc} */
    @Override
    protected String getToken() {
        return database.getToken();
    }

    /** {@inheritDoc} */
    @Override
    protected DataAPIOptions.HttpClientOptions getHttpClientOptions() {
        return database.getOptions().getHttpClientOptions();
    }

}
