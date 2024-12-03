package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Duration;

public class CreateIndex {
 public static void main(String[] args) {
   //Database db = new DataAPIClient("token").getDatabase("endpoint");
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   Table<Row> tableGames = db.getTable("games");

   tableGames.createIndex("score_index","score");

   TableIndexDefinition definition = new TableIndexDefinition()
     .column("winner")
     .ascii(true)  // only text or ascii
     .caseSensitive(true)
     .normalize(true);

   CreateIndexOptions options = new CreateIndexOptions()
     .ifNotExists(true)
     .timeout(Duration.ofSeconds(2));
   tableGames.createIndex("winner_index", definition, options);
 }
}
