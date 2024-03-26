package com.datastax.astra.test.integration.admin;

import com.datastax.astra.test.TestConstants;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.internal.LoggingCommandObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test connectivity to API
 */
@Slf4j
abstract class AstractDatabaseAdminITTest implements TestConstants {

    protected abstract DatabaseAdmin initDatabaseAdmin();

    protected static DatabaseAdmin databaseAdmin;

    public DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            AstractDatabaseAdminITTest.databaseAdmin = initDatabaseAdmin();
        }
        return databaseAdmin;
    }

    // --------------------
    // -- Initialization
    // --------------------

    @Test
    void shouldConnectWithDefault() {
        // When
        DatabaseAdmin apiDataAPIClient = getDatabaseAdmin();
        if (apiDataAPIClient instanceof DataAPIDatabaseAdmin) {
            ((DataAPIDatabaseAdmin) apiDataAPIClient).registerListener("logger",
                    new LoggingCommandObserver(DatabaseAdmin.class));
        }

        // Then
        assertThat(apiDataAPIClient).isNotNull();
        assertThat(apiDataAPIClient.listNamespaceNames().count()).isGreaterThan(0);
        apiDataAPIClient
                .listNamespaceNamesAsync()
                .thenApply(Stream::count)
                .thenAccept(count -> assertThat(count).isEqualTo(0));
    }

    /*
    @Test
    void shouldConnectWithEndpointAndToken() {
        // When
        DatabaseAdmin apiDataAPIClient = getDatabaseAdmin();

        // Then
        assertThat(apiDataAPIClient.listNamespaceNames().count()).isGreaterThan(0);

        apiDataAPIClient.listNamespaceNamesAsync()
                .thenApply(s -> s.map(NamespaceInformation::getName))
                .thenApply(Stream::count)
                .thenAccept(count -> assertThat(count).isGreaterThan(0));
    }

    // --------------------
    // -- create Namespace
    // --------------------

    @Test
    void shouldCreateNamespaceDefault() {
        // Given
        DatabaseAdmin apiDataAPIClient = getDatabaseAdmin();
        // When
        apiDataAPIClient.createNamespace(NAMESPACE_NS1);
        // Then
        assertThat(apiDataAPIClient.namespaceExists(NAMESPACE_NS1)).isTrue();
        assertThat(apiDataAPIClient.getDatabase().getNamespaceName()).isEqualTo(NAMESPACE_NS1);

        // When
        apiDataAPIClient.createNamespaceAsync("ns2").thenAccept(dan -> assertThat(dan).isNotNull());

        // Surface
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> apiDataAPIClient.createNamespace(null))
                .withMessage("Parameter 'namespace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> apiDataAPIClient.createNamespace(""))
                .withMessage("Parameter 'namespace' should be null nor empty");

    }

    @Test
    void shouldCreateNamespaceSimpleStrategy() {
        // Given
        DatabaseAdmin apiDataAPIClient = DataApiClients.create();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver(DatabaseAdmin.class));
        Database ns2 = apiDataAPIClient.createNamespace("ns2",
                CreateNamespaceOptions.simpleStrategy(1));
        assertThat(ns2).isNotNull();
        assertThat(apiDataAPIClient.isNamespaceExists("ns2")).isTrue();
    }

    @Test
    void shouldCreateNamespaceNetworkStrategy() {
        // Given
        DatabaseAdmin apiDataAPIClient = DataApiClients.create();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver(DatabaseAdmin.class));
        // When
        Database ns3 = apiDataAPIClient.createNamespace("ns3",
                CreateNamespaceOptions.networkTopologyStrategy(Map.of("datacenter1", 1)));
        assertThat(ns3).isNotNull();
        assertThat(apiDataAPIClient.isNamespaceExists("ns3")).isTrue();

        // non-passing case
        assertThatExceptionOfType(DataApiException.class).isThrownBy(() ->
            apiDataAPIClient.createNamespace("ns4",
                        CreateNamespaceOptions.networkTopologyStrategy(Map.of("invalid", 1)))
        );

        // DROP NAMESPACES
        apiDataAPIClient.dropNamespace("ns3");
        assertThat(apiDataAPIClient.isNamespaceExists("ns3")).isFalse();
        apiDataAPIClient.dropNamespaceAsync("ns3");

        // non-passing case
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> apiDataAPIClient.dropNamespace(null))
                .withMessage("Parameter 'namespace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> apiDataAPIClient.dropNamespace(""))
                .withMessage("Parameter 'namespace' should be null nor empty");
    }

    @Test
    void shouldAccessNamespace() {
        // Given
        DatabaseAdmin apiDataAPIClient = DataApiClients.create();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver(DatabaseAdmin.class));

        apiDataAPIClient.createNamespace("ns2");
        assertThat(apiDataAPIClient.listNamespaceNames())
                .as("Check if 'ns2' is present in the namespace names")
                .anyMatch("ns2"::equals);
        assertThat(apiDataAPIClient.isNamespaceExists("ns2")).isTrue();

        Database ns2 = apiDataAPIClient.getNamespace("ns2");
        assertThat(ns2).isNotNull();

        ns2.drop();
        assertThat(apiDataAPIClient.isNamespaceExists("ns2")).isFalse();
    }

    @Test
    void shouldDropNamespace() {
        // Given
        DatabaseAdmin apiDataAPIClient = DataApiClients.create();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver(DatabaseAdmin.class));

        assertThat(apiDataAPIClient).isNotNull();
        Database tmp = apiDataAPIClient.createNamespace("tmp", CreateNamespaceOptions.simpleStrategy(1));
        assertThat(tmp).isNotNull();
        assertThat(apiDataAPIClient.isNamespaceExists("tmp")).isTrue();
        // When
        apiDataAPIClient.dropNamespace("tmp");
        assertThat(apiDataAPIClient.isNamespaceExists("tmp")).isFalse();
    }*/

}
