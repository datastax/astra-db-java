package com.datastax.astra.client.admin;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.DataAPIClient;

import java.util.UUID;

public class DropDatabase {
  public static void main(String[] args) {
    AstraDBAdmin astraDBAdmin = new DataAPIClient("TOKEN").getAdmin();

    // Delete an existing database
    astraDBAdmin.dropDatabase("<database_name>");

    // Delete an existing database by ID
    astraDBAdmin.dropDatabase(UUID.fromString("<database_id>"));

  }
}
