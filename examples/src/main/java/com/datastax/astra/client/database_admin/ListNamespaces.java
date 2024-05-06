package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;

import java.util.Set;
import java.util.UUID;

public class ListNamespaces {
    public static void main(String[] args) {

        DataAPIClient client = new DataAPIClient("TOKEN");

        // Accessing admin providing a new token possibly with stronger permissions
        AstraDBAdmin admin = client.getAdmin("SUPER_USER_TOKEN");

        DatabaseAdmin dbAdmin = admin.getDatabaseAdmin(UUID.fromString("DATABASE_ID"));

        // List available namespaces
        Set<String> names = dbAdmin.listNamespaceNames();
    }
}
