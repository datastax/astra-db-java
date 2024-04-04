package com.datastax.astra.client;

import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.InsertManyResult;

import java.util.List;

import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.SimilarityMetric.COSINE;

public class QuickStart {
  public static void main(String[] args) {
    // Initializing the client with a token
    DataAPIClient client = new DataAPIClient("TOKEN");
    // Accessing the Database
    Database db = client.getDatabase("API_ENDPOINT");
    // Create collection with vector search
    Collection<Document> col = db.createCollection("demo", 5, COSINE);
    // Insert
    InsertManyResult res =col.insertMany(
     new Document().append("state", "CA").append("city", "San Francisco").append("zip", 94107),
     new Document().append("state", "NY").append("city", "New York").append("zip", 10001),
     new Document().append("state", "IL").append("city", "Chicago").append("zip", 60601),
     new Document().append("state", "TX").append("city", "Austin").append("zip", 78701),
     new Document().append("state", "FL").append("city", "Miami").append("zip", 33101),
     new Document().append("state", "FL").append("city", "Orlando").append("zip", 32801),
     new Document().append("state", "FL").append("city", "Tampa").append("zip", 33602)
    );

    col.insertMany(List.of(
       new Document("id1")
           .vector(new float[]{.1f, 0.2f, .2f, .4f, .1f})
           .append("country", "USA").append("state", "CA").append("city", "San Francisco").append("zip", 94107),
       new Document("id2")
           .vector(new float[]{.2f, .2f, .2f, .2f, .2f})
           .append("country", "USA").append("state", "CA").append("zip", 93190),
       new Document("id3")
           .vector(new float[]{.2f, .3f, .1f, .1f, .3f})
           .append("country", "France").append("state", "Mayenne").append("zip", 53100)));

    // Give me documents in the USA
    FindIterable<Document> us = col.find(eq("country", "USA"));

    //


    // Perform a similarity search
    float[] embeddings = new float[] {1f, 1f, 1f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    FindIterable<Document> docs = col.find(embeddings, 10);

    // Print result
    for (Document doc : docs) System.out.println(doc);
  }
}
