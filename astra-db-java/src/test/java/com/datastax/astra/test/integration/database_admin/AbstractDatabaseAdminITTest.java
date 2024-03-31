package com.datastax.astra.test.integration.database_admin;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.test.TestConstants;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
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

    /**
     * Initialize the Test database on an Astra Environment.
     *
     * @param env
     *      target environment
     * @param cloud
     *      target cloud
     * @param region
     *      target region
     * @return
     *      the database instance
     */
    public static Database initAstraDatabase(AstraEnvironment env, CloudProviderType cloud, String region) {
        log.info("Working in environment '{}'", env.name());
        AstraDBAdmin client = getAstraDBClient(env);
        DatabaseAdmin databaseAdmin =  client.createDatabase(DATABASE_NAME, cloud, region);
        Database db = databaseAdmin.getDatabase();
        db.registerListener("logger", new LoggingCommandObserver(Database.class));
        return db;
    }

    /**
     * Access AstraDBAdmin for different environment (to create DB).
     *
     * @param env
     *      astra environment
     * @return
     *      instance of AstraDBAdmin
     */
    public static AstraDBAdmin getAstraDBClient(AstraEnvironment env) {
        switch (env) {
            case DEV:
                return DataAPIClients.createForAstraDev(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_DEV'")))
                        .getAdmin();
            case PROD:
                return DataAPIClients.create(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN'")))
                        .getAdmin();
            case TEST:
                return DataAPIClients.createForAstraTest(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_TEST")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_TEST'")))
                        .getAdmin();
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
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
        getDatabaseAdmin().createNamespace("tmp");
        Thread.sleep(1000);
        assertThat( getDatabaseAdmin().listNamespaceNames())
                .as("Check if 'ns2' is present in the namespace names")
                .anyMatch("tmp"::equals);
        assertThat( getDatabaseAdmin().namespaceExists("tmp")).isTrue();

        Database ns2 =  getDatabaseAdmin().getDatabase("tmp");
        System.out.println(ns2.getNamespaceName());
        assertThat(ns2).isNotNull();

        ns2.drop();
        assertThat(getDatabaseAdmin().namespaceExists("tmp")).isFalse();
    }



}
