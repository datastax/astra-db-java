package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.ListIndexesOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Duration;

public class ListIndexesNames {
 public static void main(String[] args) {
   //Database db = new DataAPIClient("token").getDatabase("endpoint");
   Database db = DataAPIClients.localDbWithDefaultKeyspace();
   Table<Row> tableGames = db.getTable("games");

   //List<String> indexesNames = tableGames.listIndexesNames();

   ListIndexesOptions options = new ListIndexesOptions()
     .timeout(Duration.ofSeconds(5));

   tableGames.listIndexesNames(options)
             .forEach(System.out::println);
 }
}
