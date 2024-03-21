package com.datastax.astra.client;

import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.SimilarityMetric;
import io.stargate.sdk.data.client.model.collections.CollectionDefinition;
import io.stargate.sdk.data.client.model.collections.CollectionOptions;

import java.util.stream.Stream;

/**
 * Class to interact with a Namespace.
 */
public interface Database extends CommandRunner {

    // ------------------------------------------
    // ----   General Informations           ----
    // ------------------------------------------

    /**
     * Gets the name of the database.
     *
     * @return the database name
     */
    String getNamespaceName();

    /**
     * Gets the name of the database.
     *
     * @return the database name
     */
    DatabaseAdmin getClient();

    // ------------------------------------------
    // ----     Collection CRUD              ----
    // ------------------------------------------

    /**
     * Gets the names of all the collections in this database.
     *
     * @return
     *      a stream containing all the names of all the collections in this database
     */
    Stream<String> listCollectionNames();

    /**
     * Finds all the collections in this database.
     *
     * @return
     *  list of collection definitions
     */
    Stream<CollectionDefinition> listCollections();

    /**
     * Evaluate if a collection exists.
     *
     * @param collection
     *      namespace name.
     * @return
     *      if namespace exists
     */
    default boolean collectionExists(String collection) {
        return listCollectionNames().anyMatch(collection::equals);
    }

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
    default Collection<Document> getCollection(String collectionName) {
        return getCollection(collectionName, Document.class);
    }

    /**
     * Gets a collection, with a specific default document class.
     *
     * @param collectionName
     *      the name of the collection to return
     * @param documentClass
     *      the default class to cast any documents returned from the database into.
     * @param <DOC>
     *      the type of the class to use instead of {@code Document}.
     * @return
     *      the collection
     */
    <DOC> Collection<DOC> getCollection(String collectionName, Class<DOC> documentClass);

    /**
     * Drops this namespace
     */
    void drop();

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     */
    default Collection<Document> createCollection(String collectionName) {
        return createCollection(collectionName, null, Document.class);
    }

    /**
     * Create a default new collection for vector.
     * @param collectionName
     *      collection name
     * @param dimension
     *      vector dimension
     * @param metric
     *      vector metric
     * @return
     *      the instance of collection
     */
    default Collection<Document> createCollection(String collectionName, int dimension, SimilarityMetric metric) {
        return createCollection(collectionName, dimension, metric, Document.class);
    }

    /**
     * Create a default new collection for vector.
     * @param collectionName
     *      collection name
     * @param dimension
     *      vector dimension
     * @param metric
     *      vector metric
     * @param documentClass
     *      class of document to return
     * @return
     *      the instance of collection
     */
    default <DOC> Collection<DOC> createCollection(String collectionName, int dimension, SimilarityMetric metric, Class<DOC> documentClass) {
            return createCollection(collectionName, CollectionOptions.builder()
                    .withVectorDimension(dimension)
                    .withVectorSimilarityMetric(metric)
                    .build(), documentClass);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     */
    default <DOC> Collection<DOC> createCollection(String collectionName, Class<DOC> documentClass) {
        return createCollection(collectionName, null, documentClass);
    }

    /**
     * Create a new collection with the given name.
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param collectionOptions
     *      various options for creating the collection
     */
    default Collection<Document> createCollection(String collectionName, CollectionOptions collectionOptions) {
        return createCollection(collectionName, collectionOptions, Document.class);
    }

    /**
     * Create a new collection with the selected options
     *
     * @param collectionName
     *      the name for the new collection to create
     * @param collectionOptions
     *      various options for creating the collection
     */
    <DOC> Collection<DOC> createCollection(String collectionName, CollectionOptions collectionOptions, Class<DOC> documentClass);

    /**
     * Delete a collection.
     *
     * @param collectionName
     *      collection name
     */
    void dropCollection(String collectionName);

}
