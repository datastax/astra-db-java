package com.datastax.astra.test;

import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceOptions;
import com.datastax.astra.client.exceptions.DataAPIException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class Local_01_DatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    // Initialization
    public String getEndPoint() {
        readEnvVariable("DATA_API_ENDPOINT");
        return "";
    }

    public String getUserName() {
        return "";
    }

    public DataAPIDestination getDataAPIDestination() {
        return DataAPIDestination.HCD;
    }

    @Test
    void shouldCreateKeyspaceSimpleStrategy() {
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) getDatabaseAdmin();
        dbAdmin.createKeyspace("ns2", KeyspaceOptions.simpleStrategy(1));
        assertThat(dbAdmin.keyspaceExists("ns2")).isTrue();
        Database ns2 = dbAdmin.getDatabase("ns2");
        assertThat(ns2).isNotNull();
    }

    @Test
    void shouldCreateKeyspaceNetworkStrategy() {
        // Given
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) getDatabaseAdmin();
        // When
        dbAdmin.createKeyspace("ns3", KeyspaceOptions.networkTopologyStrategy(Map.of("dc1", 1)));
        assertThat(dbAdmin.keyspaceExists("ns3")).isTrue();
        Database ns3 = dbAdmin.getDatabase("ns3");
        assertThat(ns3).isNotNull();

        // non-passing case
        final KeyspaceOptions options = KeyspaceOptions.networkTopologyStrategy(Map.of("invalid", 1));
        assertThatExceptionOfType(DataAPIException.class).isThrownBy(() ->
                dbAdmin.createKeyspace("ns4", options));

        // DROP NAMESPACES
        dbAdmin.dropKeyspace("ns3");
        assertThat(dbAdmin.keyspaceExists("ns3")).isFalse();
        dbAdmin.dropKeyspaceAsync("ns3");

        // non-passing case
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin.dropKeyspace(null))
                .withMessage("Parameter 'keyspace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin.dropKeyspace(""))
                .withMessage("Parameter 'keyspace' should be null nor empty");
    }
}
