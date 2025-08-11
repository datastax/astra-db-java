package com.datastax.astra.client.tables.udt;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.AlterTypeAddFields;
import com.datastax.astra.client.tables.commands.AlterTypeRenameFields;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeField;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;

import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.TEXT;

public class UdtAlterTypeAddFields {

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

        database.alterType("member", new AlterTypeAddFields()
          .addFieldText("email")
          .addFieldInt("credits"));
    }

}
