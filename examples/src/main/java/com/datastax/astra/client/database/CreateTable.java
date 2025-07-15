package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Game;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static java.time.Duration.ofSeconds;

public class CreateTable {

 public static void main(String[] args) {
  // Database astraDb = new DataAPIClient(token).getDatabase(endpoint);
  Database db = DataAPIClients.localDbWithDefaultKeyspace();

  // Definition of the table in fluent style
  TableDefinition tableDefinition = new TableDefinition()
   .addColumnText("match_id")
   .addColumnInt("round")
   .addColumnVector("m_vector", 
     new TableColumnDefinitionVector().dimension(3).metric(COSINE))
   .addColumn("score", TableColumnTypes.INT)
   .addColumn("when",  TableColumnTypes.TIMESTAMP)
   .addColumn("winner",  TableColumnTypes.TEXT)
   .addColumnSet("fighters", TableColumnTypes.UUID)
   .addPartitionBy("match_id")
   .addPartitionSort(ascending("round"));


  // Minimal creation
  Table<Row> table1 =
    db.createTable("games", tableDefinition);

  // Minimal Creation with a Bean
  Table<Game> table2 =
    db.createTable("game2", tableDefinition, Game.class);

  // One can add options to setup the creation with finer grained:
  CreateTableOptions createTableOptions = new CreateTableOptions()
   .keyspace("ks2")
   .ifNotExists(true)
   .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("api-key"))
   .timeout(ofSeconds(5));
  Table<Row> table3 =
    db.createTable("game3", tableDefinition, createTableOptions);
 }
}
