package com.datastax.astra.samples.client;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.databases.Database;

import java.time.Duration;

public class SampleAstraTests {



    public static final String DB_VECTOR           = "https://8460e4f1-57d5-46e7-bff0-4f7b25458a92-us-east-2.apps.astra.datastax.com";
    public static final String DB_NONVECTOR        = "https://8460e4f1-57d5-46e7-bff0-4f7b25458a92-us-east-2.apps.astra.datastax.com/api/v1/json";
    public static final String DB_NONVECTOR_VECTOR = "https://c2550360-b819-4d1c-add9-82dfae9cd2ec-eu-central-1.apps.astra.datastax.com";
    public static final String ASTRA_TOKEN  = System.getenv("ASTRA_DB_APPLICATION_TOKEN");

    public static void main(String[] args) {

        //DataAPIClientOptions options = new DataAPIClientOptions().logRequests();
        //Database db = new DataAPIClient(ASTRA_TOKEN, options).getDatabase(DB_NONVECTOR_VECTOR);
        //db.listCollections().stream().forEach(System.out::println);


        // --- TOP LEVEL ---

        // Simpler Initialization
        DataAPIClient simpleClient = new DataAPIClient("AstraCS:....");


        DataAPIClient client = new DataAPIClient("YOUR_TOKEN");

        Database db = client
         .getDatabase("http://....", "default_keyspace");



        // Add extra options
        DataAPIClientOptions options = new DataAPIClientOptions()
                .logRequests() // log request to API
                .timeoutOptions(new TimeoutOptions()
                        .connectTimeout(Duration.ofSeconds(10))
                        .requestTimeout(Duration.ofSeconds(60)));
        DataAPIClient clientWithOptions = new DataAPIClient("AstraCS:....", options);


        // --- ACCESS DB ---


        Database defaultKeyspaceDB = simpleClient.getDatabase("http://....", "default_keyspace");

        Database extraKeyspaceDB = simpleClient.getDatabase("http://....", "default_keyspace");








        //db.createCollection("demo2");

    }
}
