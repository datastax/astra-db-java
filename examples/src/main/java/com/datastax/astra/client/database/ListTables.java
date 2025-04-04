package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Game;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.commands.options.ListTablesOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ListTables {

 public static void main(String[] args) {
  // Database astraDb =  new DataAPIClient(token).getDatabase(endpoint);
  Database db =
    DataAPIClients.localDbWithDefaultKeyspace();

  // Default
  List<TableDescriptor> tableList = db.listTables();

  // Options
  db.getDatabaseAdmin().createKeyspace("ks2");
  ListTablesOptions options = new ListTablesOptions()
          .keyspace("ks2")
          .timeout(Duration.ofSeconds(5));
  List<TableDescriptor> tableList2 = db.listTables(options);
  Table<Row> ts = db.getTable("table_simple", new TableOptions().keyspace("ks2"));
  // Expecting an error as table does not exist in ks2
 }
}
