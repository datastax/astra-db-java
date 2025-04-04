package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.ListIndexesOptions;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Duration;

public class ListIndexes {
 public static void main(String[] args) {
   Database db = new DataAPIClient("token").getDatabase("endpoint");
   Table<Row> tableGames = db.getTable("games");

   //List<TableIndexDescriptor> indexes = tableGames.listIndexes();

   ListIndexesOptions options = new ListIndexesOptions()
     .timeout(Duration.ofSeconds(5));

   tableGames.listIndexes(options).forEach(idx -> {
        System.out.println("Index: " + idx.getName() + " on column: " + idx.getDefinition().getColumn());
   });
 }
}
