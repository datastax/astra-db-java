package com.datastax.astra.documentation.client;

import com.datastax.astra.client.AstraDBAdmin;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.DataAPIOptions;

import java.net.http.HttpClient;

public class CreateeDataAPIClient {
  public static void main(String[] args) {

    // Client initialization.
    DataAPIOptions options = DataAPIOptions.builder()
            .withHttpConnectTimeout(10)
            .withHttpRequestTimeout(10)
            .withDestination(DataAPIDestination.ASTRA)
            .withCaller("myApp", "1.0")
            .withHtpVersion(HttpClient.Version.HTTP_2)
            .build();

    // Create client
   DataAPIClient client = new DataAPIClient("TOKEN", options);

    // Access Administration level (Astra ONLY)
    AstraDBAdmin astraDBAdmin = client.getAdmin();
    astraDBAdmin.createDatabase("demo");

  }
}
