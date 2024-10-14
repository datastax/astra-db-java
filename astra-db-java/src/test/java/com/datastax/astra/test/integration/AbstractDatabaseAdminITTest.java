package com.datastax.astra.test.integration;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;
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
public abstract class AbstractDatabaseAdminITTest extends AbstractDataAPITest {

    @Test
    @Order(1)
    void shouldListAvailableKeyspace() {
        // Initialization
        assertThat(getDatabaseAdmin()).isNotNull();

        // Sync
        assertThat(getDatabaseAdmin().listKeyspaceNames()).isNotEmpty();

        // Async
        getDatabaseAdmin().listKeyspacesNamesAsync()
                .thenApply(Set::size)
                .thenAccept(count -> assertThat(count).isPositive());
    }

    @Test
    @Order(2)
    void shouldListKeyspaces() {
        assertThat(getDatabaseAdmin()).isNotNull();
        // Sync
        assertThat(getDatabaseAdmin().listKeyspaceNames()).isNotEmpty();

        // Async
        getDatabaseAdmin().listKeyspacesNamesAsync()
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
    void shouldKeyNamespaceDefault() throws InterruptedException {
        // When
        if (!getDatabaseAdmin().keyspaceExists("nsx")) {
            getDatabaseAdmin().createKeyspace("nsx");
            while (!getDatabaseAdmin().keyspaceExists("nsx")) {
                Thread.sleep(1000);
            }
        }

        // Then
        assertThat(getDatabaseAdmin().keyspaceExists("nsx")).isTrue();
        assertThat(getDatabaseAdmin().getDatabase("nsx")
                .getKeyspaceName()).isEqualTo("nsx");

        if (!getDatabaseAdmin().keyspaceExists("nsx2")) {
            getDatabaseAdmin().createKeyspace("nsx2", true);
            while (!getDatabaseAdmin().keyspaceExists("nsx2")) {
                Thread.sleep(1000);
            }
            assertThat(getDatabaseAdmin().getDatabase().getKeyspaceName()).isEqualTo("nsx2");
        }

        // Surface
        final DatabaseAdmin dbAdmin2 = getDatabaseAdmin();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin2.createKeyspace(null))
                .withMessage("Parameter 'keyspace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> dbAdmin2.createKeyspace(""))
                .withMessage("Parameter 'keyspace' should be null nor empty");
    }

    @Test
    @Order(5)
    void shouldCreateKeyspace() throws InterruptedException {
        assertThat(getDatabaseAdmin().listKeyspaceNames())
                .as("Check if 'nsx' is present in the namespace names")
                .anyMatch("nsx"::equals);
        assertThat(getDatabaseAdmin().keyspaceExists("nsx")).isTrue();
        Database ns2 = getDatabaseAdmin().getDatabase("nsx");
        assertThat(ns2).isNotNull();

            getDatabaseAdmin().dropKeyspace("nsx");
            while (getDatabaseAdmin().keyspaceExists("nsx")) {
                log.warn("Waiting for namespace 'nsx' to be delete");
                Thread.sleep(1000);
            }
            assertThat(getDatabaseAdmin().keyspaceExists("nsx")).isFalse();

    }

}
