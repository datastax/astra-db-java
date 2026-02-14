package com.datastax.astra.test.integration;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.test.integration.utils.TestConfig;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static com.datastax.astra.internal.utils.AnsiUtils.green;

/**
 * Base class for integration tests that provides access to DataAPI client and database.
 * <p>
 * Configuration is loaded from test-config.properties files on the classpath.
 * Environment variables can override config file settings.
 * <p>
 * By default, tests run against local HCD/DSE instance (http://localhost:8181).
 * Set test.environment=astra_prod (or astra_dev) to run against Astra.
 *
 * @see TestConfig for configuration details
 */
@Slf4j
public abstract class AbstractDataAPITest {

    protected static TestConfig config;
    protected static DataAPIClient dataApiClient;
    protected static AstraDBAdmin astraDbAdmin;
    protected static Database database;
    protected static DatabaseAdmin databaseAdmin;
    protected static DataAPIDestination destination;

    @BeforeAll
    static void initTestConfig() {
        config = TestConfig.getInstance();
    }

    /**
     * Get the test configuration.
     */
    protected TestConfig getConfig() {
        if (config == null) {
            config = TestConfig.getInstance();
        }
        return config;
    }

    /**
     * Get the DataAPI destination based on configuration.
     */
    protected DataAPIDestination getDataApiDestination() {
        if (destination == null) {
            destination = getConfig().getDataAPIDestination();
        }
        return destination;
    }

    /**
     * Get the cloud provider for Astra tests.
     */
    protected CloudProviderType getCloudProvider() {
        return getConfig().getCloudProvider();
    }

    /**
     * Get the cloud region for Astra tests.
     */
    protected String getCloudRegion() {
        return getConfig().getCloudRegion();
    }

    /**
     * Get the DataAPI client, creating it if necessary.
     */
    protected DataAPIClient getDataApiClient() {
        if (dataApiClient == null) {
            log.info("Initializing DataAPI client for {}", getDataApiDestination().name());
            switch (getDataApiDestination()) {
                case ASTRA:
                    dataApiClient = DataAPIClients.astra(getConfig().getAstraToken());
                    break;
                case ASTRA_DEV:
                    dataApiClient = DataAPIClients.astraDev(getConfig().getAstraToken());
                    break;
                case ASTRA_TEST:
                    dataApiClient = DataAPIClients.astraTest(getConfig().getAstraToken());
                    break;
                case HCD:
                case DSE:
                    dataApiClient = DataAPIClients.clientHCD();
                    break;
                case CASSANDRA:
                    dataApiClient = DataAPIClients.clientCassandra();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Environment: " + getDataApiDestination());
            }
        }
        return dataApiClient;
    }

    /**
     * Get the Astra DB Admin (for organization-level operations).
     */
    protected AstraDBAdmin getAstraDBAdmin() {
        if (astraDbAdmin == null) {
            astraDbAdmin = getDataApiClient().getAdmin();
        }
        return astraDbAdmin;
    }

    /**
     * Get the database instance, creating it if necessary.
     */
    protected synchronized Database getDatabase() {
        if (database == null) {
            final DataAPIDestination env = getDataApiDestination();
            DatabaseOptions options = new DatabaseOptions().logRequest();
            switch (env) {
                case HCD:
                case CASSANDRA:
                case DSE:
                    database = getDataApiClient().getDatabase(getConfig().getLocalEndpoint(), options);
                    // Create keyspace if it doesn't exist
                    if (!database.getDatabaseAdmin().keyspaceExists(DataAPIClientOptions.DEFAULT_KEYSPACE)) {
                        log.info("Creating keyspace {}", DataAPIClientOptions.DEFAULT_KEYSPACE);
                        database.getDatabaseAdmin().createKeyspace(DataAPIClientOptions.DEFAULT_KEYSPACE);
                    }
                    break;
                case ASTRA:
                case ASTRA_DEV:
                case ASTRA_TEST:
                    String databaseName = env.name().toLowerCase() + "_"
                            + getCloudProvider().name().toLowerCase() + "_"
                            + getCloudRegion().replaceAll("-", "_");
                    log.info("Access Astra " + green("{}") + " in {}/{}.",
                            databaseName,
                            getCloudProvider().name().toLowerCase(),
                            getCloudRegion());
                    // We Want to add logging at DB level
                    DatabaseOptions optionsWithLogging = new DatabaseOptions(
                            getDataApiClient().getToken(),
                            getDataApiClient().getOptions().clone().logRequests());
                    database = getAstraDBAdmin()
                            .createDatabase(databaseName, getCloudProvider(), getCloudRegion())
                            .getDatabase(null,null, optionsWithLogging);

                    break;
                default:
                    throw new IllegalArgumentException("Invalid Environment: " + env);
            }
        }
        return database;
    }

    /**
     * Get the database admin for the current database.
     */
    protected DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            databaseAdmin = getDatabase().getDatabaseAdmin();
        }
        return databaseAdmin;
    }

    /**
     * Drop a collection by name.
     */
    protected void dropCollection(String name) {
        getDatabase().dropCollection(name);
        log.info("Collection {} dropped", name);
    }

    /**
     * Drop a table by name.
     */
    protected void dropTable(String name) {
        getDatabase().dropTable(name);
        log.info("Table {} dropped", name);
    }

    /**
     * Drop a UDT (user-defined type) by name.
     */
    protected void dropType(String name) {
        getDatabase().dropType(name);
        log.info("Type {} dropped", name);
    }

    /**
     * Drop all collections in the database and wait for drops to propagate.
     * Astra drops can be asynchronous, so this method polls until the
     * collection list is empty (up to 30 seconds).
     * <p>
     * Always operates on the default keyspace to avoid issues when
     * a previous test class switched the shared database to a test keyspace.
     */
    protected void dropAllCollections() {
        Database db = getDatabase().useKeyspace(DataAPIClientOptions.DEFAULT_KEYSPACE);
        List<String> names = db.listCollectionNames();
        if (names.isEmpty()) return;
        log.info("Dropping {} collection(s): {}", names.size(), names);
        names.forEach(db::dropCollection);
        // Wait for drops to fully propagate (Astra can be async)
        int maxWait = 30;
        List<String> remaining;
        while (!(remaining = db.listCollectionNames()).isEmpty() && maxWait-- > 0) {
            log.info("Waiting for collection drops to propagate... ({} remaining)", remaining.size());
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    /**
     * Drop all tables in the database.
     * <p>
     * Always operates on the default keyspace to avoid issues when
     * a previous test class switched the shared database to a test keyspace.
     */
    protected void dropAllTables() {
        Database db = getDatabase().useKeyspace(DataAPIClientOptions.DEFAULT_KEYSPACE);
        db.listTableNames().forEach(db::dropTable);
    }

    /**
     * Check if running against local environment.
     */
    protected boolean isLocal() {
        return getConfig().isLocal();
    }

    /**
     * Check if running against Astra.
     */
    protected boolean isAstra() {
        return getConfig().isAstra();
    }

    /**
     * Read an environment variable value.
     *
     * @param name the environment variable name
     * @return the value, or null if not set
     */
    protected String readEnvVariable(String name) {
        return System.getenv(name);
    }

    /**
     * Read an environment variable value with a default.
     *
     * @param name         the environment variable name
     * @param defaultValue the default value if not set
     * @return the value, or the default if not set
     */
    protected String readEnvVariable(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
}
