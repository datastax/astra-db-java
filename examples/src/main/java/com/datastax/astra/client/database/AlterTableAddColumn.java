package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTableAddColumns;
import com.datastax.astra.client.tables.definition.rows.Row;

public class AlterTableAddColumn {

 public static void main(String[] args) {
  // Database db = new DataAPIClient(token).getDatabase(endpoint);
  Database db = DataAPIClients.localDbWithDefaultKeyspace();
  Table<Row> myTable1 = db.getTable("games");

  // Add A Columns
  AlterTableAddColumns add = new AlterTableAddColumns()
          .addColumnBoolean("tie_break")
          .addColumnText("venue");
  myTable1.alter(add);

 }

}
