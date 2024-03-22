package com.datastax.astra.documentation.client;

import com.datastax.astra.client.AstraDBAdmin;
import com.datastax.astra.AstraClientOptions;

public class InitAstraDBClient {
  public static void main(String[] args) {

    // Default Initialization
    AstraDBAdmin client = new AstraDBAdmin("TOKEN");

    // Specialize with some extra options
    AstraDBAdmin client2 = new AstraDBAdmin("TOKEN", AstraClientOptions.builder()
            .connectionRequestTimeoutInSeconds(10)
            .responseTimeoutInSeconds(10)
            // more options
            .build());

    // You can omit the token if you defined the `ASTRA_DB_APPLICATION_TOKEN`
    // environment variable or if you are using the Astra CLI.
    AstraDBAdmin defaultClient = new AstraDBAdmin();
  }
}
