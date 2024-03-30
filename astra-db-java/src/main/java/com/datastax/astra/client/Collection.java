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
import com.datastax.astra.client.exception.DataAPIFaultyResponseException;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.BulkWriteOptions;
import com.datastax.astra.client.model.BulkWriteResult;
import com.datastax.astra.client.model.CollectionIdTypes;
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
import com.datastax.astra.client.model.ObjectId;
import com.datastax.astra.client.model.Page;
import com.datastax.astra.client.model.ReplaceOneOptions;
import com.datastax.astra.client.model.UUIDv6;
import com.datastax.astra.client.model.UUIDv7;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateOneOptions;
import com.datastax.astra.client.model.UpdateResult;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.internal.utils.Assert;
import com.datastax.astra.internal.utils.CustomEJsonInstantDeserializer;
import com.datastax.astra.internal.utils.CustomUuidv6Serializer;
import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
public class Collection<T> extends AbstractCommandRunner {

    /** Collection identifier. */
    @Getter
    private final String collectionName;

    /** Working class representing documents of the collection. The default value is {@link Document}. */
    @Getter
    protected final Class<T> documentClass;

    /** Parent Database reference.  */
    @Getter
    private final Database database;

    /** Api Endpoint for the Database, if using an astra environment it will contain the database id and the database region.  */
    private final String apiEndpoint;

    /**
     * Keep Collection options in -memory to avoid multiple calls to the API.
     */
    private CollectionOptions options;

    /**
     * Check if options has been fetched
     */
    private boolean optionChecked = false;

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
     * @param clazz The {@code Class<DOC>} object that represents the model for documents within
     *              this collection. This class is used for serialization and deserialization of
     *              documents to and from the database. It ensures type safety and facilitates
     *              the mapping of database documents to Java objects.
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
    protected Collection(Database db, String collectionName, Class<T> clazz) {
        notNull(db, "database");
        notNull(clazz, "working class");
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
     * Retrieves the name of the parent namespace associated with this collection. A namespace in
     * this context typically refers to a higher-level categorization or grouping mechanism within
     * the database that encompasses one or more collections. This method allows for identifying
     * the broader context in which this collection exists, which can be useful for operations
     * requiring knowledge of the database structure or for dynamic database interaction patterns.
     *
     * @return A {@code String} representing the name of the parent namespace of the current
     *         collection. This name serves as an identifier for the namespace and can be used
     *         to navigate or query the database structure.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Collection myCollection = ... // assume myCollection is already initialized
     * String namespaceName = myCollection.getNamespaceName();
     * System.out.println("The collection belongs to the namespace: " + namespaceName);
     * }
     * </pre>
     */
    public String getNamespaceName() {
        return getDatabase().getNamespaceName();
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
     * CollectionOptions options = collection.getOptions();
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
        if (!optionChecked) {
            options = Optional.ofNullable(getDefinition().getOptions()).orElse(new CollectionOptions());
            optionChecked = true;
        }
        return options;
    }

    /**
     * Retrieves the name of the collection. This name serves as a unique identifier within the database and is
     * used to reference the collection in database operations such as queries, updates, and deletions. The collection
     * name is defined at the time of collection creation and is immutable.
     *
     * @return A {@code String} representing the name of the collection. This is the same name that was specified
     *         when the collection was created or initialized.
     */
    public String getName() {
        return collectionName;
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
     * <li>If {@code defaultId} is set to  {@code objectId}, ids will be an {@link com.datastax.astra.client.model.ObjectId}</li>
     * <li>If {@code defaultId} is set to  {@code uuidv6}, ids will be an {@link com.datastax.astra.client.model.UUIDv6}</li>
     * <li>If {@code defaultId} is set to {@code uuidv7}, ids will be an {@link com.datastax.astra.client.model.UUIDv7}</li>
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
    public final InsertOneResult insertOne(T document) {
        Assert.notNull(document, "document");
        return _insertOne(JsonUtils.convertValueForDataApi(document, Document.class));
    }

    /**
     * Asynchronously inserts a single document into the collection. This method provides the same functionality as
     * {@link #insertOne(T document)}, but it operates asynchronously, returning a {@link CompletableFuture} that
     * will be completed with the insertion result. Utilizing this method is beneficial for non-blocking operations,
     * allowing other tasks to proceed while the document insertion is being processed.
     *
     * <p>The asynchronous operation ensures that your application can remain responsive, making this method ideal for
     * applications requiring high throughput or for operations that do not need immediate completion confirmation.</p>
     *
     * <p>For details on the behavior, parameters, and return type, refer to the documentation of the synchronous
     * {@link #insertOne(T document)} method. This method inherits all the properties and behaviors of its synchronous
     * counterpart, including error handling and the generation or requirement of the {@code _id} field.</p>
     *
     * @param document The document to be inserted into the collection. The specifications regarding the document
     *                 structure and the {@code _id} field are the same as described in {@link #insertOne(T document)}.
     * @return A {@link CompletableFuture} that, upon completion, contains the result of the insert operation as an
     *         {@link InsertOneResult}. The completion may occur with a result in case of success or with an exception
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
    public final CompletableFuture<InsertOneResult> insertOneAsync(T document) {
        return CompletableFuture.supplyAsync(() -> insertOne(document));
    }

    /**
     * Inserts a single document into the collection in an atomic operation, similar to the {@link #insertOne(T document)}
     * method, but with the additional capability to include vector embeddings. These embeddings are typically used for
     * advanced querying capabilities, such as similarity search or machine learning models. This method ensures atomicity
     * of the insertion, maintaining the integrity and consistency of the collection.
     *
     * <p><b>Note:</b> Like the base {@code insertOne} method, if the {@code _id} field is explicitly provided and matches
     * an existing document's {@code _id} in the collection, the insertion will fail with an error. If the {@code _id} field
     * is not provided, it will be automatically generated by the server, ensuring the document's uniqueness within the
     * collection. This variant of the method allows for the explicit addition of a "$vector" property to the document,
     * storing the provided embeddings.</p>
     *
     * <p>The embeddings should be a float array representing the vector to be associated with the document. This vector
     * can be utilized by the database for operations that require vector space computations. An array containing only
     * zero is not valid as it would lead to computation error with division by zero.</p>
     *
     * @param document   The document to be inserted. This can include or omit the {@code _id} field. If omitted,
     *                   an {@code _id} will be automatically generated.
     * @param embeddings The vector embeddings to be associated with the document, expressed as an array of floats.
     *                   This array populates the "$vector" property of the document, enabling vector-based operations.
     * @return An {@code InsertOneResult} object that contains information about the result of the insertion, including
     *         the {@code _id} of the newly inserted document, whether it was explicitly provided or generated.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Document without an explicit _id and embeddings for vector-based operations
     * Document newDocument = new Document().append("name", "Jane Doe").append("age", 25);
     * float[] embeddings = new float[]{0.12f, 0.34f, 0.56f, 0.78f};
     * InsertOneResult result = collection.insertOne(newDocument, embeddings);
     * System.out.println("Inserted document id: " + result.getInsertedId());
     * }
     * </pre>
     */
    public final InsertOneResult insertOne(T document, float[] embeddings) {
        Assert.notNull(document, "document");
        Assert.notNull(embeddings, "vectorize");
        return _insertOne(JsonUtils.convertValueForDataApi(document, Document.class).vector(embeddings));
    }

    /**
     * Asynchronously inserts a single document into the collection with vector embeddings. This method mirrors the
     * functionality of {@link #insertOne(T document, float[] embeddings)}, operating asynchronously to return a
     * {@link CompletableFuture} that completes with the insertion result. It is designed for use cases where
     * non-blocking operations are essential, enabling other processes to continue while the document insertion
     * is executed in the background.
     *
     * <p>This method provides a convenient way to insert documents along with their associated vector embeddings
     * without halting the execution of your application, making it particularly suitable for applications that
     * require high levels of responsiveness or for operations where immediate confirmation of completion is not
     * critical.</p>
     *
     * <p>For a comprehensive understanding of the behavior, parameters, including the purpose and use of vector
     * embeddings, refer to the synchronous {@link #insertOne(T document, float[] embeddings)} method. This
     * asynchronous variant adopts all the behaviors and properties of its synchronous counterpart.</p>
     *
     * @param document   The document to be inserted, potentially without an {@code _id} field which, if omitted,
     *                   will be automatically generated.
     * @param embeddings The vector embeddings associated with the document, intended to be used for advanced
     *                   database operations such as similarity search.
     * @return A {@link CompletableFuture} that, upon completion, contains the result of the insert operation as an
     *         {@link InsertOneResult}. This future may be completed with a successful result or an exception in
     *         case of insertion failure.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Asynchronously inserting a document with embeddings
     * Document newDocument = new Document().append("title", "Async Insert with Embeddings").append("content", "Content for embeddings");
     * float[] embeddings = {0.1f, 0.2f, 0.3f};
     * CompletableFuture<InsertOneResult> futureResult = collection.insertOneAsync(newDocument, embeddings);
     * futureResult.thenAccept(result -> System.out.println("Inserted document id: " + result.getInsertedId()))
     *              .exceptionally(error -> { System.err.println("Insertion failed: " + error.getMessage()); return null; });
     * }
     * </pre>
     */
    public final CompletableFuture<InsertOneResult> insertOneAsync(T document, float[] embeddings) {
        return CompletableFuture.supplyAsync(() -> insertOne(document, embeddings));
    }

    /**
     * Inserts a single document into the collection in an atomic operation, extending the base functionality of
     * the {@link #insertOne(T document)} method by adding the capability to compute and include a vector of embeddings
     * directly within the document. This is achieved through a specified expression, which the service translates
     * into vector embeddings. These embeddings can then be utilized for advanced database operations that leverage
     * vector similarity.
     *
     * <p><b>Note:</b> As with the base {@code insertOne} method, providing an {@code _id} field that matches an existing
     * document's {@code _id} in the collection will cause the insertion to fail with an error. If the {@code _id} field
     * is not present, it will be automatically generated, ensuring the document's uniqueness. This method variant
     * introduces the ability to automatically compute embeddings based on the provided {@code vectorize} string,
     * populating the "$vectorize" property of the document for later use in vector-based operations.</p>
     *
     * <p>The {@code vectorize} parameter should be a string that conveys meaningful information about the document,
     * which will be converted into a vector representation by the database's embedding service. This functionality
     * is especially useful for enabling semantic searches or clustering documents based on their content similarity.</p>
     *
     * @param document  The document to be inserted. It can optionally include the {@code _id} field. If omitted,
     *                  an {@code _id} will be automatically generated.
     * @param vectorize The expression to be translated into a vector of embeddings. This string is processed by
     *                  the service to generate vector embeddings that are stored in the document under the "$vectorize"
     *                  property.
     * @return An {@code InsertOneResult} object that contains information about the result of the insertion, including
     *         the {@code _id} of the newly inserted document, whether it was explicitly provided or generated.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Document without an explicit _id and a string to be vectorized
     * Document newDocument = new Document().append("title", "How to Use Vectorization");
     * String vectorizeExpression = "This is a guide on vectorization.";
     * InsertOneResult result = collection.insertOne(newDocument, vectorizeExpression);
     * System.out.println("Inserted document id: " + result.getInsertedId());
     * }
     * </pre>
     */
    public final InsertOneResult insertOne(T document, String vectorize) {
        Assert.notNull(document, "document");
        Assert.hasLength(vectorize, "vectorize");
        return _insertOne(JsonUtils.convertValueForDataApi(document, Document.class).vectorize(vectorize));
    }

    /**
     * Asynchronously inserts a single document into the collection with a vectorization expression. This method
     * provides an asynchronous counterpart to {@link #insertOne(T document, String vectorize)}, allowing for
     * non-blocking operations while a document, along with its vectorization based on the provided string, is
     * inserted into the collection.
     *
     * <p>Utilizing this method facilitates the insertion of documents in scenarios where application responsiveness
     * is crucial. It allows the application to continue with other tasks while the document insertion, including
     * its vectorization, is processed in the background. This is particularly useful for operations that can
     * benefit from parallel execution or when the insertion time is not critical to the application's flow.</p>
     *
     * <p>For detailed information on the behavior and parameters, especially the purpose and processing of the
     * {@code vectorize} string, refer to the documentation of the synchronous
     * {@link #insertOne(T document, String vectorize)} method. This asynchronous method inherits all functionalities
     * and behaviors from its synchronous counterpart, ensuring consistency across the API.</p>
     *
     * @param document  The document to be inserted into the collection. The requirements and options regarding the
     *                  {@code _id} field and the document structure are identical to those described in the synchronous
     *                  version.
     * @param vectorize The string expression that will be used to compute the vector embeddings. This parameter enables
     *                  the automatic generation of embeddings to be associated with the document, enhancing its
     *                  usefulness for vector-based operations within the database.
     * @return A {@link CompletableFuture} that, when completed, provides the {@link InsertOneResult} indicating the
     *         outcome of the insert operation. The future may complete normally with the insertion result or exceptionally
     *         in case of an error.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Asynchronously inserting a document with a vectorization expression
     * Document newDocument = new Document().append("title", "Async Insert with Vectorization").append("description", "Description for vectorization");
     * String vectorizationExpression = "Description for vectorization";
     * CompletableFuture<InsertOneResult> futureResult = collection.insertOneAsync(newDocument, vectorizationExpression);
     * futureResult.thenAccept(result -> System.out.println("Inserted document id: " + result.getInsertedId()))
     *              .exceptionally(error -> { System.err.println("Insertion failed: " + error.getMessage()); return null; });
     * }
     * </pre>
     */
    public final CompletableFuture<InsertOneResult> insertOneAsync(T document, String vectorize) {
        return CompletableFuture.supplyAsync(() -> insertOne(document, vectorize));
    }

    /**
     * Insert a single document in the collection in an atomic internal operation.
     *
     * @param document
     *      the document to be inserted.
     * @return
     *      object wrapping the returned identifier
     */
    private InsertOneResult _insertOne(Document document) {
        Assert.notNull(document, "document");
        Command insertOne = Command.create("insertOne").withDocument(document);
        Object documentId = runCommand(insertOne)
                .getStatusKeyAsList("insertedIds", Object.class)
                .get(0);
        return new InsertOneResult(unmarshallDocumentId(documentId));
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
    protected Object unmarshallDocumentId(Object id) {
        if (id instanceof Map) {
            // only maps will required to be unmarshalled
            Map<String, Object> mapId = (Map<String, Object>) id;
            if (mapId.containsKey("$date")) {
                // eJson date
                return  Instant.ofEpochMilli((Long) mapId.get("$date"));
            }
            if (mapId.containsKey("$uuid")) {
                // defaultId with UUID
                UUID uid = UUID.fromString((String) mapId.get("$uuid"));
                if (getOptions() != null && getOptions().getDefaultId() != null) {
                    switch(getOptions().getDefaultId().get("type")) {
                        case uuidv6:
                            return new UUIDv6(uid);
                        case uuidv7:
                            return new UUIDv7(uid);
                        default:
                            return uid;
                    }
                }
                throw new IllegalStateException("Returned is is a UUID, but no defaultId is set in the collection definition.");
            }
            if (mapId.containsKey("$objectId")) {
                // defaultId with ObjectId
                return new ObjectId((String) mapId.get("$objectId"));
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
     * {@link InsertManyResult}.</p>
     *
     * <p><b>Timeout Handling:</b> The method attempts to complete all insertion tasks within the specified
     * timeout. If tasks do not complete in time, a {@link TimeoutException} is thrown.</p>
     *
     * <p><b>Error Handling:</b> Exceptions encountered during insertion or result aggregation are captured,
     * and a {@link RuntimeException} is thrown, indicating an issue with merging results into a single
     * {@link InsertManyResult}.</p>
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
     * @return An {@link InsertManyResult} object containing the IDs of all successfully inserted documents.
     * @throws IllegalArgumentException if the documents list is null or empty, any document is null, or if
     *                                  the options specified are invalid.
     * @throws RuntimeException if there is an error in merging the results of concurrent insertions.
     */
    public InsertManyResult insertMany(List<? extends T> documents, InsertManyOptions options) {
        Assert.isTrue(documents != null && !documents.isEmpty(), "documents list cannot be null or empty");
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted while waiting", e);
        }  catch (TimeoutException e) {
            throw new RuntimeException("Operation timed out", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during async operation", e.getCause());
        }
        return finalResult;
    }

    /**
     * Asynchronously inserts a batch of documents into the collection with customizable insertion options.
     * This method is the asynchronous counterpart to {@link #insertMany(List, InsertManyOptions)}, allowing
     * for non-blocking operations. It employs default or specified {@link InsertManyOptions} to optimize the
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
     * @return A {@link CompletableFuture} that, upon completion, contains the {@link InsertManyResult} indicating
     *         the outcome of the insert operation. The future may complete normally with the insertion result
     *         or exceptionally in case of an error.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     */
    public CompletableFuture<InsertManyResult > insertManyAsync(List<? extends T> documents, InsertManyOptions options) {
        return CompletableFuture.supplyAsync(() -> insertMany(documents, options));
    }

    /**
     * Inserts a batch of documents into the collection using default insertion options. This method is a
     * simplified version of {@link #insertMany(List, InsertManyOptions)}, intended for use cases where
     * default settings for concurrency, chunk size, and insertion order are sufficient. It provides an
     * efficient way to insert multiple documents concurrently, optimizing the insertion process with
     * predefined settings.
     *
     * <p>The default {@link InsertManyOptions} used by this method assumes non-concurrent (sequential)
     * insertion, with no specific chunk size or timeout constraints. This is suitable for general use
     * cases where the simplicity of invocation is prioritized over the customization of insertion
     * parameters. For more advanced control over the insertion process, including the ability to specify
     * concurrency levels, chunk sizes, and operation timeouts, use the overloaded
     * {@link #insertMany(List, InsertManyOptions)} method.</p>
     *
     * <p>This method leverages the same underlying insertion logic as its overloaded counterpart,
     * ensuring consistent behavior and error handling. It automatically handles validation of the
     * input documents list, chunking of documents based on default settings, and aggregation of
     * insertion results into a single {@link InsertManyResult}.</p>
     *
     * <p><b>Usage:</b> Ideal for inserting a collection of documents without the need for custom
     * insertion options. Simplifies the insertion process for basic use cases.</p>
     *
     * @param documents A list of documents to be inserted. Must not be null or empty, and no document should
     *                  be null.
     * @return An {@link InsertManyResult} object containing the IDs of all successfully inserted documents.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     * @throws RuntimeException if there is an error in merging the results of concurrent insertions.
     */
    public InsertManyResult insertMany(List<? extends T> documents) {
        return insertMany(documents, InsertManyOptions.builder().build());
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
     * concurrency, and aggregates the results into a single {@link InsertManyResult} asynchronously.</p>
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
     * @return A {@link CompletableFuture} that, upon completion, contains the {@link InsertManyResult} indicating the
     *         outcome of the insert operation. The future may complete with the insertion results or exceptionally in
     *         case of an error.
     * @throws IllegalArgumentException if the documents list is null or empty, or if any document is null.
     */
    public CompletableFuture<InsertManyResult > insertManyAsync(List<? extends T> documents) {
        return CompletableFuture.supplyAsync(() -> insertMany(documents));
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
    private Callable<InsertManyResult> getInsertManyResultCallable(List<? extends T> documents, InsertManyOptions options, int start) {
        int end = Math.min(start + options.getChunkSize(), documents.size());
        return () -> {
            log.debug("Insert block (" + cyan("size={}") + ") in collection {}", end - start, green(getCollectionName()));
            Command insertMany = new Command("insertMany")
                    .withDocuments(documents.subList(start, end))
                    .withOptions(new Document().append("ordered", options.isOrdered()));
            return new InsertManyResult(runCommand(insertMany)
                    .getStatusKeyAsList("insertedIds", Object.class)
                    .stream()
                    .map(this::unmarshallDocumentId)
                    .collect(Collectors.toList()));
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
     * @return An {@link java.util.Optional< T >} encapsulating the found document, if any, that meets the filter criteria.
     *         If no document matches the specified conditions, an empty {@link java.util.Optional} is returned, ensuring
     *         that retrieval operations can be performed safely without the concern of {@link java.util.NoSuchElementException}.
     */
    public Optional<T> findOne(Filter filter) {
        return findOne(filter, FindOneOptions.builder().build());
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
     * @param options The {@link FindOneOptions} instance containing additional options for the find operation,
     * @return An {@link Optional< T >} that contains the found document if one exists that matches
     *         the filter criteria. Returns an empty {@link Optional} if no matching document is found,
     *         enabling safe retrieval operations without the risk of {@link java.util.NoSuchElementException}.
     */
    public Optional<T> findOne(Filter filter, FindOneOptions options) {
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
    public CompletableFuture<Optional<T>> findOneASync(Filter filter) {
        return CompletableFuture.supplyAsync(() -> findOne(filter));
    }


    /**
     * Asynchronously attempts to find a single document within the collection that matches the given filter criteria,
     * utilizing the specified {@link FindOneOptions} for the query. This method offers a non-blocking approach to
     * querying the database, making it well-suited for applications requiring efficient I/O operations without
     * compromising the responsiveness of the application.
     *
     * <p>By executing the search operation in an asynchronous manner, this method allows other tasks to proceed
     * concurrently, effectively utilizing system resources and improving application throughput. The query leverages
     * a {@link Filter} instance to define the search criteria, and {@link FindOneOptions} to specify query
     * customizations, such as projection or sort parameters.</p>
     *
     * <p>In cases where no document matches the filter, the method returns a {@link CompletableFuture} completed with
     * an empty {@link java.util.Optional}, thus avoiding exceptions for non-existent documents. This behavior ensures
     * a more graceful handling of such scenarios, allowing for cleaner and more robust client code by leveraging
     * the {@link java.util.Optional} pattern within asynchronous workflows.</p>
     *
     * @param filter  The {@link Filter} instance encapsulating the criteria used to identify the desired document.
     *                It defines the conditions that a document must meet to be considered a match.
     * @param options The {@link FindOneOptions} providing additional query configurations such as projection
     *                and sort criteria to tailor the search operation.
     * @return A {@link CompletableFuture<Optional< T >>} that, upon completion, contains an {@link Optional< T >}
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
    public CompletableFuture<Optional<T>> findOneASync(Filter filter, FindOneOptions options) {
        return CompletableFuture.supplyAsync(() -> findOne(filter, options));
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
     * Asynchronously retrieves a document from the collection by its unique identifier. This method wraps
     * the synchronous {@code findById} operation in a {@link CompletableFuture}, enabling non-blocking
     * database queries that improve application responsiveness and efficiency. The method is ideal for
     * applications that require the retrieval of specific documents without impacting the performance
     * of the user interface or other concurrent operations.
     *
     * <p>The unique identifier used to locate the document is typically the primary key or a unique field
     * within the collection. This method abstracts the complexity of asynchronous programming, providing
     * a straightforward way to execute and handle database queries within a non-blocking model.</p>
     *
     * <p>If the document with the specified identifier exists, it is wrapped in an {@link Optional} and
     * returned within the completed future. Otherwise, the future is completed with an empty {@link Optional},
     * neatly handling cases where no matching document is found without throwing exceptions.</p>
     *
     * @param id The unique identifier of the document to retrieve. This can be of any type that the database
     *           recognizes as a valid identifier format (e.g., String, Integer).
     * @return A {@link CompletableFuture<Optional< T >>} that, upon completion, contains an {@link Optional< T >}
     *         with the document if found. If no document matches the specified identifier, a completed future
     *         with an empty {@link Optional} is returned.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * Object documentId = "uniqueDocumentId123";
     * CompletableFuture<Optional<Document>> futureDocument = collection.findByIdASync(documentId);
     * futureDocument.thenAccept(document -> document.ifPresent(System.out::println));
     * }
     * </pre>
     */
    public CompletableFuture<Optional<T>> findByIdASync(Object id) {
        return CompletableFuture.supplyAsync(() -> findById(id));
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
    public FindIterable<T> find(Filter filter, FindOptions options) {
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
    public FindIterable<T> find() {
        return find(null, FindOptions.builder().build());
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
    public FindIterable<T> find(Filter filter) {
        return find(filter, FindOptions.builder().build());
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
    public FindIterable<T> find(Filter filter, float[] vector, int limit) {
        return find(filter, FindOptions.builder()
                .vector(vector)
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
    public FindIterable<T> find(Filter filter, String vectorize, int limit) {
        return find(filter, FindOptions.builder()
                .vectorize(vectorize)
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
    public FindIterable<T> find(FindOptions options) {
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
    public Page<T> findPage(Filter filter, FindOptions options) {
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
     * @param <F>
     *      the target type of the iterable.
     * @return
     *      an iterable of distinct values
     */
    public <F> DistinctIterable<T, F> distinct(String fieldName, Class<F> resultClass) {
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
    public <F> DistinctIterable<T, F> distinct(String fieldName, Filter filter, Class<F> resultClass) {
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
    public Optional<T> findOneAndReplace(Filter filter, T replacement) {
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
    public Optional<T> findOneAndReplace(Filter filter, T replacement, FindOneAndReplaceOptions options) {

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
    public UpdateResult replaceOne(Filter filter, T replacement) {
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
    public UpdateResult replaceOne(Filter filter, T replacement, ReplaceOneOptions replaceOneOptions) {

        Command findOneAndReplace = Command
                .create("findOneAndReplace")
                .withFilter(filter)
                .withReplacement(replacement)
                .withOptions(new Document()
                        .appendIfNotNull("upsert", replaceOneOptions.getUpsert())
                        .append("returnDocument", FindOneAndReplaceOptions.ReturnDocument.before.name())
                );

        // Execute the `findOneAndReplace`
        FindOneAndReplaceResult<T> res = executeFindOneAndReplace(findOneAndReplace);

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
    private FindOneAndReplaceResult<T> executeFindOneAndReplace(Command cmd) {
        // Run Command
        ApiResponse apiResponse = runCommand(cmd);
        // Parse Command Result
        FindOneAndReplaceResult<T> result = new FindOneAndReplaceResult<>();
        if (apiResponse.getData() == null) {
            throw new DataAPIFaultyResponseException(cmd, apiResponse,"Faulty response from find_one_and_replace API command.");
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
    public Optional<T> findOneAndUpdate(Filter filter, Update update) {
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
    public Optional<T> findOneAndUpdate(Filter filter, Update update, FindOneAndUpdateOptions options) {
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
    public Optional<T> findOneAndDelete(Filter filter) {
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
    public Optional<T> findOneAndDelete(Filter filter, FindOneAndDeleteOptions options) {
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

            try {
                ExecutorService executor = Executors.newFixedThreadPool(options.getConcurrency());
                List<Future<ApiResponse>> futures = new ArrayList<>();
                commands.forEach(req -> futures.add(executor.submit(() -> runCommand(req))));
                executor.shutdown();
                for (Future<ApiResponse> future : futures) {
                    result.getResponses().add(future.get());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread was interrupted while waiting for command results", e);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Cannot access command results", e);
            } catch (Exception e) {
                throw new IllegalStateException("Error occurred during command execution", e.getCause());
            }
        }
        return result;
    }

    // --- Required for the Command Runner ---

    /**
     * Register the logging listener to the collection.
     *
     * @return
     *      self reference
     */
    public Collection<T> enableLogging() {
        registerListener("logger", new LoggingCommandObserver(Collection.class));
        return this;
    }


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
