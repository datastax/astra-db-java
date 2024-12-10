package com.datastax.astra.client;

import com.datastax.astra.client.core.auth.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.command.ExecutionInfos;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;

public class Connecting {
  public static void main(String[] args) {

  // Preferred Access with DataAPIClient (default options) ASTRA
  DataAPIClient clientWithAstra =
    new DataAPIClient("AstraCS:TOKEN");

  // If you work locally, create a token from username and password
  String localToken =
    new UsernamePasswordTokenProvider("username", "password").getToken();
  DataAPIClient clientlocal = new DataAPIClient(localToken);

  // Specialization of the DataAPIClient
  DataAPIClientOptions options = new DataAPIClientOptions()
          .destination(DataAPIDestination.ASTRA) ; // HCD, DSE, CASSANDRA

  // Specialization of the HTTP CLIENT
  HttpClientOptions httpClientOptions = new HttpClientOptions()
          // RETRIES => default is not retry
          .retryCount(3).retryDelay(Duration.ofMillis(200))
          // Http Redirect
          .httpRedirect(HttpClient.Redirect.NORMAL)
          // Http version
          .httpVersion(HttpClient.Version.HTTP_2)
          // default is no proxy
          .httpProxy(new HttpProxy().hostname("localhost").port(8080));
  options.httpClientOptions(httpClientOptions);

  // Specialization of the TIMEOUTS
  TimeoutOptions timeoutsOptions = new TimeoutOptions()
      // Collection Admin (DDL)
      .collectionAdminTimeoutMillis(5000)
      .collectionAdminTimeout(Duration.ofMillis(5000))
      // Table Admin (DDL)
      .tableAdminTimeoutMillis(5000)
      .tableAdminTimeout(Duration.ofMillis(5000))
      // Database Admin (DDL)
      .databaseAdminTimeoutMillis(15000)
      .databaseAdminTimeout(Duration.ofMillis(15000))
      // Generation operation (DML)
      .generalMethodTimeoutMillis(1000)
      .generalMethodTimeout(Duration.ofMillis(1000))
      // Specialization of 1 http request when multiple are done (insert Many)
      .requestTimeoutMillis(200)
      .requestTimeout(Duration.ofMillis(200))
      //HTTP Connect delay
      .connectTimeoutMillis(100)
      .connectTimeout(Duration.ofMillis(100));
  options.timeoutOptions(timeoutsOptions);

  // Loggers and observers
  options.addObserver("my_dummy_logger", new CommandObserver() {
      @Override
      public void onCommand(ExecutionInfos executionInfo) {
          System.out.println("Command executed: " + executionInfo.getCommand().getName());
      }
  });
  options.logRequests(); // <-- get you a sl4j logger at debug level

  // Add your application in the chain of callers in the header
  options.addCaller("MySampleApplication", "1.0.0");

  // Add an header to computer embeddings externally (integration)
  options.embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("key_embeddings"));
  options.embeddingAuthProvider(new AWSEmbeddingHeadersProvider("aws_access_key", "aws_secret_key"));

  // Add Headers to call for admin or database operations
  options.addAdminAdditionalHeader("X-My-Header", "MyValue");
  options.addDatabaseAdditionalHeader("X-My-Header", "MyValue");

  // Create the client with the options
  DataAPIClient client1 = new DataAPIClient("token", options);

  // -------------------------------
  // -- Initializing Database ------
  // -------------------------------

  // Access the Database from its endpoint
  Database db1 = client1.getDatabase("*API_ENDPOINT*");
  Database db2 = client1.getDatabase("*API_ENDPOINT*", new DatabaseOptions()
    .keyspace("*KEYSPACE*"));

  // (ASTRA ONLY !) Access the Database from IDS
  UUID databaseId = UUID.fromString("f5abf92f-ff66-48a0-bbc2-d240bc25dc1f");
  Database db3 = client1.getDatabase(databaseId);
  Database db4 = client1.getDatabase(databaseId, new DatabaseOptions()
     .keyspace("*KEYSPACE*"));
  Database db5 = client1.getDatabase(databaseId, "us-east-2", new DatabaseOptions()
      .keyspace("*KEYSPACE*"));
  db5.useKeyspace("yet_another");

  }
}