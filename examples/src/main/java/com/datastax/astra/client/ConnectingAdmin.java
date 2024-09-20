package com.datastax.astra.client;


import com.datastax.astra.client.admin.AstraDBAdmin;

import static com.dtsx.astra.sdk.db.domain.CloudProviderType.GCP;

public class ConnectingAdmin {
    public static void main(String[] args) {
        // Default Initialization
        DataAPIClient client = new DataAPIClient("TOKEN");

        // Accessing admin providing a new token possibly with stronger permissions
        AstraDBAdmin astradbAdmin = client.getAdmin("SUPER_USER_TOKEN");

        // Create a Database
        astradbAdmin.createDatabase("db-demo", GCP, "us-east-1").listKeyspaceNames();
    }
}