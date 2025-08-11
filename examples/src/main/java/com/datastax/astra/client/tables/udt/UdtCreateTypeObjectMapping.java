package com.datastax.astra.client.tables.udt;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedType;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeField;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UdtCreateTypeObjectMapping {

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

    @Data
    @NoArgsConstructor
    @TableUserDefinedType("member2")
    public static final class UdtMember {
        @TableUserDefinedTypeField(name ="name", type = TableUserDefinedTypeFieldTypes.TEXT)
        String name;

    }

    public static void main(String[] args) {

        Database database = getHCDDatabase(
                "http://localhost:8181",
                "cassandra",
                "cassandra",
                "quickstart_keyspace");

        // Create a UDT
        TableUserDefinedTypeDefinition typeDefinition = new TableUserDefinedTypeDefinition()
                .addFieldText("name")
                .addFieldBoolean("is_active")
                .addFieldDate("date_joined");

        database.createType("member", typeDefinition,  CreateTypeOptions.IF_NOT_EXISTS);
    }
}
