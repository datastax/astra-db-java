package com.datastax.astra.client.admin;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.DataAPIClient;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.util.UUID;

public class CreateDatabase {
  public static void main(String[] args) {
    AstraDBAdmin astraDBAdmin = new DataAPIClient("TOKEN").getAdmin();

    // Choose a cloud provider (GCP, AZURE, AWS) and a region
    CloudProviderType cloudProvider = CloudProviderType.GCP;
    String cloudRegion = "us-east1";

    // Create a database
    DatabaseAdmin admin = astraDBAdmin.createDatabase("DATABASE_NAME", cloudProvider, cloudRegion);
  }
}
