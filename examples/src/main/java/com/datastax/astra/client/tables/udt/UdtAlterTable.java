package com.datastax.astra.client.tables.udt;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;

import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;

public class UdtAlterTable {

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

        TableDefinition tableDefinition = new TableDefinition()
          .addColumnText("email")
          .addColumnText("name")
          // Given an Existing type 'member'
          .addColumnUserDefinedType("my_member", "member")
          .addColumnListUserDefinedType("my_member_list", "member")
          .addColumnSetUserDefinedType("my_member_set", "member")
          .addColumnMapUserDefinedType("my_member_map", "member", TableColumnTypes.TEXT)
          .partitionKey("email");

        database.createTable("members_table",tableDefinition, IF_NOT_EXISTS);
    }
}
