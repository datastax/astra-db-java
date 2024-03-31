package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.Sorts;

import java.util.Optional;

import static com.datastax.astra.client.model.Filters.and;
import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Filters.gt;
import static com.datastax.astra.client.model.FindOneOptions.Builder.vector;

public class FindOne {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("my_token")
                .getDatabase("http://db-region.apps.astra.datastax.com")
                .getCollection("my_collection");

        // Insert records
        collection.insertOne(new Document().id(1).append("name", "John").append("age", 30));

        // FindOne with a filter on id
        Optional<Document> res = collection.findOne(eq(1));

        // FindOne with filters on metadata
        Optional<Document> res2 = collection.findOne(eq("name", "John"));
        Optional<Document> res3 = collection.findOne(and(eq("name", "John"), gt("age", 30)));

        // FindOne with vector clause (no filter)
        Optional<Document> res4 = collection.findOne(null, vector(new float[] {.1f, .2f}).includeSimilarity());
    }
}
