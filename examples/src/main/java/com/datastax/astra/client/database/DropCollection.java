package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.commands.options.DropCollectionOptions;

public class DropCollection {
  public static void main(String[] args) {
    Database db = new DataAPIClient("TOKEN")
            .getDatabase("API_ENDPOINT");

    // Delete an existing collection
    DropCollectionOptions options = new DropCollectionOptions();
    db.dropCollection("collection_vector2", options);
  }
}
