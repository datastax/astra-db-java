package com.datastax.astra.documentation.client;

import com.datastax.astra.client.AstraDBAdmin;

import java.util.UUID;

public class DropDatabase {
  public static void main(String[] args) {
    AstraDBAdmin client = new AstraDBAdmin("TOKEN");

    // Delete an existing database
    client.dropDatabase("<database_name>");

    // Delete an existing database by ID
    client.dropDatabase(UUID.fromString("<database_id>"));
  }
}
