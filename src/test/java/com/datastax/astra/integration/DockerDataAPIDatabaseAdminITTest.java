package com.datastax.astra.integration;

import com.datastax.astra.TestConstants;
import com.datastax.astra.internal.auth.StargateAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test connectivity to API
 */
@Slf4j
class DockerDataAPIDatabaseAdminITTest implements TestConstants {

    // --------------------
    // -- Initialization
    // --------------------

    @Test
    void shouldGetToken() {
        assertThat(new StargateAuthenticationService().getToken()).isNotEmpty();
    }

    /*
    @Test
    void shouldConnectWithDefault() {
        // When
        DatabaseAdmin apiDataAPIClient = DataApiClients.create();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver(DatabaseAdmin.class));
        // Then
        assertThat(apiDataAPIClient).isNotNull();
        assertThat(apiDataAPIClient.listNamespaceNames().count()).isGreaterThan(0);
        apiDataAPIClient
                .listNamespaceNamesAsync()
                .thenApply(Stream::count)
                .thenAccept(count -> assertThat(count).isEqualTo(0));
    }

    @Test
    void shouldConnectWithEndpointAndToken() {
        // When
        DatabaseAdmin apiDataAPIClient = DataApiClients.create(
                DataApiClients.DEFAULT_ENDPOINT,
                new StargateAuthenticationService().getToken(),
                HttpClientOptions.builder()
                        .userAgentCallerName("stargate-sdk-data")
                        .userAgentCallerVersion("2.0")
                        .build());
        // Then
        assertThat(apiDataAPIClient).isNotNull();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver("listNameSpaces"));
        assertThat(apiDataAPIClient.listNamespaceNames().count()).isGreaterThan(0);

        apiDataAPIClient.listNamespacesAsync()
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
        DatabaseAdmin apiDataAPIClient = DataApiClients.create();
        apiDataAPIClient.registerListener("logger", new LoggerCommandObserver(DatabaseAdmin.class));
        // When
        Database ns1 = apiDataAPIClient.createNamespace(NAMESPACE_NS1);
        // Then
        assertThat(ns1).isNotNull();
        assertThat(apiDataAPIClient.isNamespaceExists(NAMESPACE_NS1)).isTrue();
        assertThat(ns1.getNamespaceName()).isEqualTo(NAMESPACE_NS1);

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
