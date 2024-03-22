package com.datastax.astra.documentation.db;

import com.datastax.astra.internal.astra.AstraDatabase;
import io.stargate.sdk.data.client.DataApiCollection;
import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.collections.CreateCollectionOptions;

public class FindCollection {
  public static void main(String[] args) {
    AstraDatabase db = new AstraDatabase("TOKEN", "API_ENDPOINT");

    // Find a collection
    DataApiCollection<Document> collection = db.getCollection("collection_vector1");

    // Gather collection information
    CreateCollectionOptions options = collection.getOptions();

    // Check if a collection exists
    boolean collectionExists = db.getCollection("collection_vector2").exists();
  }
}
