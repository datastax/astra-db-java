package com.datastax.astra.client.database;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.CollectionOptions;

public class FindCollection {
  public static void main(String[] args) {
    Database db = new Database("TOKEN", "API_ENDPOINT");

    // Find a collection
    Collection<Document> collection = db.getCollection("collection_vector1");

    // Gather collection information
    CollectionOptions options = collection.getOptions();

    // Check if a collection exists
    boolean collectionExists = db.getCollection("collection_vector2").exists();
  }
}
