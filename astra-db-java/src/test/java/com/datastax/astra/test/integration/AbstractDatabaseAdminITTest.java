package com.datastax.astra.test.integration;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Test connectivity to API
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public abstract class AbstractDatabaseAdminITTest implements TestDataSet {

    protected static DatabaseAdmin databaseAdmin;

    public DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            AbstractDatabaseAdminITTest.databaseAdmin = initDatabaseAdmin();
        }
        return databaseAdmin;
    }

    protected abstract AstraEnvironment getAstraEnvironment();
    protected abstract CloudProviderType getCloudProvider();
    protected abstract String getRegion();

    protected DatabaseAdmin initDatabaseAdmin() {
        return initializeDatabase(getAstraEnvironment(), getCloudProvider(), getRegion()).getDatabaseAdmin();
    }

    @Test
    @Order(1)
    void shouldListAvailableNamespace() {
        assertThat(getDatabaseAdmin()).isNotNull();
        // Sync
        assertThat(getDatabaseAdmin().listNamespaceNames()).isNotEmpty();

        // Async
        getDatabaseAdmin().listNamespaceNamesAsync()
                .thenApply(Set::size)
                .thenAccept(count -> assertThat(count).isPositive());
    }


    @Test
    @Order(2)
    void shouldListNamespaces() {
        assertThat(getDatabaseAdmin()).isNotNull();
        // Sync
        assertThat(getDatabaseAdmin().listNamespaceNames()).isNotEmpty();

        // Async
        getDatabaseAdmin().listNamespaceNamesAsync()
                .thenApply(Set::size)
                .thenAccept(count -> assertThat(count).isPositive());
    }

    @Test
    @Order(3)
    void shouldListEmbeddingProvider() {
        FindEmbeddingProvidersResult result = getDatabaseAdmin().findEmbeddingProviders();
        assertThat(result).isNotNull();
        Map<String, EmbeddingProvider> mapOfProviders = result.getEmbeddingProviders();
        assertThat(mapOfProviders).isNotNull();
        assertThat(mapOfProviders).isNotEmpty();
        assertThat(mapOfProviders).containsKeys("openai");
    }

    // --------------------
    // -- create Namespace
    // --------------------

    @Test
    @Order(4)
    void shouldCreateNamespaceDefault() throws InterruptedException {
        // When
        if (!getDatabaseAdmin().namespaceExists("nsx")) {
            getDatabaseAdmin().createNamespace("nsx");
            Thread.sleep(5000);
        }
        // Then
        assertThat(getDatabaseAdmin().namespaceExists("nsx")).isTrue();
        assertThat(getDatabaseAdmin().getDatabase("nsx").getNamespaceName()).isEqualTo("nsx");

        // When
        getDatabaseAdmin().createNamespaceAsync("ns2").thenAccept(dan -> assertThat(dan).isNotNull());

        // Surface
        final DatabaseAdmin dbAdmin2 = getDatabaseAdmin();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin2.createNamespace(null))
                .withMessage("Parameter 'keyspace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin2.createNamespace(""))
                .withMessage("Parameter 'keyspace' should be null nor empty");
    }

    @Test
    @Order(5)
    void shouldDropNamespace() throws InterruptedException {
        assertThat( getDatabaseAdmin().listNamespaceNames())
                .as("Check if 'ns2' is present in the namespace names")
                .anyMatch("nsx"::equals);
        assertThat( getDatabaseAdmin().namespaceExists("nsx")).isTrue();
        Database ns2 =  getDatabaseAdmin().getDatabase("nsx");
        assertThat("nsx").isNotNull();
        ns2.drop();
        while (getDatabaseAdmin().namespaceExists("tmp2")) {
            log.warn("Waiting for namespace 'tmp2' to be delete");
            Thread.sleep(1000);
        }
        assertThat(getDatabaseAdmin().namespaceExists("nsx")).isFalse();
    }

}
