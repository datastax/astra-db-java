package com.datastax.astra.documentation;


import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;

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

        // Access the Database directly
        Database db = new Database("TOKEN", "API_ENDPOINT");
        Database db1 = new Database("TOKEN", "API_ENDPOINT", "NAMESPACE");

    }
}