package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.options.DataAPIOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.exception.DataAPIHttpException;
import com.datastax.astra.client.exception.DataAPIResponseException;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.Set;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests against a Local Instance of Stargate.
 */
@EnabledIfEnvironmentVariable(named = "ENABLED_TEST_DATA_API_LOCAL", matches = "true")
class LocalDatabaseITTest extends AbstractDatabaseTest {

    @Override
    protected AstraEnvironment getAstraEnvironment() { return null; }
    @Override
    protected CloudProviderType getCloudProvider() { return null; }
    @Override
    protected String getRegion() { return "";}
    @Override
    protected Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.defaultLocalDatabase();
        }
        return database;
    }

    @Test
    void shouldGetATokenFromAuthenticationEndpoint() {
        assertThat(new UsernamePasswordTokenProvider().getToken()).isNotNull();
    }

    @Test
    void shouldThrowAuthenticationCode() {
        UsernamePasswordTokenProvider tokenProvider = new UsernamePasswordTokenProvider("invalid", "invalid");
        assertThatThrownBy(tokenProvider::getToken).isInstanceOf(DataAPIHttpException.class);
    }

    @Test
    void shouldRunInvalidCommand() {
        try {
            //getDatabase().registerListener("demo", new MockCommandObserver());
            getDatabase().runCommand(new Command("invalid", new Document()));
            //getDatabase().deleteListener("demo");
        } catch(DataAPIResponseException dat) {
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
                new UsernamePasswordTokenProvider().getToken(),
                DataAPIOptions.builder()
                        .withDestination(DataAPIDestination.CASSANDRA)
                        .withHttpProxy(new HttpProxy(mockWebServer.getHostName(), mockWebServer.getPort()))
                        .build());
        Set<String> names = otherCallerClient
                .getDatabase(DEFAULT_ENDPOINT_LOCAL, DEFAULT_NAMESPACE)
                .getDatabaseAdmin()
                .listKeyspaceNames();
        assertThat(names).isNotNull();

        // Shutdown the server
        mockWebServer.shutdown();
    }

    @Test
    void shouldInitializeHttpClientWithCallerAndProxy() {
        DataAPIClient otherCallerClient = new DataAPIClient(
                new UsernamePasswordTokenProvider().getToken(),
                DataAPIOptions.builder()
                        .withDestination(DataAPIDestination.CASSANDRA)
                        .addCaller("Cedrick", "1.0")
                        .build());
        assertThat(otherCallerClient
                .getDatabase(DEFAULT_ENDPOINT_LOCAL, DEFAULT_NAMESPACE)
                .listCollectionNames()).isNotNull();
    }

}
