package com.datastax.astra.test.integration.database_admin;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.NamespaceOptions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LocalAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    protected DatabaseAdmin initDatabaseAdmin() {
        return DataAPIClients.createDefaultLocalDatabase().getDatabaseAdmin();
    }

    @Test
    void shouldCreateNamespaceSimpleStrategy() {
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) getDatabaseAdmin();
        dbAdmin.createNamespace("ns2", NamespaceOptions.simpleStrategy(1));
        assertThat(dbAdmin.namespaceExists("ns2")).isTrue();
        Database ns2 = dbAdmin.getDatabase("ns2");
        assertThat(ns2).isNotNull();
    }

    @Test
    void shouldCreateNamespaceNetworkStrategy() {
        // Given
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) getDatabaseAdmin();
        // When
        dbAdmin.createNamespace("ns3", NamespaceOptions.networkTopologyStrategy(Map.of("datacenter1", 1)));
        assertThat(dbAdmin.namespaceExists("ns3")).isTrue();
        Database ns3 = dbAdmin.getDatabase("ns3");
        assertThat(ns3).isNotNull();

        // non-passing case
        assertThatExceptionOfType(DataApiException.class).isThrownBy(() ->
                dbAdmin.createNamespace("ns4",
                        NamespaceOptions.networkTopologyStrategy(Map.of("invalid", 1)))
        );

        // DROP NAMESPACES
        dbAdmin.dropNamespace("ns3");
        assertThat(dbAdmin.namespaceExists("ns3")).isFalse();
        dbAdmin.dropNamespaceAsync("ns3");

        // non-passing case
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin.dropNamespace(null))
                .withMessage("Parameter 'namespace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin.dropNamespace(""))
                .withMessage("Parameter 'namespace' should be null nor empty");
    }
}
