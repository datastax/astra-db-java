package com.datastax.astra.client.admin;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.DatabaseInfo;

import java.util.UUID;

public class GetDatabaseInformation {
  public static void main(String[] args) {

    AstraDBAdmin astraDBAdmin = new DataAPIClient("TOKEN").getAdmin();

    // Check if a database exists
    boolean exists1 = astraDBAdmin.databaseExists("database_name");
    boolean exists2 = astraDBAdmin.databaseExists(UUID.fromString("<database_id>"));

    // Find a database by name (names may not be unique)
    DatabaseInfo databaseInformation = astraDBAdmin
            .getDatabaseInfo(UUID.fromString("<database_id>"));
    System.out.println("Name=" + databaseInformation.getName());
  }
}
