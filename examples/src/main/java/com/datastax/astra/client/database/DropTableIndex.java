package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableOptions;

import static java.time.Duration.ofSeconds;

public class DropTableIndex {

 public static void main(String[] args) {
  // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);
  Database db = DataAPIClients.localDbWithDefaultKeyspace();

  // Drop without options
  db.dropTableIndex("games");

  // Adding a timestamp
  DropTableIndexOptions options = new DropTableIndexOptions()
   .ifExists(false)
   .keyspace("KEYSPACE_NAME")
   .timeout(ofSeconds(5));
  db.dropTableIndex("games", options);
 }
}
