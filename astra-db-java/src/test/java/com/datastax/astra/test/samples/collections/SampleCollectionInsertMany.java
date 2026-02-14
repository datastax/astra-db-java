package com.datastax.astra.test.samples.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Demonstrates typed collection with POJO and {@link CollectionInsertManyOptions}
 * (chunkSize, concurrency, ordered, timeouts).
 *
 * @see Collection#insertMany(java.util.List, CollectionInsertManyOptions)
 */
@SuppressWarnings("unused")
public class SampleCollectionInsertMany {

    @Data @AllArgsConstructor
    public static class Product {
        @JsonProperty("_id")
        private String id;
        private String name;
    }

    /** Insert untyped documents. */
    static void insertManyDocuments() {
        Collection<Document> collectionDoc = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        Document doc1 = new Document("1").append("name", "joe");
        Document doc2 = new Document("2").append("name", "joe");
        CollectionInsertManyResult res = collectionDoc.insertMany(List.of(doc1, doc2));
        System.out.println("Identifiers inserted: " + res.getInsertedIds());
    }

    /** Insert typed POJOs with chunking and timeout options. */
    static void insertManyTypedWithOptions() {
        Collection<Product> collectionProduct = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION2_NAME", Product.class);

        CollectionInsertManyOptions options = new CollectionInsertManyOptions()
                .chunkSize(20)
                .concurrency(1)
                .ordered(false)
                .timeoutOptions(new TimeoutOptions()
                        .generalMethodTimeoutMillis(50000)
                        .requestTimeoutMillis(2000));

        CollectionInsertManyResult res = collectionProduct.insertMany(
                List.of(new Product("1", "joe"),
                        new Product("2", "joe")),
                options);
    }
}
