package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.databases.Database;

public class DropNamespace {

    public static void main(String[] args) {
        // Default initialization
        Database db = new Database("API_ENDPOINT", "TOKEN");

        // Drop a keyspace
        db.getDatabaseAdmin().dropNamespace("<namespace_name>");
    }
}
