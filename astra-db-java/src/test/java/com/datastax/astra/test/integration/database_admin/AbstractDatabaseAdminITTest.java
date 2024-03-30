package com.datastax.astra.test.integration.database_admin;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.TestConstants;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Test connectivity to API
 */
@Slf4j
abstract class AbstractDatabaseAdminITTest implements TestConstants {

    protected abstract DatabaseAdmin initDatabaseAdmin();

    protected static DatabaseAdmin databaseAdmin;

    public DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            AbstractDatabaseAdminITTest.databaseAdmin = initDatabaseAdmin();
            if (databaseAdmin instanceof DataAPIDatabaseAdmin) {
                ((DataAPIDatabaseAdmin) databaseAdmin).registerListener("logger",
                        new LoggingCommandObserver(DatabaseAdmin.class));
            }
        }
        return databaseAdmin;
    }

    @Test
    void shouldListAvailableNamespace() {
        assertThat(getDatabaseAdmin()).isNotNull();
        // Sync
        assertThat(getDatabaseAdmin().listNamespaceNames().size()).isGreaterThan(0);

        // Async
        getDatabaseAdmin().listNamespaceNamesAsync()
                .thenApply(Set::size)
                .thenAccept(count -> assertThat(count).isGreaterThan(0));
    }

    // --------------------
    // -- create Namespace
    // --------------------

    @Test
    void shouldCreateNamespaceDefault() {
        // When
        getDatabaseAdmin().createNamespace(NAMESPACE_NS1);
        // Then
        assertThat(getDatabaseAdmin().namespaceExists(NAMESPACE_NS1)).isTrue();
        assertThat(getDatabaseAdmin().getDatabase(NAMESPACE_NS1).getNamespaceName()).isEqualTo(NAMESPACE_NS1);

        // When
        getDatabaseAdmin().createNamespaceAsync("ns2").thenAccept(dan -> assertThat(dan).isNotNull());

        // Surface
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getDatabaseAdmin().createNamespace(null))
                .withMessage("Parameter 'namespace' should be null nor empty");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> getDatabaseAdmin().createNamespace(""))
                .withMessage("Parameter 'namespace' should be null nor empty");
    }

    @Test
    void shouldDropNamespace() throws InterruptedException {
        getDatabaseAdmin().createNamespace("ns2");
        Thread.sleep(1000);
        assertThat( getDatabaseAdmin().listNamespaceNames())
                .as("Check if 'ns2' is present in the namespace names")
                .anyMatch("ns2"::equals);
        assertThat( getDatabaseAdmin().namespaceExists("ns2")).isTrue();

        Database ns2 =  getDatabaseAdmin().getDatabase("ns2");
        System.out.println(ns2.getNamespaceName());
        assertThat(ns2).isNotNull();

        ns2.drop();
        assertThat(getDatabaseAdmin().namespaceExists("ns2")).isFalse();
    }



}
