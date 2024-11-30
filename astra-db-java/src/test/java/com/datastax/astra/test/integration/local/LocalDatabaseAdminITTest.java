package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.keyspaces.KeyspaceOptions;
import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@EnabledIfEnvironmentVariable(named = "ENABLED_TEST_DATA_API_LOCAL", matches = "true")
class LocalDatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    protected AstraEnvironment getAstraEnvironment() { return null; }
    @Override
    protected CloudProviderType getCloudProvider() { return null; }
    @Override
    protected String getRegion() { return "";}

    @Override
    protected Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.localDbWithDefaultKeyspace();
        }
        return database;
    }

    @Override
    public DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            AbstractDatabaseAdminITTest.databaseAdmin = getDatabase().getDatabaseAdmin();
        }
        return databaseAdmin;
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
        dbAdmin.createKeyspace("ns3", KeyspaceOptions.networkTopologyStrategy(Map.of("datacenter1", 1)));
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
