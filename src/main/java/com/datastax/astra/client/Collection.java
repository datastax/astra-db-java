package com.datastax.astra.client;

import com.datastax.astra.client.model.collections.CollectionDefinition;
import com.datastax.astra.client.model.collections.CollectionOptions;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


/**
 * A Data API collection, the main object to interact with the Data API, especially for DDL operations.
 * This class has a synchronous and asynchronous signature for all operations.
 * <p>
 * A Collection is spawned from a DataApiNameSpace object, from which it inherits the details on how to reach the API server
 * (endpoint, authentication). A Collection has a name, which is its unique identifier for a namespace and
 * options to specialize the usage as vector collections or advanced indexing parameters.
 * </p>
 *
 * <p>
 * A Collection is typed object designed to work both with default @{@link io.stargate.sdk.data.client.model.Document} (wrapper for a Map) and application
 * plain old java objects (pojo). The serialization is performed with Jackson and application beans can be annotated.
 * </p>
 *
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
public interface Collection<DOC> extends CommandRunner {

    // ----------------------------
    // --- Global Informations ----
    // ----------------------------

    /**
     * Retrieves the parent {@link Database} instance.
     * This parent namespace is used for performing CRUD (Create, Read, Update, Delete) operations on collections.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Given a collection
     * DataApiCollection<Document> collection;
     *   // TODO
     * }
     * </pre>
     *
     * @return parent namespace client.
     */
    Database getNamespace();

    /**
     * Retrieves the configuration options for the collection, including vector and indexing settings.
     * These options specify how the collection should be created and managed, potentially affecting
     * performance, search capabilities, and data organization.
     * <p></p>
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
    CollectionOptions getOptions();

    /**
     * Retrieves the full definition of the collection with its name and options.
     * <p></p>
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
     * @return the full collection definition.
     *
     * @see CollectionDefinition##getOptions()
     */
    CollectionDefinition getDefinition();

    /**
     * Retrieves the class type of the POJO (Plain Old Java Object) used for unmarshalling documents
     * within the collection. This class type is crucial for converting the raw data from the collection
     * into more manageable, object-oriented representations. By default, this method returns the
     * {@link io.stargate.sdk.data.client.model.Document} class, which serves as the standard container
     * for document data. Custom implementations can override this default to utilize a different POJO
     * that better suits their data structure and requirements.
     *
     * @return The {@code Class<DOC>} type representing the POJO class used for unmarshalling documents
     *         from the collection. This class facilitates the conversion from stored document formats
     *         to Java object instances, allowing for more intuitive data manipulation and access within
     *         the application.
     */
    Class<DOC> getDocumentClass();

    /**
     * Retrieves the name of the collection.
     *
     * @return The name of the collection
     */
    String getName();

    /**
     * Delete the collection from its namespace.
     */
    void drop();

    /**
     * Checks if the specified collection exists within the current namespace.
     *
     * <p>
     * This method delegates the existence check to the {@code existCollection} method of the associated
     * namespace, determined by {@link #getNamespace()}, and evaluates the existence based on the
     * collection's name, as retrieved by {@link #getName()}.
     * </p>
     *
     * @return {@code true} if the collection exists within the namespace, {@code false} otherwise.
     */
    default boolean exists() {
        return getNamespace().collectionExists(getName());
    }

    // --------------------------
    // ---      Find         ----
    // --------------------------

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
     * @return An {@link Optional<DOC>} that contains the found document if one exists that matches
     *         the filter criteria. Returns an empty {@link Optional} if no matching document is found,
     *         enabling safe retrieval operations without the risk of {@link java.util.NoSuchElementException}.
     */
    default Optional<DOC> findOne(Filter filter) {
        return findOne(filter, new FindOneOptions());
    }

    /**
     * Initiates an asynchronous search to find a single document that matches the given filter criteria.
     * This method leverages the functionality of {@link Collection#findOne(Filter)} to perform the
     * search, but it does so asynchronously, returning a {@link CompletableFuture}. This approach allows
     * the calling thread to remain responsive and perform other tasks while the search operation completes.
     * The result of the operation is wrapped in a {@link CompletableFuture} that, upon completion, will
     * contain an {@link Optional} instance. This instance either holds the document that matches the filter
     * criteria or is empty if no such document exists.
     *
     * @param filter The {@link Filter} specifying the conditions that the document must meet to be considered
     *               a match. This parameter determines how the search is conducted and what criteria the
     *               document must satisfy to be retrieved.
     * @return A {@link CompletableFuture<Optional<DOC>>} that, when completed, will contain the result of
     *         the search operation. If a matching document is found, the {@link Optional} is non-empty;
     *         otherwise, it is empty to indicate the absence of a matching document. This future allows for
     *         non-blocking operations and facilitates the integration of asynchronous programming patterns.
     */
    default CompletableFuture<Optional<DOC>> findOneASync(Filter filter) {
        return CompletableFuture.supplyAsync(() -> findOne(filter));
    }

    /**
     * Create a filter by id.
     *
     * @param id
     *      value for identifier
     * @return
     *      document if it exists
     */
    default Optional<DOC> findById(Object id) {
        return findOne(eq(id));
    }

    /**
     * Find one document from a filter.
     *
     * @param filter
     *      filter
     * @param options
     *      no options
     * @return
     *      document
     */
    Optional<DOC> findOne(Filter filter, FindOneOptions options);

    /**
     * Finds all documents in the collection.
     *
     * @return
     *      the find iterable interface
     */
    default FindIterable<DOC> find() {
        return find(null, new FindOptions());
    }

    /**
     * Finds all documents in the collection.
     *
     * @param filter
     *      the query filter
     * @return
     *      the find iterable interface
     */
    default FindIterable<DOC> find(Filter filter) {
        return find(filter, new FindOptions());
    }

    /**
     * Finds all documents in the collection.
     *
     * @param options
     *      options of find one
     * @return
     *      the find iterable interface
     */
    default FindIterable<DOC> find(FindOptions options) {
        return find(null, options);
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
    FindIterable<DOC> find(Filter filter, FindOptions options);

    /**
     * Mapping the 'find' operation of the Rest Endpoint this function returns up to MAX_PAGE_SIZE
     * document as a page with the eventual {@code pageState}.
     * <p>
     * This method utilizes the {@link io.stargate.sdk.data.client.model.Filters} class to build filter
     * predicates for querying the database. It is designed to filter results based on the provided
     * {@code filter}.
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * // Assuming a document structure where 'age' and 'country' are fields within the document.
     * Filters filters = Filters.and(
     *      Filters.eq("age", 25),
     *      Filters.eq("country", "US")
     * );
     *
     * // Now, use the filters with the 'find' operation.
     * List<Document> results = find(filters);
     * }
     * </pre>
     *
     * @param filter
     *      a predicate to filter the results, use class {@link io.stargate.sdk.data.client.model.Filters} to help build those.
     *
     * @param options
     *      options to retrieve page including a pagedState
     * @return
     *      a list of page
     */
    Page<DOC> findPage(Filter filter, FindOptions options);

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
    default <FIELD> DistinctIterable<DOC, FIELD> distinct(String fieldName, Class<FIELD> resultClass) {
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
    <FIELD> DistinctIterable<DOC, FIELD> distinct(String fieldName, Filter filter, Class<FIELD> resultClass);

    // --------------------------
    // ---   Count           ----
    // --------------------------

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
    int countDocuments(int upperBound) throws TooManyDocumentsToCountException;

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
    int countDocuments(Filter filter, int upperBound)  throws TooManyDocumentsToCountException;

    // --------------------------
    // ---   Insert          ----
    // --------------------------

    /**
     * Insert a single document in the collection in an atomic operation.
     *
     * <p>
     * <blockquote><b>Note:</b>If an `_id` is explicitly provided, which corresponds to a document
     * that exists already in the collection, an error is raised and the insertion fails.
     * Inserts the provided document. If the document is missing an identifier, the server will generate one.
     * </blockquote>
     * </p>
     *
     * @param document
     *     the document expressing the document to insert. The `_id` field of the document can be left out, in which case it will be created automatically.
     * @return
     *       an InsertOneResult object.
     */
    InsertOneResult insertOne(DOC document);

    InsertOneResult insertOne(DOC document, float[] embeddings);

    InsertOneResult insertOne(DOC document, String vectorize);

    /**
     * Inserts one or more documents.

     * @param documents
     *      the documents to insert
     * @return
     *      the insert many result
     * @throws IllegalArgumentException
     *      if the documents list is null or empty, or any of the documents in the list are null
     */
    InsertManyResult insertMany(List<? extends DOC> documents);

    /**
     * Inserts one or more documents.
     *
     * @param documents
     *      the documents to insert
     * @param options
     *      options to insert many documents
     * @return
     *      the insert many result
     * @throws IllegalArgumentException
     *      if the documents list is null or empty, or any of the documents in the list are null
     */
   InsertManyResult insertMany(List<? extends DOC> documents, InsertManyOptions options);

    /**
     * Executes a mix of inserts, updates, replaces, and deletes.
     *
     * @param commands
     *      list of commands to run
     * @return
     *      the result of the bulk write
     */
    default BulkWriteResult bulkWrite(List<Command> commands) {
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
    BulkWriteResult bulkWrite(List<Command> commands, BulkWriteOptions options);

    // --------------------------
    // ---   Delete          ----
    // --------------------------

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
    default DeleteResult deleteOne(Filter filter) {
        return deleteOne(filter, new DeleteOneOptions());
    }

    /**
     * Removes at most one document from the collection that matches the given filter.
     * If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @param options
     *      the option to driver the deletes (here sort)
     * @return
     *      the result of the remove one operation
     *
     */
    DeleteResult deleteOne(Filter filter, DeleteOneOptions options);

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @param filter
     *      the query filter to apply the delete operation
     * @return
     *      the result of the remove many operation
     */
    DeleteResult deleteMany(Filter filter);

    /**
     * Removes all documents from the collection that match the given query filter. If no documents match, the collection is not modified.
     *
     * @return
     *      the result of the remove many operation
     */
    DeleteResult deleteAll();

    /**
     * Atomically find a document and remove it.
     *
     * @param filter
     *      the query filter to find the document with
     * @return
     *      the document that was removed.  If no documents matched the query filter, then null will be returned
     */
    default Optional<DOC> findOneAndDelete(Filter filter) {
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
    Optional<DOC> findOneAndDelete(Filter filter, FindOneAndDeleteOptions options);

    // --------------------------
    // ---   Update/Replace  ----
    // --------------------------

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
    default UpdateResult replaceOne(Filter filter, DOC replacement) {
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
    UpdateResult replaceOne(Filter filter, DOC replacement, ReplaceOneOptions replaceOneOptions);

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
    default Optional<DOC> findOneAndReplace(Filter filter, DOC replacement) {
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
    Optional<DOC> findOneAndReplace(Filter filter, DOC replacement, FindOneAndReplaceOptions options);

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
    default UpdateResult updateOne(Filter filter, Update update) {
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
    UpdateResult updateOne(Filter filter, Update update, UpdateOneOptions updateOptions);

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
    default UpdateResult updateMany(Filter filter, Update update) {
        return updateMany(filter, update, new UpdateOneOptions());
    }

    /**
     * Update all documents in the collection according to the specified arguments.
     *
     * @param filter
     *      a document describing the query filter, which may not be null.
     * @param update
     *      a document describing the update, which may not be null. The update to apply must include only update operators.
     * @param updateOptions
     *      the options to apply to the update operation
     * @return
     *      the result of the update many operation
     */
    UpdateResult updateMany(Filter filter, Update update, UpdateOneOptions updateOptions);

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
    default Optional<DOC> findOneAndUpdate(Filter filter, Update update) {
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
    Optional<DOC> findOneAndUpdate(Filter filter, Update update, FindOneAndUpdateOptions options);

}
