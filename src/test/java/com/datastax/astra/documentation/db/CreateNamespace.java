package com.datastax.astra.documentation.db;

import com.datastax.astra.AstraDatabase;

public class CreateNamespace {
  public static void main(String[] args) {
    // Default initialization
    AstraDatabase db = new AstraDatabase("API_ENDPOINT", "TOKEN");

    // Create a new namespace
    db.createNamespace("<namespace_name>");
  }
}
