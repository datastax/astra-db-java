package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.options.AdminOptions;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests against a Local Instance of Stargate.
 */
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
class Local_02_Database_ITTest extends AbstractDatabaseTest {

    @Test
    void shouldGetATokenFromAuthenticationEndpoint() {
        assertThat(new UsernamePasswordTokenProvider().getToken()).isNotNull();
    }

    @Test
    void shouldRunInvalidCommand() {
        try {
            //getDatabase().registerListener("demo", new MockCommandObserver());
            getDatabase().runCommand(new Command("invalid", new Document()));
            //getDatabase().deleteListener("demo");
        } catch(DataAPIException dat) {
            assertThat(dat.getMessage()).contains("COMMAND_UNKNOWN");;
        }

        getDataApiClient().getDatabase("endpoints", new DatabaseOptions()
                .keyspace("sss").token("...")
                .dataAPIClientOptions(new DataAPIClientOptions()
                        .timeoutOptions(new TimeoutOptions()
                                .requestTimeout(Duration.ofSeconds(30)))));

    }

    @Test
    void shouldInitializeHttpClientWithProxy() throws IOException {
        // Create a MockWebServer
        MockWebServer mockWebServer = new MockWebServer();
        // Enqueue a mock response (this could be adapted to simulate proxy behavior)
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("{\n" +
                        "  \"status\": {\n" +
                        "    \"keyspaces\": [\n" +
                        "      \"mock1\",\n" +
                        "      \"mock2\"\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}")
                .setResponseCode(200));

        // Start the server
        mockWebServer.start();

        DataAPIClient otherCallerClient = DataAPIClients.clientCassandra();
        Set<String> names = otherCallerClient
                .getDatabase(DEFAULT_ENDPOINT_LOCAL)
                // Moving to admin I add a HTTP PROXY
                .getDatabaseAdmin(new AdminOptions()
                        .dataAPIClientOptions(new DataAPIClientOptions()
                                .timeoutOptions(new TimeoutOptions())
                                .httpClientOptions(new HttpClientOptions()
                                        .httpProxy(new HttpProxy(mockWebServer.getHostName(), mockWebServer.getPort()))
                                )
                                .logRequests()))
                .listKeyspaceNames();
        assertThat(names).isNotNull();

        // Shutdown the server
        mockWebServer.shutdown();
    }

    @Test
    void shouldInitializeHttpClientWithCallerAndProxy() {
        DataAPIClient otherCallerClient = new DataAPIClient(
                new UsernamePasswordTokenProvider().getToken(),
                new DataAPIClientOptions()
                        .destination(DataAPIDestination.CASSANDRA)
                        .addObserver(new LoggingCommandObserver(getClass()))
                        .addCaller("Cedrick", "1.0"));
        assertThat(otherCallerClient
                .getDatabase(DEFAULT_ENDPOINT_LOCAL)
                .listCollectionNames()).isNotNull();
    }

}
