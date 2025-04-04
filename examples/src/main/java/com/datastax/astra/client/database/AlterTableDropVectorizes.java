package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTableDropColumns;
import com.datastax.astra.client.tables.commands.AlterTableDropVectorize;
import com.datastax.astra.client.tables.definition.rows.Row;

public class AlterTableDropVectorizes {

 public static void main(String[] args) {
  // Database db = new DataAPIClient(token).getDatabase(endpoint);
  Database db = DataAPIClients.localDbWithDefaultKeyspace();
  Table<Row> myTable1 = db.getTable("games");

  // Add A Columns
  AlterTableDropVectorize dropVectorize = new AlterTableDropVectorize("m_vector");
  myTable1.alter(dropVectorize);

 }

}
