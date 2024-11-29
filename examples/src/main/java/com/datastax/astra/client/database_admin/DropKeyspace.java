package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;

public class DropKeyspace {

    public static void main(String[] args) {
        // Default initialization
        Database db = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT");
        // Drop a Namespace
        db.getDatabaseAdmin().dropKeyspace("<keyspace_name>");
    }
}
