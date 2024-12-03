package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.commands.options.ListTablesOptions;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.datastax.astra.client.tables.definition.rows.Row;

import java.time.Duration;
import java.util.List;

public class ListTablesNames {

 public static void main(String[] args) {
  // Database astraDb =  new DataAPIClient(token).getDatabase(endpoint);
  Database db =
    DataAPIClients.localDbWithDefaultKeyspace();

  // Default
  List<String> tableNames = db.listTableNames();

  // Options
  db.getDatabaseAdmin().createKeyspace("ks2");
  ListTablesOptions options = new ListTablesOptions()
          .keyspace("ks2")
          .timeout(Duration.ofSeconds(5));
  List<String> tableList2 = db.listTableNames(options);

 }
}
