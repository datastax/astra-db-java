package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionInsertManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class InsertMany {

    @Data @AllArgsConstructor
    public static class Product {
        @JsonProperty("_id")
        private String id;
        private String name;
    }

    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collectionDoc = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Insert a document
        Document doc1 = new Document("1").append("name", "joe");
        Document doc2 = new Document("2").append("name", "joe");
        CollectionInsertManyResult res1 = collectionDoc.insertMany(List.of(doc1, doc2));
        System.out.println("Identifiers inserted: " + res1.getInsertedIds());

        // Given an existing collection
        Collection<Product> collectionProduct = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION2_NAME", Product.class);

        // Insert a document with embeddings
        CollectionInsertManyOptions options = new CollectionInsertManyOptions()
                .chunkSize(20)  // how many process per request
                .concurrency(1) // parallel processing
                .ordered(false) // allows parallel processing
                .timeoutOptions(new TimeoutOptions()
                        .generalMethodTimeoutMillis(50000)
                        .requestTimeoutMillis(2000)); // timeout in millis

        CollectionInsertManyResult res2 = collectionProduct.insertMany(
                List.of(new Product("1", "joe"),
                        new Product("2", "joe")),
                options);
    }
}
