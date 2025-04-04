package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;

import java.time.Duration;

public class DropIndex {
 public static void main(String[] args) {
   Database db = new DataAPIClient("token").getDatabase("endpoint");

   db.dropTableIndex("score_index");

   DropTableIndexOptions options = new DropTableIndexOptions()
       .ifExists(true)
       .timeout(Duration.ofSeconds(5));
   db.dropTableIndex("winner_index", options);
 }
}
