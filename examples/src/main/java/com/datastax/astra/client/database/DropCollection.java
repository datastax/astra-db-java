package com.datastax.astra.client.database;

import com.datastax.astra.client.Database;

public class DropCollection {
  public static void main(String[] args) {
    Database db = new Database("API_ENDPOINT", "TOKEN");

    // Delete an existing collection
    db.dropCollection("collection_vector2");
  }
}
