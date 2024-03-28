package com.datastax.astra.client.admin;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.DatabaseInfo;

public class ListDatabases {
    public static void main(String[] args) {
        
        // Initialization of admin (astra only)
        AstraDBAdmin astraDBAdmin = new DataAPIClient("TOKEN").getAdmin();

        // Display all database information
        astraDBAdmin.listDatabases().stream()
                .map(DatabaseInfo::getId)
                .forEach(System.out::println);

        // Display all database names
        astraDBAdmin.listDatabaseNames();
    }
}
