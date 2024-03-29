package com.datastax.astra.client.database_admin;


import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;

import java.util.UUID;

import static com.dtsx.astra.sdk.db.domain.CloudProviderType.GCP;

public class GetDatabaseAdmin {
    public static void main(String[] args) {
        // Default Initialization
        DataAPIClient client = new DataAPIClient("TOKEN");

        // Accessing admin providing a new token possibly with stronger permissions
        AstraDBAdmin astradbAdmin = client.getAdmin("SUPER_USER_TOKEN");

        DatabaseAdmin admin = astradbAdmin.getDatabaseAdmin(UUID.fromString("<database_id>"));
    }
}