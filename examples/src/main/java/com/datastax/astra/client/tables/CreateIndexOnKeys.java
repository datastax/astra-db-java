package com.datastax.astra.client.tables;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.client.tables.definition.rows.Row;

public class CreateIndexOnKeys {

    public static Database getHCDDatabase(String url, String username, String password, String keyspace) {
        String authToken = new UsernamePasswordTokenProvider(username, password)
                .getToken();
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.HCD)
                .enableFeatureFlagTables()
                .logRequests();
        DataAPIClient client = new DataAPIClient(authToken,options);
        return client.getDatabase(url, keyspace);
    }

    public static void main(String[] args) {

        Database database = getHCDDatabase(
                "http://localhost:8181",
                "cassandra",
                "cassandra",
                "quickstart_keyspace");

        // Create a Table with a Map
        TableDefinition tableDefinition = new TableDefinition()
          .addColumnText("email")
          .addColumnMap("example_map_column", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
          .partitionKey("email");

        Table<Row> table = database
          .createTable("example_index_table2",
                  tableDefinition,
                  CreateTableOptions.IF_NOT_EXISTS);

        // Index a column
        table.createIndex(
                "example_index_name2",
                "example_map_column",
                TableIndexMapTypes.KEYS,
                CreateIndexOptions.IF_NOT_EXISTS);
    }
}
