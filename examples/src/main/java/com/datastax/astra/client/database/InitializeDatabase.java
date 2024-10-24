package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.databases.Database;

public class InitializeDatabase {
  public static void main(String[] args) {
    // Default initialization
    Database db = new Database("API_ENDPOINT", "TOKEN");

    // Initialize with a non-default namespace.
    Database db2 = new Database("API_ENDPOINT", "TOKEN", "KEYSPACE");

    // non-default namespace + options
    // 'Options' allows fined-grained configuration.
    DataAPIOptions options = DataAPIOptions.builder()
            .withMaxTimeMS(10)
            .withHttpConnectTimeout(10)
            .build();
    Database db3 = new Database("API_ENDPOINT", "TOKEN", "KEYSPACE", options);
  }
}
