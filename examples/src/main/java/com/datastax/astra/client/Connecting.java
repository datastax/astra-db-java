package com.datastax.astra.client;


import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.util.UUID;

public class Connecting {
    public static void main(String[] args) {
        // Preferred Access with DataAPIClient (default options)
        DataAPIClient client = new DataAPIClient("TOKEN");

        // Overriding the default options
        DataAPIClient client1 = new DataAPIClient("TOKEN", DataAPIOptions
                .builder()
                .withHttpConnectTimeout(10)
                .withHttpRequestTimeout(10)
                .build());

        // Access the Database from its endpoint
        Database db1 = client1.getDatabase("*API_ENDPOINT*");
        Database db2 = client1.getDatabase("*API_ENDPOINT*", "*NAMESPACE*");

        // Access the Database from its endpoint
        UUID databaseId = UUID.fromString("f5abf92f-ff66-48a0-bbc2-d240bc25dc1f");
        Database db3 = client.getDatabase(databaseId);
        Database db4 = client.getDatabase(databaseId, "*NAMESPACE*");
        Database db5 = client.getDatabase(databaseId, "*NAMESPACE*", "us-east-2");

    }
}