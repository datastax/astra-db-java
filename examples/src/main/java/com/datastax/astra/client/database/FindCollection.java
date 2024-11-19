package com.datastax.astra.client.database;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionDefinitionOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.databases.Database;

public class FindCollection {
  public static void main(String[] args) {
    Database db = new Database("TOKEN", "API_ENDPOINT");

    // Find a collection
    Collection<Document> collection = db.getCollection("collection_vector1");

    // Gather collection information
    CollectionDefinitionOptions options = collection.getOptions();

    // Check if a collection exists
    boolean collectionExists = db.getCollection("collection_vector2").exists();
  }
}
