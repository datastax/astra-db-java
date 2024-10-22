package com.datastax.astra.client.collection;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.Document;

public class ClearCollection {
  public static void main(String[] args) {
    // Connect to running dn
    Database db = new Database("API_ENDPOINT", "TOKEN");

    // Accessing the collection
    Collection<Document> collection = db.getCollection("collection_simple");

    // Delete all rows from an existing collection
    collection.deleteAll();
  }
}
