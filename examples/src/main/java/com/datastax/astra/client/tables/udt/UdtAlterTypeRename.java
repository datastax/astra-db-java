package com.datastax.astra.client.tables.udt;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.AlterTypeRenameFields;

public class UdtAlterTypeRename {

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

        database.alterType("member", new AlterTypeRenameFields()
          .addField("name", "first_name")
          .addField("is_active", "is_member"));
    }
}
