package com.datastax.astra.documentation.client;

import com.datastax.astra.client.AstraDBAdmin;
import com.datastax.astra.devops.db.domain.Database;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class FindDatabase {
  public static void main(String[] args) {
    AstraDBAdmin client = new AstraDBAdmin("TOKEN");

    // Check if a database exists
    boolean exists = client.databaseExists("<database_name>");

    // Find a database by name (names may not be unique)
    Stream<Database> dbStream = client.getDatabaseInformations("<database_name>");
    Optional<Database> dbByName = dbStream.findFirst();

    // Find a database by ID
    Optional<Database> dbById = client
        .getDatabaseInformations(UUID.fromString("<database_id>"));
  }
}
