package com.datastax.astra.test.integration.database;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.exception.AuthenticationException;
import com.datastax.astra.client.exception.DataApiResponseException;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.auth.TokenProviderStargate;
import com.datastax.astra.test.unit.MockCommandObserver;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static com.datastax.astra.internal.auth.TokenProviderStargate.DEFAULT_AUTH_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class LocalDatabaseITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return DataAPIClients.createDefaultLocalDatabase();
    }

    @Test
    void shouldGetATokenFromAuthenticationEndpoint() {
        assertThat(new TokenProviderStargate().getToken()).isNotNull();
    }

    @Test
    void shouldThrowAuthenticationCode() {
        TokenProviderStargate tokenProvider = new TokenProviderStargate("invalid", "invalid", DEFAULT_AUTH_URL);
        assertThatThrownBy(tokenProvider::getToken).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldRunInvalidCommand() {
        try {
            getDatabase().registerListener("demo", new MockCommandObserver());
            getDatabase().runCommand(new Command("invalid", new Document()));
            getDatabase().deleteListener("demo");
        } catch(DataApiResponseException dat) {
            assertThat(dat.getMessage()).contains("No \"invalid\" command found ");
            assertThat(dat.getApiErrors()).isNotEmpty();
            assertThat(dat.getCommandsList()).isNotEmpty();
        }
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
                        "    \"namespaces\": [\n" +
                        "      \"mock1\",\n" +
                        "      \"mock2\"\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}")
                .setResponseCode(200));

        // Start the server
        mockWebServer.start();

        DataAPIClient otherCallerClient = new DataAPIClient(
                new TokenProviderStargate().getToken(),
                DataAPIOptions.builder()
                        .withDestination(DataAPIOptions.DataAPIDestination.CASSANDRA)
                        .withHttpProxy(new DataAPIOptions.HttpProxy(mockWebServer.getHostName(), mockWebServer.getPort()))
                        .build());
        Set<String> names = otherCallerClient
                .getDatabase(DEFAULT_ENDPOINT_LOCAL, DEFAULT_NAMESPACE)
                .getDatabaseAdmin()
                .listNamespaceNames();
        assertThat(names).isNotNull();

        // Shutdown the server
        mockWebServer.shutdown();
    }

    @Test
    void shouldInitializeHttpClientWithCallerAndProxy() {
        DataAPIClient otherCallerClient = new DataAPIClient(
                new TokenProviderStargate().getToken(),
                DataAPIOptions.builder()
                        .withDestination(DataAPIOptions.DataAPIDestination.CASSANDRA)
                        .withCaller("Cedrick", "1.0")
                        .build());
        assertThat(otherCallerClient
                .getDatabase(DEFAULT_ENDPOINT_LOCAL, DEFAULT_NAMESPACE)
                .listCollectionNames()).isNotNull();
    }

}
