package com.datastax.astra.client.database_admin;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.options.CreateKeyspaceOptions;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;

public class CreateKeyspace {
  public static void main(String[] args) {
    // Default initialization
    Database db = new DataAPIClient("TOKEN").getDatabase("API_ENDPOINT");

    // Create a new keyspace
    db.getDatabaseAdmin().createKeyspace("<keyspace_name>");

    // The database can be mutate on keyspace creation
    db.getDatabaseAdmin().createKeyspace(
            new KeyspaceDefinition().name("<keyspace2_name>"),
            new CreateKeyspaceOptions().updateDBKeyspace(true));
  }
}
