package com.datastax.astra.documentation.collection;

import com.datastax.astra.db.AstraCollection;
import com.datastax.astra.internal.astra.AstraDatabase;
import io.stargate.sdk.data.client.model.Document;

public class ClearCollection {
  public static void main(String[] args) {
    // Connect to running dn
    AstraDatabase db = new AstraDatabase("API_ENDPOINT", "TOKEN");

    // Accessing the collection
    AstraCollection<Document> collection = db.getCollection("collection_simple");

    // Delete all rows from an existing collection
    collection.deleteAll();
  }
}
