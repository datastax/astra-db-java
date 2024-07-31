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
    void shouldListAvailableNamespace() {
        // Initialization
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
            while (!getDatabaseAdmin().namespaceExists("nsx")) {
                Thread.sleep(1000);
            }
        }

        // Then
        assertThat(getDatabaseAdmin().namespaceExists("nsx")).isTrue();
        assertThat(getDatabaseAdmin().getDatabase("nsx")
                .getNamespaceName()).isEqualTo("nsx");

        if (!getDatabaseAdmin().namespaceExists("nsx2")) {
            getDatabaseAdmin().createNamespace("nsx2", true);
            while (!getDatabaseAdmin().namespaceExists("nsx2")) {
                Thread.sleep(1000);
            }
            assertThat(getDatabaseAdmin().getDatabase().getNamespaceName()).isEqualTo("nx2");
        }

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
        assertThat(getDatabaseAdmin().listNamespaceNames())
                .as("Check if 'nsx' is present in the namespace names")
                .anyMatch("nsx"::equals);
        assertThat( getDatabaseAdmin().namespaceExists("nsx")).isTrue();
        Database ns2 = getDatabaseAdmin().getDatabase("nsx");
        assertThat(ns2).isNotNull();
        try {
            ns2.drop();
            while (getDatabaseAdmin().namespaceExists("nsx")) {
                log.warn("Waiting for namespace 'nsx' to be delete");
                Thread.sleep(1000);
            }
            assertThat(getDatabaseAdmin().namespaceExists("nsx")).isFalse();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
