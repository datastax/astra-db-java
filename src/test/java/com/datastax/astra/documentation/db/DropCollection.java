package com.datastax.astra.documentation.db;

import com.datastax.astra.AstraDatabase;

public class DropCollection {
  public static void main(String[] args) {
    AstraDatabase db = new AstraDatabase("API_ENDPOINT", "TOKEN");

    // Delete an existing collection
    db.dropCollection("collection_vector2");
  }
}
