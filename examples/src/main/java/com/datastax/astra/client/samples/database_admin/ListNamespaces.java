package com.datastax.astra.client.samples.database_admin;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;

import java.util.stream.Stream;

public class ListNamespaces {
    public static void main(String[] args) {

        // Default initialization
        Database db = new Database("API_ENDPOINT", "TOKEN");

        // List available namespaces
        Stream<String> names = db.getDatabaseAdmin().listNamespaceNames();

        // Only for Local deployments
        DataAPIDatabaseAdmin dbAdminLocal = (DataAPIDatabaseAdmin) db.getDatabaseAdmin();
        //dbAdminLocal.
    }
}
