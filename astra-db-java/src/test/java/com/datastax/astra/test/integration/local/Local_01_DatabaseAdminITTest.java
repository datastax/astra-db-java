package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceOptions;
import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "local")
class Local_01_DatabaseAdminITTest extends AbstractDatabaseAdminITTest {

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
