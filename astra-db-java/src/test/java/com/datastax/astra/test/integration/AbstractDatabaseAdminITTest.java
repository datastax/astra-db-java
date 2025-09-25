package com.datastax.astra.test.integration;

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.databases.commands.options.CreateKeyspaceOptions;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;
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
    void should_clean_up_db() {
        dropAllTables();
        dropAllCollections();
    }

    @Test
    @Order(2)
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
    @Order(3)
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
    @Order(4)
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
    @Order(5)
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
                .getKeyspace()).isEqualTo("nsx");

        if (!getDatabaseAdmin().keyspaceExists("nsx2")) {

            getDatabaseAdmin().createKeyspace(new KeyspaceDefinition().name("nsx2"), new CreateKeyspaceOptions().updateDBKeyspace(true));
            while (!getDatabaseAdmin().keyspaceExists("nsx2")) {
                Thread.sleep(1000);
            }
            assertThat(getDatabaseAdmin().getDatabase().getKeyspace()).isEqualTo("nsx2");
        }
    }

    @Test
    @Order(6)
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
