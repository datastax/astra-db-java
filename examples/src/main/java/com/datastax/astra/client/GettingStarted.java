package com.datastax.astra.client;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.commands.FindIterable;

import java.util.List;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.vector.SimilarityMetric.EUCLIDEAN;

public class GettingStarted {
 public static void main(String[] args) {
  // Initializing client with a token
  DataAPIClient client = new DataAPIClient("my_token");

  // Accessing the Database through the HTTP endpoint
  Database db = client.getDatabase("http://db-region.apps.astra.datastax.com");

  // Create collection with vector support
  Collection<Document> col = db.createCollection("demo", 2, EUCLIDEAN);

  // Insert records
  col.insertMany(List.of(
   new Document("doc1").vector(new float[]{.1f, 0.2f}).append("key", "value1"),
   new Document().id("doc2").vector(new float[]{.2f, 0.4f}).append("hello", "world"),
   new Document("doc3").vector(new float[]{.5f, 0.6f}).append("key", "value1"))
  );

  // Search
  FindIterable<Document> docs = col.find(
    eq("key", "value1"), // metadata filter
    new float[] {.5f, .5f},              //vector
    10);                                 // maxRecord

  // Iterate and print your results
  for (Document doc : docs) System.out.println(doc);
 }
}