package com.datastax.astra.client.collections;

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

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.ReturnDocument;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.exceptions.TooManyDocumentsToCountException;
import com.datastax.astra.client.collections.commands.options.CollectionDeleteManyOptions;
import com.datastax.astra.client.collections.commands.options.CollectionDeleteOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneAndDeleteOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneAndReplaceOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneAndUpdateOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.options.CollectionInsertOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionReplaceOneOptions;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateManyOptions;
import com.datastax.astra.client.collections.commands.options.CountDocumentsOptions;
import com.datastax.astra.client.collections.commands.options.EstimatedCountDocumentsOptions;
import com.datastax.astra.client.collections.commands.options.UpdateOneOptions;
import com.datastax.astra.client.collections.commands.results.CollectionDeleteResult;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.commands.results.FindOneAndReplaceResult;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.collections.commands.cursor.CollectionCursor;
import com.datastax.astra.client.collections.commands.cursor.CollectionDistinctIterable;
import com.datastax.astra.client.core.paging.FindIterable;
import com.datastax.astra.client.core.paging.Page;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv6;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv7;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.exceptions.UnexpectedDataAPIResponseException;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.api.DataAPIStatus;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.MAX_CHUNK_SIZE;
import static com.datastax.astra.client.core.options.DataAPIClientOptions.MAX_COUNT;
import static com.datastax.astra.client.core.DataAPIKeywords.SORT_VECTOR;
import static com.datastax.astra.client.exceptions.DataAPIException.ERROR_CODE_INTERRUPTED;
import static com.datastax.astra.client.exceptions.DataAPIException.ERROR_CODE_TIMEOUT;
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
 * All features are provided in synchronous and asynchronous flavors.
 * </p>
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Given a Database
 * Database db = new DataAPIClient("token").getDatabase("api_endpoint");
 *
 * // Initialization with no POJO
 * Collection<Document> collection = db.getCollection("collection1");
 *
 * // Initialization with POJO
 * Collection<MyBean> collection = db.getCollection("collection1", MyBean.class);
 * }
 * </pre>
 *
 * @param <T>
 *     Java bean to unmarshall documents for collection.
 */
@Slf4j
public class Collection<T> extends AbstractCommandRunner<CollectionOptions> {

    /** parameters names. */
    protected static final String ARG_OPTIONS = "options";
    /** parameters names. */
    protected static final String ARG_UPDATE = "update";
    /** parameters names. */
    protected static final String DOCUMENT = "document";

    /** Default collection serializer. */
    public static final DataAPISerializer DEFAULT_COLLECTION_SERIALIZER = new DocumentSerializer();

    /** Collection identifier. */
    @Getter
    private final String collectionName;

    /** Parent Database reference.  */
    @Getter
    private final Database database;

    @Getter
    private final Class<T> documentClass;

    /**
     * Keep Collection options in -memory to avoid multiple calls to the API.
     */
    private CollectionDefinition collectionDefinition;

    /**
     * Constructs an instance of a collection within the specified database. This constructor
     * initializes the collection with a given name and associates it with a specific class type
     * that represents the schema of documents within the collection. This setup is designed for
     * CRUD (Create, Read, Update, Delete) operations.
     *
     * @param db The {@code Database} instance representing the client's namespace for HTTP
     *           communication with the database. It encapsulates the configuration and management
     *           of the database connection, ensuring that operations on this collection are
     *           executed within the context of this database.
     * @param collectionName A {@code String} that uniquely identifies the collection within the
     *                       database. This name is used to route operations to the correct
     *                       collection and should adhere to the database's naming conventions.
     * @param collectionOptions the options to apply to the command operation. If left blank the default collection
     * @param documentClass The {@code Class} object representing the schema of documents stored
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a client
     * DataAPIClient client = new DataAPIClient("token");
     * // Given a database
     * Database myDb = client.getDatabase("myDb");
     * // Initialize a collection with a working class
     * Collection<MyDocumentClass> myCollection = new Collection<>(myDb, "myCollectionName", MyDocumentClass.class);
     * }
     * </pre>
     */
    public Collection(Database db, String collectionName, CollectionOptions collectionOptions, Class<T> documentClass) {
        super(db.getApiEndpoint() + "/" + collectionName, collectionOptions);
        hasLength(collectionName, "collection name");
        notNull(documentClass, "documentClass");
        notNull(collectionOptions, "collection options");
        this.database       = db;
        this.collectionName = collectionName;
        this.documentClass  = documentClass;
        this.options.serializer(new DocumentSerializer());
        if (collectionOptions.getKeyspace() != null) {
            this.database.useKeyspace(collectionOptions.getKeyspace());
        }
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
        return getDatabase().getKeyspace();
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
     * DataApiCollection<Document> collection;
     * // Access its Definition
     * CollectionDefinition definition = collection.getDefinition();
     * System.out.println("Name=" + definition.getName());
     * CollectionOptions options = definition.getOptions();
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
    public CollectionDefinition getDefinition() {
        if (collectionDefinition == null) {
            collectionDefinition = database
                    .listCollections().stream()
                    .filter(col -> col.getName().equals(collectionName))
                    .findFirst()
                    .map(CollectionDescriptor::getOptions)
                    .orElseThrow(() -> new DataAPIException("[COLLECTION_NOT_EXIST] - Collection does not exist, " +
                            "collection name: '" + collectionName + "'", "COLLECTION_NOT_EXIST", null));
        }
        return collectionDefinition;
    }

    // --------------------------
    // ---   Insert*         ----
    // --------------------------

    /**
     * Inserts a single document into the collection as an atomic operation, ensuring that the
     * document is added in a single, indivisible step.
     *
     * <p><b>Note:</b> The document can optionally include an {@code _id} property, which serves as
     * its unique identifier within the collection. If the {@code _id} property is provided and it
     * matches the {@code _id} of an existing document in the collection, the insertion will fail
     * and an error will be raised. This behavior ensures that each document in the collection has
     * a unique identifier. If the {@code _id} property is not provided, the server will
     * automatically generate a unique {@code _id} for the document, ensuring its uniqueness within
     * the collection.</p>
     *
     * <p>The `_id` can be of multiple types, by default it can be any json scalar String, Number, $date. But
     * at the collection definition level you can enforce property `defaultId` to work with specialize ids.</p>
     * <ul>
     * <li>If {@code defaultId} is set to  {@code uuid}, ids will be uuid v4 {@link java.util.UUID}</li>
     * <li>If {@code defaultId} is set to  {@code objectId}, ids will be an {@link ObjectId}</li>
     * <li>If {@code defaultId} is set to  {@code uuidv6}, ids will be an {@link UUIDv6}</li>
     * <li>If {@code defaultId} is set to {@code uuidv7}, ids will be an {@link UUIDv7}</li>
     * </ul>
     *
     * <p>The method returns an {@code InsertOneResult} object, which provides details about the
     * outcome of the insertion operation. This object can be used to verify the success of the
     * operation and to access the {@code _id} of the inserted document, whether it was provided
     * explicitly or generated automatically.</p>
     *
     * @param document the document to be inserted into the collection. This parameter should represent
     *                 the document in its entirety. The {@code _id} field is optional and, if omitted,
     *                 will be automatically generated.
     * @return An {@code InsertOneResult} object that contains information about the result of the
     *         insertion operation, including the {@code _id} of the newly inserted document.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Create a document without id.
     * Document newDocument = new Document("name", "John Doe").append("age", 30);
     * InsertOneResult result = collection.insertOne(newDocument);
     * System.out.println("(generated) document id: " + result.getInsertedId());
     *
     * // Provide a document id
     * Document doc2 = Document.create("doc2").append("name", "John Doe").append("age", 30);
     * InsertOneResult result = collection.insertOne(doc2);
     * result.getInsertedId(); // will be "doc2"
     *
     * // More way to provide to populate ids.
     * Document doc3 = new Document("doc3");
     * Document doc4 = new Document().id("doc4");
     * Document doc5 = new Document().append("_id", "doc5");
     * }
     * </pre>
     */
    public final CollectionInsertOneResult insertOne(T document) {
        return insertOne(document, null);
    }

    /**
     * Inserts a single document into the collection as an atomic operation, ensuring that the
     * document is added in a single, indivisible step.
     *
     * <p><b>Note:</b> The document can optionally include an {@code _id} property, which serves as
     * its unique identifier within the collection. If the {@code _id} property is provided and it
     * matches the {@code _id} of an existing document in the collection, the insertion will fail
     * and an error will be raised. This behavior ensures that each document in the collection has
     * a unique identifier. If the {@code _id} property is not provided, the server will
     * automatically generate a unique {@code _id} for the document, ensuring its uniqueness within
     * the collection.</p>
     *
     * <p>The `_id` can be of multiple types, by default it can be any json scalar String, Number, $date. But
     * at the collection definition level you can enforce property `defaultId` to work with specialize ids.</p>
     * <ul>
     * <li>If {@code defaultId} is set to  {@code uuid}, ids will be uuid v4 {@link java.util.UUID}</li>
     * <li>If {@code defaultId} is set to  {@code objectId}, ids will be an {@link ObjectId}</li>
     * <li>If {@code defaultId} is set to  {@code uuidv6}, ids will be an {@link UUIDv6}</li>
     * <li>If {@code defaultId} is set to {@code uuidv7}, ids will be an {@link UUIDv7}</li>
     * </ul>
     *
     * <p>The method returns an {@code InsertOneResult} object, which provides details about the
     * outcome of the insertion operation. This object can be used to verify the success of the
     * operation and to access the {@code _id} of the inserted document, whether it was provided
     * explicitly or generated automatically.</p>
     *
     * @param document the document to be inserted into the collection. This parameter should represent
     *                 the document in its entirety. The {@code _id} field is optional and, if omitted,
     *                 will be automatically generated.
     * @param collectionInsertOneOptions
     *                 the options to apply to the insert operation. If left blank the default collection
     *                 options will be used. If collection option is blank DataAPIOptions will be used.
     * @return An {@code InsertOneResult} object that contains information about the result of the
     *         insertion operation, including the {@code _id} of the newly inserted document.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Create a document without id.
     * Document newDocument = new Document("name", "John Doe").append("age", 30);
     * InsertOneResult result = collection.insertOne(newDocument);
     * System.out.println("(generated) document id: " + result.getInsertedId());
     *
     * // Provide a document id
     * Document doc2 = Document.create("doc2").append("name", "John Doe").append("age", 30);
     * InsertOneResult result = collection.insertOne(doc2);
     * result.getInsertedId(); // will be "doc2"
     *
     * // More way to provide to populate ids.
     * Document doc3 = new Document("doc3");
     * Document doc4 = new Document().id("doc4");
     * Document doc5 = new Document().append("_id", "doc5");
     * }
     * </pre>
     */
    public final CollectionInsertOneResult insertOne(T document, CollectionInsertOneOptions collectionInsertOneOptions) {
        Assert.notNull(document, DOCUMENT);
        DataAPISerializer serializer = getSerializer();
        if (collectionInsertOneOptions != null && collectionInsertOneOptions.getSerializer() != null) {
            serializer = collectionInsertOneOptions.getSerializer();
        }
        return internalInsertOne(serializer.convertValue(document, Document.class), collectionInsertOneOptions);
    }

    /**
     * Asynchronously inserts a single document into the collection. This method provides the same functionality as
     * {@link #insertOne(Object)}, but it operates asynchronously, returning a {@link CompletableFuture} that
     * will be completed with the insertion result. Utilizing this method is beneficial for non-blocking operations,
     * allowing other tasks to proceed while the document insertion is being processed.
     *
     * <p>The asynchronous operation ensures that your application can remain responsive, making this method ideal for
     * applications requiring high throughput or for operations that do not need immediate completion confirmation.</p>
     *
     * <p>For details on the behavior, parameters, and return type, refer to the documentation of the synchronous
     * {@link #insertOne(Object)} method. This method inherits all the properties and behaviors of its synchronous
     * counterpart, including error handling and the generation or requirement of the {@code _id} field.</p>
     *
     * @param document The document to be inserted into the collection. The specifications regarding the document
     *                 structure and the {@code _id} field are the same as described in {@link #insertOne(Object)}.
     * @return A {@link CompletableFuture} that, upon completion, contains the result of the insert operation as an
     *         {@link CollectionInsertOneResult}. The completion may occur with a result in case of success or with an exception
     *         in case of failure.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Asynchronously inserting a document
     * Document newDocument = new Document().append("name", "Async User").append("age", 34);
     * CompletableFuture<InsertOneResult> futureResult = collection.insertOneAsync(newDocument);
     * futureResult.thenAccept(result -> System.out.println("Inserted document id: " + result.getInsertedId()))
     *             .exceptionally(error -> { System.err.println("Insertion failed: " + error.getMessage()); return null; });
     * }
     * </pre>
     */
    public final CompletableFuture<CollectionInsertOneResult> insertOneAsync(T document) {
        return CompletableFuture.supplyAsync(() -> insertOne(document));
    }

    /**
     * Asynchronous implementation of {@link #insertOne(Object, CollectionInsertOneOptions)}.
     *
     * @param document
     *      document to insert
     * @param options
     *      the options to apply to the insert operation. If left blank the default collection
     *      options will be used. If collection option is blank DataAPIOptions will be used.
     * @return
     *      result for insertion
     */
    public final CompletableFuture<CollectionInsertOneResult> insertOneAsync(T document, CollectionInsertOneOptions options) {
        return CompletableFuture.supplyAsync(() -> insertOne(document, options));
    }

    /**
     * Insert a single document in the collection in an atomic internal operation.
     *
     * @param document
     *      the document to be inserted.
     * @param collectionInsertOneOptions
     *      override default configuration for the insert operation.
     * @return
     *      object wrapping the returned identifier
     */
    private CollectionInsertOneResult internalInsertOne(Document document, CollectionInsertOneOptions collectionInsertOneOptions) {
        Assert.notNull(document, DOCUMENT);
        Command insertOne = Command.create("insertOne").withDocument(document);
        Object documentId = runCommand(insertOne, collectionInsertOneOptions)
                .getStatus().getInsertedIds()
                .get(0);
        return new CollectionInsertOneResult(unmarshallDocumentId(documentId));
    }

    /**
     * Unmarshall the document id.
     *
     * @param id
     *      object id returned by the server
     * @return
     *      unmarshalled id
     */
    @SuppressWarnings("unchecked")
    private Object unmarshallDocumentId(Object id) {
        if (id instanceof Map) {
            // only maps will required to be unmarshalled
            Map<String, Object> mapId = (Map<String, Object>) id;
            if (mapId.containsKey(DataAPIKeywords.DATE.getKeyword())) {
                // eJson date
                return  Instant.ofEpochMilli((Long) mapId.get(DataAPIKeywords.DATE.getKeyword()));
            }
            if (mapId.containsKey(DataAPIKeywords.UUID.getKeyword())) {
                // defaultId with UUID
                UUID uid = UUID.fromString((String) mapId.get(DataAPIKeywords.UUID.getKeyword()));

                if (getDefinition().getDefaultId() != null) {
                    CollectionDefaultIdTypes defaultIdType = getDefinition().getDefaultId().getType();
                    return switch (defaultIdType) {
                        case UUIDV6 -> new UUIDv6(uid);
                        case UUIDV7 -> new UUIDv7(uid);
                        default -> uid;
                    };
                }
                throw new IllegalStateException("Returned is is a UUID, but no defaultId is set in the collection definition.");
            }
            if (mapId.containsKey(DataAPIKeywords.OBJECT_ID.getKeyword())) {
                // defaultId with ObjectId
                return new ObjectId((String) mapId.get(DataAPIKeywords.OBJECT_ID.getKeyword()));
            }
            throw new IllegalArgumentException("Cannot marshall id " + id);
        }
        return id;
    }

    /**
     * Inserts a batch of documents into the collection concurrently, optimizing the insertion process for
     * large datasets. This method provides a powerful mechanism to insert multiple documents with customizable
     * concurrency levels and batch sizes, while also ensuring error handling and performance optimization.
     *
     * <p><b>Validation:</b> The method validates the input documents list for nullity and emptiness. It also
     * checks each document within the list to ensure none are null, throwing an {@link IllegalArgumentException}
     * if these conditions are not met.</p>
     *
     * <p><b>Concurrency and Ordering:</b> If concurrent insertion is requested with ordered inserts (via
     * {@code options}), the method throws an {@link IllegalArgumentException}, as ordered operations cannot
     * be performed concurrently.</p>
     *
     * <p><b>Chunk Size and Maximum Insertions:</b> The method checks if the specified chunk size exceeds the
     * maximum number of documents allowed for insertion in a single operation, throwing an
     * {@link IllegalArgumentException} if this limit is breached.</p>
     *
     * <p>Documents are then split into chunks, each processed in parallel, according to the concurrency level
     * specified in {@code options}. The results of these insertions are aggregated into a single
     * {@link CollectionInsertManyResult}.</p>
     *
     * <p><b>Timeout Handling:</b> The method attempts to complete all insertion tasks within the specified
     * timeout. If tasks do not complete in time, a {@link TimeoutException} is thrown.</p>
     *
     * <p><b>Error Handling:</b> Exceptions encountered during insertion or result aggregation are captured,
     * and a {@link RuntimeException} is thrown, indicating an issue with merging results into a single
     * {@link CollectionInsertManyResult}.</p>
     *
     * <p><b>Example usage:</b> Inserting a list of 100 documents </p>
     * <pre>
     * {@code
     * InsertManyOptions options = InsertManyOptions.builder()
     *   .ordered(false)     // required for concurrent processing
     *   .withConcurrency(5) // recommended
     *   .withChunkSize(20)  // maximum chunk size is 20
     *   .withTimeout(100)   // global timeout
     *   .build();
     * List<Document> documents = new ArrayList<>();
     * for (int i = 0; i < 100; i++) {
     *     documents.add(new Document().append("key" + i, "value" + i));
     * }
     * InsertManyResult result = collection.insertMany(documents, options);
     * System.out.println("Inserted document count: " + result.getInsertedIds().size());
     * }
     * </pre>
     *
     * <p><b>Performance Monitoring:</b> Logs the total response time for the insert many operation, aiding
     * in performance analysis and optimization efforts.</p>
     *
     * @param documents A list of documents to be inserted. Must not be null or empty, and no document should
     *                  be null.
     * @param options   Detailed options for the insert many operation, including concurrency level, chunk size,
     *                  and whether the inserts should be ordered.
     * @return An {@link CollectionInsertManyResult} object containing the IDs of all successfully inserted documents.
     * @throws IllegalArgumentException if the documents list is null or empty, any document is null, or if
     *                                  the options specified are invalid.
     * @throws RuntimeException if there is an error in merging the results of concurrent insertions.
     */
    public CollectionInsertManyResult insertMany(List<? extends T> documents, CollectionInsertManyOptions options) {
        Assert.isTrue(documents != null && !documents.isEmpty(), "documents list cannot be null or empty");
        Assert.notNull(options, "insertMany options cannot be null");
        if (options.getConcurrency() > 1 && options.isOrdered()) {
            throw new IllegalArgumentException("Cannot run ordered insert_many concurrently.");
        }
        if (options.getChunkSize() > MAX_CHUNK_SIZE) {
            throw new IllegalArgumentException("Cannot insert more than " + MAX_CHUNK_SIZE + " at a time.");
        }
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
        List<Future<CollectionInsertManyResult>> futures = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += options.getChunkSize()) {
            futures.add(executor.submit(getInsertManyResultCallable(documents, options, i)));
        }
        executor.shutdown();

        // Grouping All Insert ids in the same list.
        CollectionInsertManyResult finalResult = new CollectionInsertManyResult();
        try {
            for (Future<CollectionInsertManyResult> future : futures) {
                CollectionInsertManyResult res = future.get();
                finalResult.getInsertedIds().addAll(res.getInsertedIds());
                finalResult.getDocumentResponses().addAll(res.getDocumentResponses());
            }
            // Set a default timeouts for the overall operation
            long totalTimeout = this.options.getTimeout();
            if (options.getDataAPIClientOptions() != null) {
                totalTimeout = options.getTimeout();
            }
            if (executor.awaitTermination(totalTimeout, TimeUnit.MILLISECONDS)) {
                log.debug(magenta(".[total insertMany.responseTime]") + "=" + yellow("{}") + " millis.",
                        System.currentTimeMillis() - start);
            } else {
                throw new DataAPIException(ERROR_CODE_TIMEOUT, "Request did not complete within ");
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

    /**
     * Asynchronously inserts a batch of documents into the collection with customizable insertion options.
     * This method is the asynchronous counterpart to {@link #insertMany(List, CollectionInsertManyOptions)}, allowing
     * for non-blocking operations. It employs default or specified {@link CollectionInsertManyOptions} to optimize the
     * insertion process for large datasets, utilizing concurrency and batch processing to enhance performance.
     *
     * <p>Utilizing {@link CompletableFuture}, this method facilitates the insertion of multiple documents
     * without halting the execution of your application, making it well-suited for applications requiring
     * high throughput or responsiveness. For scenarios necessitating specific insertion behaviors, such as
     * concurrency levels and chunk sizes, the provided {@code options} parameter enables fine-tuned control
     * over the asynchronous operation.</p>
     *
     * <p>This method inherits all the validation, chunking, and result aggregation logic from its synchronous
     * counterpart, ensuring consistent behavior and error handling, while extending functionality to support
     * asynchronous execution patterns.</p>
     *
     * <p><b>Usage:</b> Recommended for inserting large numbers of documents or when the application's
     * workflow benefits from non-blocking operations. For simpler use cases or default settings, the overload
     * without {@code options} provides a more straightforward approach.</p>
     *
     * <p><b>Example usage:</b> Asynchronously inserting a list of documents with custom options.</p>
     * <pre>
     * {@code
     * List<Document> documents = new ArrayList<>();
     * for (int i = 0; i < 100; i++) {
     *     documents.add(new Document().append("key" + i, "value" + i));
     * }
     * InsertManyOptions options = new InsertManyOptions().setConcurrency(5).setChunkSize(20);
     * CompletableFuture<InsertManyResult> futureResult = collection.insertManyAsync(documents, options);
     * futureResult.thenAccept(result -> System.out.println("Inserted document count: " + result.getInsertedIds().size()))
     *             .exceptionally(error -> { System.err.println("Insertion failed: " + error.getMessage()); return null; });
     * }
     * </pre>
     *
     * @param documents A list of documents to be inserted. The list must not be null or empty, and no document
     *                  should be null.
     * @param options   Detailed options for the insert many operation, allowing customization of concurrency
     *                  level, chunk size, and insertion order.
     * @return A {@link CompletableFuture} that, upon completion, contains the {@link CollectionInsertManyResult} indicating
     *         the outcome of the insert operation. The future may complete normally with the insertion result
     *         or exceptionally in case of an error.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     */
    public CompletableFuture<CollectionInsertManyResult> insertManyAsync(List<? extends T> documents, CollectionInsertManyOptions options) {
        return CompletableFuture.supplyAsync(() -> insertMany(documents, options));
    }

    /**
     * Inserts a batch of documents into the collection using default insertion options. This method is a
     * simplified version of {@link #insertMany(List, CollectionInsertManyOptions)}, intended for use cases where
     * default settings for concurrency, chunk size, and insertion order are sufficient. It provides an
     * efficient way to insert multiple documents concurrently, optimizing the insertion process with
     * predefined settings.
     *
     * <p>The default {@link CollectionInsertManyOptions} used by this method assumes non-concurrent (sequential)
     * insertion, with no specific chunk size or timeout constraints. This is suitable for general use
     * cases where the simplicity of invocation is prioritized over the customization of insertion
     * parameters. For more advanced control over the insertion process, including the ability to specify
     * concurrency levels, chunk sizes, and operation timeouts, use the overloaded
     * {@link #insertMany(List, CollectionInsertManyOptions)} method.</p>
     *
     * <p>This method leverages the same underlying insertion logic as its overloaded counterpart,
     * ensuring consistent behavior and error handling. It automatically handles validation of the
     * input documents list, chunking of documents based on default settings, and aggregation of
     * insertion results into a single {@link CollectionInsertManyResult}.</p>
     *
     * <p><b>Usage:</b> Ideal for inserting a collection of documents without the need for custom
     * insertion options. Simplifies the insertion process for basic use cases.</p>
     *
     * @param documents A list of documents to be inserted. Must not be null or empty, and no document should
     *                  be null.
     * @return An {@link CollectionInsertManyResult} object containing the IDs of all successfully inserted documents.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     * @throws RuntimeException if there is an error in merging the results of concurrent insertions.
     */
    public CollectionInsertManyResult insertMany(List<? extends T> documents) {
        return insertMany(documents, new CollectionInsertManyOptions());
    }

    /**
     * Inserts a batch of documents into the collection using default insertion options. This method is a
     * simplified version of {@link #insertMany(List, CollectionInsertManyOptions)}, intended for use cases where
     * default settings for concurrency, chunk size, and insertion order are sufficient. It provides an
     * efficient way to insert multiple documents concurrently, optimizing the insertion process with
     * predefined settings.
     *
     * <p>The default {@link CollectionInsertManyOptions} used by this method assumes non-concurrent (sequential)
     * insertion, with no specific chunk size or timeout constraints. This is suitable for general use
     * cases where the simplicity of invocation is prioritized over the customization of insertion
     * parameters. For more advanced control over the insertion process, including the ability to specify
     * concurrency levels, chunk sizes, and operation timeouts, use the overloaded
     * {@link #insertMany(List, CollectionInsertManyOptions)} method.</p>
     *
     * <p>This method leverages the same underlying insertion logic as its overloaded counterpart,
     * ensuring consistent behavior and error handling. It automatically handles validation of the
     * input documents list, chunking of documents based on default settings, and aggregation of
     * insertion results into a single {@link CollectionInsertManyResult}.</p>
     *
     * <p><b>Usage:</b> Ideal for inserting a collection of documents without the need for custom
     * insertion options. Simplifies the insertion process for basic use cases.</p>
     *
     * @param documents A list of documents to be inserted. Must not be null or empty, and no document should
     *                  be null.
     * @return An {@link CollectionInsertManyResult} object containing the IDs of all successfully inserted documents.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     * @throws RuntimeException if there is an error in merging the results of concurrent insertions.
     */
    @SafeVarargs
    public final CollectionInsertManyResult insertMany(T... documents) {
        return insertMany(Arrays.asList(documents), new CollectionInsertManyOptions());
    }

    /**
     * Asynchronously inserts a batch of documents into the collection using default insertion options. This method
     * provides an asynchronous alternative to {@link #insertMany(List)}, facilitating non-blocking operations while
     * employing a simplified insertion process suited for general use cases.
     *
     * <p>Utilizing {@link CompletableFuture}, this method allows the insertion of multiple documents without interrupting
     * the application's execution flow. It is particularly useful in scenarios requiring high throughput or when maintaining
     * application responsiveness is critical. The default insertion settings are applied, simplifying the operation and
     * making it accessible for basic insertion needs without the necessity for custom configuration.</p>
     *
     * <p>This method inherits the core logic and validations from its synchronous counterpart, ensuring consistent behavior
     * and error handling. It automatically manages the input documents list, applying default options for chunking and
     * concurrency, and aggregates the results into a single {@link CollectionInsertManyResult} asynchronously.</p>
     *
     * <p><b>Usage:</b> Ideal for applications that benefit from asynchronous document insertion, especially when inserting
     * a large number of documents under default settings. This method simplifies asynchronous batch insertions, making it
     * straightforward to integrate into existing workflows.</p>
     *
     * <p><b>Example usage:</b> Asynchronously inserting a list of 100 documents using default options.</p>
     * <pre>
     * {@code
     * List<Document> documents = new ArrayList<>();
     * for (int i = 0; i < 100; i++) {
     *     documents.add(new Document().append("key" + i, "value" + i));
     * }
     * CompletableFuture<InsertManyResult> futureResult = collection.insertManyAsync(documents);
     * futureResult.thenAccept(result -> System.out.println("Inserted document count: " + result.getInsertedIds().size()))
     *             .exceptionally(error -> { System.err.println("Insertion failed: " + error.getMessage()); return null; });
     * }
     * </pre>
     *
     * @param documents A list of documents to be inserted. Must not be null or empty, and no document within the list should
     *                  be null.
     * @return A {@link CompletableFuture} that, upon completion, contains the {@link CollectionInsertManyResult} indicating the
     *         outcome of the insert operation. The future may complete with the insertion results or exceptionally in
     *         case of an error.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     */
    public CompletableFuture<CollectionInsertManyResult> insertManyAsync(List<? extends T> documents) {
        return CompletableFuture.supplyAsync(() -> insertMany(documents));
    }

    /**
     * Execute a 1 for 1 call to the Data API.
     *
     * @param documents
     *      list of documents to be inserted
     * @param collectionInsertManyOptions
     *      options for insert many (chunk size and insertion order).
     * @param start
     *      offset in global list
     * @return
     *      insert many result for a paged call
     */
    private Callable<CollectionInsertManyResult> getInsertManyResultCallable(List<? extends T> documents, CollectionInsertManyOptions collectionInsertManyOptions, int start) {
        int end = Math.min(start + collectionInsertManyOptions.getChunkSize(), documents.size());
        return () -> {
            log.debug("Insert block (" + cyan("size={}") + ") in collection {}", end - start, green(getCollectionName()));

            Command insertMany = new Command("insertMany")
                    .withDocuments(documents.subList(start, end))
                    .withOptions(new Document()
                            .append(INPUT_ORDERED, collectionInsertManyOptions.isOrdered())
                            .append(INPUT_RETURN_DOCUMENT_RESPONSES, collectionInsertManyOptions.isReturnDocumentResponses()));

            DataAPIStatus status = runCommand(insertMany, collectionInsertManyOptions).getStatus();
            CollectionInsertManyResult result = new CollectionInsertManyResult();
            if (status.getInsertedIds()!= null && !status.getInsertedIds().isEmpty()) {
                result.setInsertedIds(status.getInsertedIds().stream().map(this::unmarshallDocumentId).toList());
            }
            if (status.getDocumentResponses()!= null && !status.getDocumentResponses().isEmpty()) {
                result.setDocumentResponses(status.getDocumentResponses());
            }
            return result;
        };
    }

    // --------------------------
    // ---   Find*           ----
    // --------------------------

    /**
     * Attempts to find a single document within the collection that matches the given filter criteria. This method
     * efficiently locates the first document that fulfills the specified conditions, making it an optimal choice for
     * queries where a unique identifier or specific characteristics are used to identify a document. Its efficiency
     * stems from the ability to halt the search as soon as a matching document is found, potentially avoiding a full
     * collection scan.
     *
     * <p>Utilizing a {@link Filter} instance to articulate the search criteria, this method sifts through the collection
     * to find a document that aligns with the provided conditions. The filter defines the parameters that a document
     * must satisfy to be deemed a match, encompassing a wide range of possible attributes and values specific to the
     * document structure and contents within the collection.</p>
     *
     * <p>In cases where the search does not yield a matching document, this method returns an empty {@link java.util.Optional},
     * signifying the absence of a compatible document. This design choice facilitates more graceful error handling,
     * allowing callers to easily distinguish between the presence and absence of a match without resorting to exception
     * handling for non-existent documents. Consequently, client code can implement more robust and cleaner retrieval
     * logic by leveraging the {@link java.util.Optional} pattern.</p>
     *
     * <p><b>Example usage:</b></p>
     * <pre>
     * {@code
     *  // Given a collection
     *  DataApiCollection<Document> collection;
     *  // Assuming a Document in the collection with an id field
     *  Document doc = new Document().id(1).append("name", "John Doe");
     *  // To find the document with the id 1
     *  Optional<Document> foundDoc = collection.findOne(Filters.eq("_id", 1));
     *  foundDoc.ifPresent(System.out::println);
     * }
     * </pre>
     *
     * @param filter The {@link Filter} instance encapsulating the search criteria used to pinpoint the desired document.
     *               This object specifies the exact conditions that must be met for a document to be selected as a match.
     * @return An {@link java.util.Optional} encapsulating the found document, if any, that meets the filter criteria.
     *         If no document matches the specified conditions, an empty {@link java.util.Optional} is returned, ensuring
     *         that retrieval operations can be performed safely without the concern of {@link java.util.NoSuchElementException}.
     */
    public Optional<T> findOne(Filter filter) {
        return findOne(filter, new CollectionFindOneOptions());
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
     * <p><b>Example usage:</b></p>
     * <pre>
     * {@code
     *  // Given a collection
     *  DataApiCollection<Document> collection;
     *  // Assuming a Document in the collection with an id field
     *  Document doc = new Document().id(1).append("name", "John Doe");
     *  // To find the document with the id 1
     *  FindOneOptions options2 = FindOneOptions.builder()
     *    .withIncludeSimilarity()     // return similarity in vector search
     *    .projections("_id", "name")  // return a subset of fields
     *    .build();
     *  Optional<Document> foundDoc = collection.findOne(Filters.eq("_id", 1), );
     *  foundDoc.ifPresent(System.out::println);
     * }
     * </pre>
     *
     * @param filter The {@link Filter} instance containing the criteria used to identify the desired document.
     *               It specifies the conditions that a document must meet to be considered a match.
     * @param findOneOptions The {@link CollectionFindOneOptions} instance containing additional options for the find operation,
     * @return An {@link Optional} that contains the found document if one exists that matches
     *         the filter criteria. Returns an empty {@link Optional} if no matching document is found,
     *         enabling safe retrieval operations without the risk of {@link java.util.NoSuchElementException}.
     */
    public Optional<T> findOne(Filter filter, CollectionFindOneOptions findOneOptions) {
        notNull(findOneOptions, ARG_OPTIONS);
        Command findOne = Command
                .create("findOne")
                .withFilter(filter)
                .withSort(findOneOptions.getSortArray())
                .withProjection(findOneOptions.getProjectionArray())
                .withOptions(new Document()
                  .appendIfNotNull(INPUT_INCLUDE_SIMILARITY, findOneOptions.includeSimilarity())
                  .appendIfNotNull(INPUT_INCLUDE_SORT_VECTOR, findOneOptions.includeSortVector())
                );

        return Optional.ofNullable(
                runCommand(findOne, findOneOptions)
                        .getData().getDocument()
                        .map(getDocumentClass()));
    }

    /**
     * Syntax sugar to provide a findOne command without a filter @see {@link #findOne(Filter, CollectionFindOneOptions)}.
     *
     * @param findOneOptions
     *      find one without a filter
     * @return An {@link Optional} that contains the found document if one exists that matches
     *         the filter criteria. Returns an empty {@link Optional} if no matching document is found,
     *         enabling safe retrieval operations without the risk of {@link java.util.NoSuchElementException}.
     */
    public Optional<T> findOne(CollectionFindOneOptions findOneOptions) {
        return findOne(null, findOneOptions);
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
    public CompletableFuture<Optional<T>> findOneASync(Filter filter) {
        return CompletableFuture.supplyAsync(() -> findOne(filter));
    }

    /**
     * Asynchronously attempts to find a single document within the collection that matches the given filter criteria,
     * utilizing the specified {@link CollectionFindOneOptions} for the query. This method offers a non-blocking approach to
     * querying the database, making it well-suited for applications requiring efficient I/O operations without
     * compromising the responsiveness of the application.
     *
     * <p>By executing the search operation in an asynchronous manner, this method allows other tasks to proceed
     * concurrently, effectively utilizing system resources and improving application throughput. The query leverages
     * a {@link Filter} instance to define the search criteria, and {@link CollectionFindOneOptions} to specify query
     * customizations, such as projection or sort parameters.</p>
     *
     * <p>In cases where no document matches the filter, the method returns a {@link CompletableFuture} completed with
     * an empty {@link java.util.Optional}, thus avoiding exceptions for non-existent documents. This behavior ensures
     * a more graceful handling of such scenarios, allowing for cleaner and more robust client code by leveraging
     * the {@link java.util.Optional} pattern within asynchronous workflows.</p>
     *
     * @param filter  The {@link Filter} instance encapsulating the criteria used to identify the desired document.
     *                It defines the conditions that a document must meet to be considered a match.
     * @param findOneOptions The {@link CollectionFindOneOptions} providing additional query configurations such as projection
     *                and sort criteria to tailor the search operation.
     * @return A {@link CompletableFuture} that, upon completion, contains an {@link Optional}
     *         with the found document if one exists matching the filter criteria. If no matching document is found,
     *         a completed future with an empty {@link Optional} is returned, facilitating safe asynchronous retrieval.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Filter filter = Filters.eq("id", 1);
     * FindOneOptions options = FindOneOptions.builder().projection("name");
     * CompletableFuture<Optional<Document>> futureDoc = collection.findOneASync(filter, options);
     * futureDoc.thenAccept(doc -> doc.ifPresent(System.out::println));
     * }
     * </pre>
     */
    public CompletableFuture<Optional<T>> findOneASync(Filter filter, CollectionFindOneOptions findOneOptions) {
        return CompletableFuture.supplyAsync(() -> findOne(filter, findOneOptions));
    }


    /**
     * Retrieves all documents in the collection.
     * <p>
     * This method returns an iterable interface that allows iterating over all documents in the collection,
     * without applying any filters. It leverages the default {@link CollectionFindOptions} for query execution.
     * </p>
     *
     * @return A {@link FindIterable} for iterating over all documents in the collection.
     */
    public FindIterable<T> findAll() {
        return find(null, new CollectionFindOptions());
    }

    /**
     * Retrieves all documents in the collection.
     * <p>
     * This method returns an iterable interface that allows iterating over all documents in the collection,
     * without applying any filters. It leverages the default {@link CollectionFindOptions} for query execution.
     * </p>
     *
     * @return A {@link CollectionCursor} for iterating over all documents in the collection.
     */
    public CollectionCursor<T> findAllWithCursor() {
        return new CollectionCursor<T>(this, null, new CollectionFindOptions());
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
    public Optional<T> findById(Object id) {
        return findOne(Filters.eq(id));
    }

    /**
     * Finds all documents in the collection.
     *
     * @param filter
     *      the query filter
     * @return
     *      the find iterable interface
     */
    public FindIterable<T> find(Filter filter) {
        return find(filter, new CollectionFindOptions());
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
    public FindIterable<T> find(Filter filter, CollectionFindOptions options) {
        return new FindIterable<>(this, filter, options);
    }

    /**
     * Finds all documents in the collection.
     * @param options
     *      options of find one
     * @return
     *      the find iterable interface
     */
    public FindIterable<T> find(CollectionFindOptions options) {
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
     * @param options The {@link CollectionFindOptions} providing additional query parameters, such as sorting and pagination.
     * @return A {@link Page} object containing the documents that match the query, along with pagination information.
     */
    public Page<T> findPage(Filter filter, CollectionFindOptions options) {
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
                        .map(d -> d.map(getDocumentClass()))
                        .collect(Collectors.toList()), sortVector);
    }

    /**
     * Executes a paginated 'find' query on the collection using the specified filter and find options asynchronously.
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
     * @param options The {@link CollectionFindOptions} providing additional query parameters, such as sorting and pagination.
     * @return A {@link Page} object containing the documents that match the query, along with pagination information.
     */
    public CompletableFuture<Page<T>> findPageASync(Filter filter, CollectionFindOptions options) {
        return CompletableFuture.supplyAsync(() -> findPage(filter, options));
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
     * @param <F>
     *      the target type of the iterable.
     * @return
     *      an iterable of distinct values
     */
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
     */
    public <F> CollectionDistinctIterable<T, F> distinct(String fieldName, Filter filter, Class<F> resultClass) {
        return new CollectionDistinctIterable<>(this, fieldName, filter, resultClass);
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
     * Calling an estimatedDocumentCount with default options. @see {@link #estimatedDocumentCount(EstimatedCountDocumentsOptions)}
     *
     * @return the estimated number of documents in the collection.
     */
    public long estimatedDocumentCount() {
       return estimatedDocumentCount(new EstimatedCountDocumentsOptions());
    }

    /**
     * Executes the "estimatedDocumentCount" command to estimate the number of documents
     * in a collection.
     * <p>
     * This method sends a command to estimate the document count and parses the count
     * from the command's response. It handles the execution of the command and the extraction
     * of the document count from the response.
     * </p>
     * @param options
     *     the options to apply to the operation
     * @return the estimated number of documents in the collection.
     */
    public long estimatedDocumentCount(EstimatedCountDocumentsOptions options) {
        Command command = new Command("estimatedDocumentCount");
        // Run command
        DataAPIResponse response = runCommand(command, options);
        // Build Result
        return response.getStatus().getInteger(RESULT_COUNT);
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
     * @param options
     *      overriding options for the count operation.
     * @return
     *      The number of documents in the collection.
     * @throws TooManyDocumentsToCountException
     *      If the number of documents counted exceeds the provided limit.
     */
    public int countDocuments(Filter filter, int upperBound, CountDocumentsOptions options)
    throws TooManyDocumentsToCountException {
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
            throw new TooManyDocumentsToCountException();
        } else if (count > upperBound) {
            throw new TooManyDocumentsToCountException(upperBound);
        }
        return count;
    }

    /**
     * Implementation of the @see {@link #countDocuments(Filter, int, CountDocumentsOptions)} method with default options.
     * @param filter
     *      filter to count
     * @param upperBound
     *      The maximum number of documents to count. It must be lower than the maximum limit accepted by the Data API.
     * @return
     *      The number of documents in the collection.
     * @throws TooManyDocumentsToCountException
     *      If the number of documents counted exceeds the provided limit.
     */
    public int countDocuments(Filter filter, int upperBound)
    throws TooManyDocumentsToCountException {
        return countDocuments(filter, upperBound, new CountDocumentsOptions());
    }

    // ----------------------------
    // ---   Delete            ----
    // ----------------------------

    /**
     * Removes at most one document from the collection that matches the given filter.
     * If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @return
     *      the result of the remove one operation
     */
    public CollectionDeleteResult deleteOne(Filter filter) {
        return deleteOne(filter, new CollectionDeleteOneOptions());
    }

    /**
     * Removes at most one document from the collection that matches the given filter.
     * If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @param collectionDeleteOneOptions
     *      the option to driver the deletes (here sort)
     * @return
     *      the result of the remove one operation
     *
     */
    public CollectionDeleteResult deleteOne(Filter filter, CollectionDeleteOneOptions collectionDeleteOneOptions) {
        Command deleteOne = Command
                .create("deleteOne")
                .withFilter(filter)
                .withSort(collectionDeleteOneOptions.getSortArray());

        DataAPIResponse apiResponse = runCommand(deleteOne, collectionDeleteOneOptions);
        int deletedCount = apiResponse.getStatus().getInteger(RESULT_DELETED_COUNT);
        return new CollectionDeleteResult(deletedCount);
    }

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @param options
     *      the options to apply to the operation
     * @return
     *      the result of the remove many operation
     */
    public CollectionDeleteResult deleteMany(Filter filter, CollectionDeleteManyOptions options) {
        AtomicInteger totalCount = new AtomicInteger(0);
        boolean moreData = false;
        do {
            Command deleteMany = Command
                    .create("deleteMany")
                    .withFilter(filter);
            DataAPIResponse apiResponse = runCommand(deleteMany, options);
            DataAPIStatus status = apiResponse.getStatus();
            if (status != null) {
                if (status.containsKey(RESULT_DELETED_COUNT)) {
                    totalCount.addAndGet(status.getInteger(RESULT_DELETED_COUNT));
                }
                moreData = status.containsKey(RESULT_MORE_DATA);
            }
        } while(moreData);
        return new CollectionDeleteResult(totalCount.get());
    }

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @return
     *      the result of the remove many operation
     */
    public CollectionDeleteResult deleteMany(Filter filter) {
        return deleteMany(filter, new CollectionDeleteManyOptions()
                .timeout(Duration.ofSeconds(30)));
    }

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @return
     *      the result of the remove many operation
     */
    public CollectionDeleteResult deleteAll() {
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
        return getDatabase().collectionExists(getCollectionName());
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
    public Optional<T> findOneAndReplace(Filter filter, T replacement) {
        return findOneAndReplace(filter, replacement, new CollectionFindOneAndReplaceOptions());
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
    public Optional<T> findOneAndReplace(Filter filter, T replacement, CollectionFindOneAndReplaceOptions options) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withSort(options.getSortArray())
                .withProjection(options.getProjectionArray())
                .withOptions(new Document()
                        .appendIfNotNull(INPUT_UPSERT, options.upsert())
                        .appendIfNotNull(INPUT_RETURN_DOCUMENT, options.returnDocument())
                );

        DataAPIResponse res = runCommand(findOneAndReplace, options);
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
    public CollectionUpdateResult replaceOne(Filter filter, T replacement) {
        return replaceOne(filter, replacement, new CollectionReplaceOneOptions());
    }

    /**
     * Replace a document in the collection according to the specified arguments.
     *
     * @param filter
     *      the query filter to apply the replace operation
     * @param replacement
     *      the replacement document
     * @param collectionReplaceOneOptions
     *      the options to apply to the replace operation
     * @return
     *      the result of the replace one operation
     */
    public CollectionUpdateResult replaceOne(Filter filter, T replacement, CollectionReplaceOneOptions collectionReplaceOneOptions) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withOptions(new Document()
                        .appendIfNotNull(INPUT_UPSERT, collectionReplaceOneOptions.upsert())
                        .append(INPUT_RETURN_DOCUMENT, ReturnDocument.BEFORE.getKey())
                );

        // Execute the `findOneAndReplace`
        FindOneAndReplaceResult<T> res = executeFindOneAndReplace(findOneAndReplace, collectionReplaceOneOptions);

        // Parse the result for a replace one
        CollectionUpdateResult result = new CollectionUpdateResult();
        result.setMatchedCount(res.getMatchedCount());
        result.setModifiedCount(res.getModifiedCount());
        if (res.getDocument() != null) {
            Document doc = getSerializer().convertValue(res.getDocument(), Document.class);
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
    private FindOneAndReplaceResult<T> executeFindOneAndReplace(Command cmd, BaseOptions<?> options) {
        // Run Command
        DataAPIResponse apiResponse = runCommand(cmd, options);
        // Parse Command Result
        FindOneAndReplaceResult<T> result = new FindOneAndReplaceResult<>();
        if (apiResponse.getData() == null) {
            throw new UnexpectedDataAPIResponseException(cmd, apiResponse,"Faulty response from find_one_and_replace API command.");
        }
        if (apiResponse.getData().getDocument() != null) {
            result.setDocument(apiResponse
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        DataAPIStatus status = apiResponse.getStatus();
        if (status != null) {
            if (status.containsKey(RESULT_MATCHED_COUNT)) {
                result.setMatchedCount(status.getInteger(RESULT_MATCHED_COUNT));
            }
            if (status.containsKey(RESULT_MODIFIED_COUNT)) {
                result.setModifiedCount(status.getInteger(RESULT_MODIFIED_COUNT));
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
    public Optional<T> findOneAndUpdate(Filter filter, Update update) {
        return findOneAndUpdate(filter, update, new CollectionFindOneAndUpdateOptions());
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
    public Optional<T> findOneAndUpdate(Filter filter, Update update, CollectionFindOneAndUpdateOptions options) {
        notNull(update, ARG_UPDATE);
        notNull(options, ARG_OPTIONS);
        Command cmd = Command
                .create("findOneAndUpdate")
                .withFilter(filter)
                .withUpdate(update)
                .withSort(options.getSortArray())
                .withProjection(options.getProjectionArray())
                .withOptions(new Document()
                        .appendIfNotNull(INPUT_UPSERT, options.upsert())
                        .append(INPUT_RETURN_DOCUMENT, options.returnDocument())
                );

        DataAPIResponse res = runCommand(cmd, options);
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
    public CollectionUpdateResult updateOne(Filter filter, Update update) {
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
    public CollectionUpdateResult updateOne(Filter filter, Update update, UpdateOneOptions updateOptions) {
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
    private static CollectionUpdateResult getUpdateResult(DataAPIResponse apiResponse) {
        CollectionUpdateResult result = new CollectionUpdateResult();
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
    public CollectionUpdateResult updateMany(Filter filter, Update update) {
        return updateMany(filter, update, new CollectionUpdateManyOptions());
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
    public CollectionUpdateResult updateMany(Filter filter, Update update, CollectionUpdateManyOptions options) {
        notNull(update, ARG_UPDATE);
        notNull(options, ARG_OPTIONS);
        String nextPageState = null;
        CollectionUpdateResult result = new CollectionUpdateResult();
        result.setMatchedCount(0);
        result.setModifiedCount(0);
        do {
            Command cmd = Command
                    .create("updateMany")
                    .withFilter(filter)
                    .withUpdate(update)
                    .withOptions(new Document()
                            .appendIfNotNull(INPUT_UPSERT, options.upsert())
                            .appendIfNotNull(INPUT_PAGE_STATE, nextPageState));
            DataAPIResponse res = runCommand(cmd, options);
            // Data
            if (res.getData() != null) {
                nextPageState = res.getData().getNextPageState();
            }
            // Status
            DataAPIStatus status = res.getStatus();
            if (status.containsKey(RESULT_MATCHED_COUNT)) {
                result.setMatchedCount(result.getMatchedCount() + status.getInteger(RESULT_MATCHED_COUNT));
            }
            if (status.containsKey(RESULT_MODIFIED_COUNT)) {
                result.setModifiedCount(result.getModifiedCount() + status.getInteger(RESULT_MODIFIED_COUNT));
            }
            if (status.containsKey(RESULT_UPSERTED_ID)) {
                result.setUpsertedId(status.getInteger(RESULT_UPSERTED_ID));
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
    public Optional<T> findOneAndDelete(Filter filter) {
        return findOneAndDelete(filter, new CollectionFindOneAndDeleteOptions());
    }

    /**
     * Delete and return a document asynchronous.
     *
     * @param filter
     *      filter to delete
     * @return
     *      the document that was removed.  If no documents matched the query filter, then null will be returned
     */
    public CompletableFuture<Optional<T>> findOneAndDeleteAsync(Filter filter) {
        return CompletableFuture.supplyAsync(() -> findOneAndDelete(filter));
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
    public Optional<T> findOneAndDelete(Filter filter, CollectionFindOneAndDeleteOptions options) {
        Command findOneAndReplace = Command
                .create("findOneAndDelete")
                .withFilter(filter)
                .withSort(options.getSortArray())
                .withProjection(options.getProjectionArray());

        DataAPIResponse res = runCommand(findOneAndReplace, options);
        if (res.getData()!= null && res.getData().getDocument() != null) {
            return Optional.ofNullable(res
                    .getData()
                    .getDocument()
                    .map(getDocumentClass()));
        }
        return Optional.empty();
    }


}
