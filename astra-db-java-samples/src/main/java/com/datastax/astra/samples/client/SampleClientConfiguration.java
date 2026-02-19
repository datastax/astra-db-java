package com.datastax.astra.samples.client;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.headers.AWSEmbeddingHeadersProvider;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
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

/**
 * Full client configuration cookbook: {@link DataAPIClientOptions}, {@link HttpClientOptions}
 * (retries, proxy, HTTP/2), {@link TimeoutOptions}, {@link CommandObserver},
 * embedding header providers, multiple database access patterns.
 *
 * @see DataAPIClient
 * @see DataAPIClientOptions
 */
@SuppressWarnings("unused")
public class SampleClientConfiguration {

    /** Connect to Astra with default options. */
    static void connectToAstra() {
        DataAPIClient client = new DataAPIClient("AstraCS:TOKEN");
    }

    /** Connect locally with username/password token. */
    static void connectToLocal() {
        String localToken = new UsernamePasswordTokenProvider("username", "password").getToken();
        DataAPIClient client = new DataAPIClient(localToken);
    }

    /** Configure all client options: destination, HTTP, timeouts, observers, headers. */
    static void configureFullOptions() {
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA);

        // HTTP client options: retries, redirect, HTTP/2, proxy
        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .retryCount(3).retryDelay(Duration.ofMillis(200))
                .httpRedirect(HttpClient.Redirect.NORMAL)
                .httpVersion(HttpClient.Version.HTTP_2)
                .httpProxy(new HttpProxy().hostname("localhost").port(8080));
        options.httpClientOptions(httpClientOptions);

        // Timeout options for each operation category
        TimeoutOptions timeoutsOptions = new TimeoutOptions()
                .collectionAdminTimeoutMillis(5000)
                .collectionAdminTimeout(Duration.ofMillis(5000))
                .tableAdminTimeoutMillis(5000)
                .tableAdminTimeout(Duration.ofMillis(5000))
                .databaseAdminTimeoutMillis(15000)
                .databaseAdminTimeout(Duration.ofMillis(15000))
                .generalMethodTimeoutMillis(1000)
                .generalMethodTimeout(Duration.ofMillis(1000))
                .requestTimeoutMillis(200)
                .requestTimeout(Duration.ofMillis(200))
                .connectTimeoutMillis(100)
                .connectTimeout(Duration.ofMillis(100));
        options.timeoutOptions(timeoutsOptions);

        // Observers / loggers
        options.addObserver("my_dummy_logger", new CommandObserver() {
            @Override
            public void onCommand(ExecutionInfos executionInfo) {
                System.out.println("Command executed: " + executionInfo.getCommand().getName());
            }
        });
        options.logRequests();

        // Caller identity in request headers
        options.addCaller("MySampleApplication", "1.0.0");

        // Embedding header providers
        options.embeddingHeadersProvider(new EmbeddingAPIKeyHeaderProvider("key_embeddings"));
        options.embeddingHeadersProvider(new AWSEmbeddingHeadersProvider("aws_access_key", "aws_secret_key"));

        // Additional custom headers
        options.addAdminAdditionalHeader("X-My-Header", "MyValue");
        options.addDatabaseAdditionalHeader("X-My-Header", "MyValue");

        DataAPIClient client = new DataAPIClient("TOKEN", options);
    }

    /** Multiple ways to obtain a Database reference. */
    static void accessDatabase() {
        DataAPIClient client = new DataAPIClient("TOKEN");

        // From endpoint
        Database db1 = client.getDatabase("API_ENDPOINT");
        Database db2 = client.getDatabase("API_ENDPOINT", new DatabaseOptions()
                .keyspace("KEYSPACE"));

        // From Astra database UUID
        UUID databaseId = UUID.fromString("f5abf92f-ff66-48a0-bbc2-d240bc25dc1f");
        Database db3 = client.getDatabase(databaseId);
        Database db4 = client.getDatabase(databaseId, new DatabaseOptions()
                .keyspace("KEYSPACE"));
        Database db5 = client.getDatabase(databaseId, "us-east-2", new DatabaseOptions()
                .keyspace("KEYSPACE"));
        db5.useKeyspace("yet_another");
    }
}
