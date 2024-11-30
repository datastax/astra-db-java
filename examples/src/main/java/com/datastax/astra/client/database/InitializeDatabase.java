package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;

import java.net.http.HttpClient;
import java.time.Duration;

public class InitializeDatabase {
  public static void main(String[] args) {
    // Default initialization
    Database db = new DataAPIClient("TOKEN")
            .getDatabase("API_ENDPOINT");

    // Initialize with a non-default namespace.
    Database db2 = new DataAPIClient("TOKEN")
            .getDatabase("API_ENDPOINT", new DatabaseOptions().keyspace("KEYSPACE"));

    // non-default namespace + options
    // 'Options' allows fined-grained configuration.
    DataAPIClientOptions options = new DataAPIClientOptions()
            .httpClientOptions(new HttpClientOptions()
                    .httpVersion(HttpClient.Version.HTTP_2)
                    .httpRetries(1, Duration.ofMillis(100))
            )
            .timeoutOptions(new TimeoutOptions()
                    .connectTimeout(Duration.ofMillis(100))
                    .generalMethodTimeout(Duration.ofSeconds(5))
            );
    Database db3 = new DataAPIClient("TOKEN", options)
            .getDatabase("API_ENDPOINT");
  }
}
