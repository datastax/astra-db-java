package com.datastax.astra.documentation.db;

import com.datastax.astra.AstraDatabase;
import com.datastax.astra.AstraClientOptions;

public class InitAstraDBDatabase {
  public static void main(String[] args) {
    // Default initialization
    AstraDatabase db = new AstraDatabase("TOKEN", "API_ENDPOINT");

    // 'Options' allows fined-grained configuration.
    AstraClientOptions options = AstraClientOptions.builder()
            .connectionRequestTimeoutInSeconds(10)
            .connectionRequestTimeoutInSeconds(10)
            .build();
    AstraDatabase db2 = new AstraDatabase("TOKEN", "API_ENDPOINT", options);

    // Initialize with a non-default namespace.
    AstraDatabase db3 =
            new AstraDatabase("TOKEN", "API_ENDPOINT", "NAMESPACE");

    // non-default namespace + options
    AstraDatabase db4 =
            new AstraDatabase("TOKEN", "API_ENDPOINT", "NAMESPACE", options);
  }
}
