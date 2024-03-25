package com.datastax.astra.client.samples.admin;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.DataAPIClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseInfo;

public class ListDatabases {
    public static void main(String[] args) {
        
        // Initialization of admin (astra only)
        AstraDBAdmin astraDBAdmin = new DataAPIClient("TOKEN").getAdmin();

        // Display all database names
        astraDBAdmin.listDatabases()
                .map(Database::getInfo)
                .map(DatabaseInfo::getName)
                .forEach(System.out::println);
    }
}
