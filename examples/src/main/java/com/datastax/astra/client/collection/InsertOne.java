package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertOneResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

public class InsertOne {

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
        InsertOneResult res1 = collectionDoc.insertOne(doc1);
        System.out.println(res1.getInsertedId()); // should be "1"

        // Insert a document with embeddings
        Document doc2 = new Document("2").append("name", "joe");
        collectionDoc.insertOne(doc2, new float[] {.1f, .2f});

        // Given an existing collection
        Collection<Product> collectionProduct = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION2_NAME", Product.class);

        // Insert a document with custom bean
        collectionProduct.insertOne(new Product("1", "joe"));
        collectionProduct.insertOne(new Product("2", "joe"), new float[] {.1f, .2f});

    }
}
