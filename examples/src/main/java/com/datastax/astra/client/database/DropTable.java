package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Game;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.DropTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static java.time.Duration.ofSeconds;

public class DropTable {

 public static void main(String[] args) {
  // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);
  Database db = DataAPIClients.localDbWithDefaultKeyspace();

  // Drop without options
  db.dropTable("games");

  // Adding a timestamp
  DropTableOptions options = new DropTableOptions()
   .ifExists(false)
   .timeout(ofSeconds(5));
  db.dropTable("games", options);
 }
}
