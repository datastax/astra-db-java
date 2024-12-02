package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Game;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.util.Map;

public class GetTable {

 public static void main(String[] args) {
  // Database astraDb =
  // new DataAPIClient(token).getDatabase(endpoint);
  Database db =
    DataAPIClients.localDbWithDefaultKeyspace();

  // Default
  Table<Row> myTable1 =
   db.getTable("games");

  // Options
  TableOptions options = new TableOptions()
    .keyspace("the_other_keyspace")
    .embeddingApiKey("secret-012abc...")
    .databaseAdditionalHeaders(Map.of("Feature-Flag-tables", "true"));
  Table<Row> myTable3 = db.getTable("games", options);

  // Typing
  Table<Game> myTable2 =
   db.getTable("games", Game.class);

  // Typing + Options
  Table<Game> myTable4 =
   db.getTable("games", Game.class, new TableOptions());



}

}
