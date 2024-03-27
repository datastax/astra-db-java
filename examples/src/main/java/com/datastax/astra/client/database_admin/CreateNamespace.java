package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.Database;

public class CreateNamespace {
  public static void main(String[] args) {
    // Default initialization
    Database db = new Database("API_ENDPOINT", "TOKEN");

    // Create a new namespace
    db.getDatabaseAdmin().createNamespace("<namespace_name>");
  }
}
