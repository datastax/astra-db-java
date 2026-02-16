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

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.options.AstraFindAvailableRegionsOptions;
import com.datastax.astra.client.core.rerank.RerankProvider;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.core.vectorize.SupportModelStatus;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.options.FindEmbeddingProvidersOptions;
import com.datastax.astra.client.databases.commands.options.FindRerankingProvidersOptions;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import com.datastax.astra.client.exceptions.DataAPIResponseException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.admin.commands.AstraAvailableRegionInfo;
import com.datastax.astra.test.integration.model.TableEntityGameWithAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for Database Admin integration tests.
 * Extend this class and add environment-specific annotations.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractDatabaseAdminIT extends AbstractDataAPITest {

    @BeforeAll
    void cleanup_stale_test_keyspaces() throws InterruptedException {
        log.info("Cleaning up stale test keyspaces...");
        boolean dropped = false;
        for (String ks : getDatabaseAdmin().listKeyspaceNames()) {
            if (ks.startsWith("test_keyspace_")) {
                log.info("Dropping leftover test keyspace '{}'", ks);
                try {
                    getDatabaseAdmin().dropKeyspace(ks);
                    dropped = true;
                } catch (Exception e) {
                    log.warn("Failed to drop keyspace '{}': {}", ks, e.getMessage());
                }
            }
        }
        // Wait for drops to propagate so the DB returns to a stable state
        if (dropped) {
            waitForNoTestKeyspaces();
        }
    }

    /**
     * Wait until no test keyspaces remain (Astra keyspace ops can be async).
     */
    private void waitForNoTestKeyspaces() throws InterruptedException {
        int maxWait = 60;
        while (maxWait-- > 0) {
            boolean hasTestKs = getDatabaseAdmin().listKeyspaceNames().stream()
                    .anyMatch(ks -> ks.startsWith("test_keyspace_"));
            if (!hasTestKs) return;
            log.info("Waiting for test keyspace drops to propagate...");
            Thread.sleep(2000);
        }
    }

    @Test
    @Order(1)
    void should_list_keyspaces() {
        assertThat(getDatabaseAdmin()).isNotNull();
        assertThat(getDatabaseAdmin().listKeyspaceNames()).isNotEmpty();

        getDatabaseAdmin().listKeyspaceNamesAsync()
                .thenApply(Set::size)
                .thenAccept(count -> assertThat(count).isPositive());
    }

    @Test
    @Order(2)
    void should_list_embedding_providers() {
        FindEmbeddingProvidersResult result = getDatabaseAdmin().findEmbeddingProviders();
        assertThat(result).isNotNull();
        Map<String, EmbeddingProvider> mapOfProviders = result.getEmbeddingProviders();
        assertThat(mapOfProviders).isNotNull();
    }

    @Test
    @Order(3)
    void should_create_and_drop_keyspace() throws InterruptedException {
        String keyspaceName = "test_keyspace_" + System.currentTimeMillis();

        // Create keyspace — retry if the DB is still settling from a previous operation
        createKeyspaceWithRetry(keyspaceName, 10);

        // Wait for creation to propagate
        int maxWait = 30;
        while (!getDatabaseAdmin().keyspaceExists(keyspaceName) && maxWait-- > 0) {
            Thread.sleep(1000);
        }
        assertThat(getDatabaseAdmin().keyspaceExists(keyspaceName)).isTrue();

        // Get database for keyspace
        Database keyspaceDb = getDatabaseAdmin().getDatabase(keyspaceName);
        assertThat(keyspaceDb).isNotNull();
        assertThat(keyspaceDb.getKeyspace()).isEqualTo(keyspaceName);

        // Drop keyspace
        getDatabaseAdmin().dropKeyspace(keyspaceName);
        maxWait = 30;
        while (getDatabaseAdmin().keyspaceExists(keyspaceName) && maxWait-- > 0) {
            log.info("Waiting for keyspace '{}' to be deleted...", keyspaceName);
            Thread.sleep(1000);
        }
        assertThat(getDatabaseAdmin().keyspaceExists(keyspaceName)).isFalse();
    }

    /**
     * Attempt to create a keyspace, retrying on 409 (DB not in valid state).
     */
    private void createKeyspaceWithRetry(String keyspaceName, int maxRetries) throws InterruptedException {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                getDatabaseAdmin().createKeyspace(keyspaceName);
                return; // success
            } catch (IllegalStateException e) {
                if (e.getMessage() != null && e.getMessage().contains("HTTP_CONFLICT") && attempt < maxRetries) {
                    log.info("DB not ready for keyspace creation (attempt {}/{}), retrying in 5s...",
                            attempt, maxRetries);
                    Thread.sleep(5000);
                } else {
                    throw e;
                }
            }
        }
    }

    @Test
    @Order(4)
    void should_list_rerank_providers() {
        try {
            FindRerankingProvidersResult result = getDatabaseAdmin().findRerankingProviders();
            assertThat(result).isNotNull();
            Map<String, RerankProvider> mapOfProviders = result.getRerankingProviders();
            assertThat(mapOfProviders).isNotNull();
            assertThat(mapOfProviders).containsKey("nvidia");
        } catch (DataAPIResponseException e) {
            if ("RERANKING_FEATURE_NOT_ENABLED".equals(e.getErrorCode())) {
                log.info("Reranking providers are not enabled in this environment, skipping test.");
            } else {
                throw e;
            }
        }
    }

    @Test
    @Order(5)
    void should_list_embedding_providers_with_filter() {
        FindEmbeddingProvidersResult result = getDatabaseAdmin()
                .findEmbeddingProviders(new FindEmbeddingProvidersOptions()
                        .filterModelStatus(SupportModelStatus.SUPPORTED));
        assertThat(result).isNotNull();
        Map<String, EmbeddingProvider> providers = result.getEmbeddingProviders();
        assertThat(providers).isNotNull();
        providers.forEach((name, provider) ->
                log.info("Embedding provider (SUPPORTED): {} — {} model(s)",
                        name, provider.getModels().size()));
    }

    @Test
    @Order(6)
    void should_list_rerank_providers_with_filter() {
        try {
            FindRerankingProvidersResult result = getDatabaseAdmin()
                    .findRerankingProviders(new FindRerankingProvidersOptions()
                            .filterModelStatus(SupportModelStatus.SUPPORTED));
            assertThat(result).isNotNull();
            Map<String, RerankProvider> providers = result.getRerankingProviders();
            assertThat(providers).isNotNull();
            providers.forEach((name, provider) ->
                    log.info("Reranking provider (SUPPORTED): {} — {} model(s)",
                            name, provider.getModels().size()));
        } catch (DataAPIResponseException e) {
            if ("RERANKING_FEATURE_NOT_ENABLED".equals(e.getErrorCode())) {
                log.info("Reranking providers are not enabled in this environment, skipping test.");
            } else {
                throw e;
            }
        }
    }

    @Test
    @Order(7)
    void should_find_available_regions() {
        if (!isAstra()) {
            log.info("Skipping findAvailableRegions — only available on Astra");
            return;
        }
        AstraDBAdmin admin = getDatabase().getAdmin();
        List<AstraAvailableRegionInfo> regions = admin
                .findAvailableRegions(new AstraFindAvailableRegionsOptions());
        assertThat(regions).isNotEmpty();
        regions.forEach(r -> log.info("Region: {} / {} (enabled: {})",
                r.getCloudProvider(), r.getName(), r.isEnabled()));

        // Also test with onlyOrgEnabledRegions=false
        List<AstraAvailableRegionInfo> allRegions = admin
                .findAvailableRegions(new AstraFindAvailableRegionsOptions()
                        .onlyOrgEnabledRegions(false));
        assertThat(allRegions).isNotEmpty();
        assertThat(allRegions.size()).isGreaterThanOrEqualTo(regions.size());
        log.info("Org-enabled regions: {}, All regions: {}", regions.size(), allRegions.size());
    }

    @Test
    @Order(8)
    void should_use_keyspace_mutate_database() throws InterruptedException {
        String testKeyspace = "test_keyspace_" + System.currentTimeMillis();
        String testCollection = "test_col_useks";

        // Start from default_keyspace
        Database db = getDatabase().useKeyspace(DataAPIClientOptions.DEFAULT_KEYSPACE);
        assertThat(db.getKeyspace()).isEqualTo(DataAPIClientOptions.DEFAULT_KEYSPACE);

        // Create a new keyspace
        createKeyspaceWithRetry(testKeyspace, 10);
        int maxWait = 30;
        while (!getDatabaseAdmin().keyspaceExists(testKeyspace) && maxWait-- > 0) {
            Thread.sleep(1000);
        }
        assertThat(getDatabaseAdmin().keyspaceExists(testKeyspace)).isTrue();

        try {
            // Switch to the new keyspace — same db instance should be mutated
            db.useKeyspace(testKeyspace);
            assertThat(db.getKeyspace()).isEqualTo(testKeyspace);

            // Create a collection in the new keyspace
            db.createCollection(testCollection);
            assertThat(db.collectionExists(testCollection)).isTrue();

            // List collections should contain the new collection
            List<String> collections = db.listCollectionNames();
            assertThat(collections).contains(testCollection);

            // Clean up collection
            db.dropCollection(testCollection);
        } finally {
            // Restore default keyspace and drop test keyspace
            db.useKeyspace(DataAPIClientOptions.DEFAULT_KEYSPACE);
            getDatabaseAdmin().dropKeyspace(testKeyspace);
            maxWait = 30;
            while (getDatabaseAdmin().keyspaceExists(testKeyspace) && maxWait-- > 0) {
                Thread.sleep(1000);
            }
        }
    }

    @Test
    @Order(9)
    void should_create_table_on_nondefault_keyspace() throws InterruptedException {
        String testKeyspace = "test_keyspace_" + System.currentTimeMillis();

        // Create a new keyspace
        createKeyspaceWithRetry(testKeyspace, 10);
        int maxWait = 30;
        while (!getDatabaseAdmin().keyspaceExists(testKeyspace) && maxWait-- > 0) {
            Thread.sleep(1000);
        }
        assertThat(getDatabaseAdmin().keyspaceExists(testKeyspace)).isTrue();

        // Get database scoped to the new keyspace
        Database db = getDatabase().useKeyspace(testKeyspace);
        assertThat(db.getKeyspace()).isEqualTo(testKeyspace);

        String tableName = db.getTableName(TableEntityGameWithAnnotation.class);
        try {
            // This should NOT throw "Keyspace 'default_keyspace' doesn't exist"
            // Bug: createTable(Class) was ignoring the database keyspace for vector index creation
            Table<TableEntityGameWithAnnotation> table = db.createTable(TableEntityGameWithAnnotation.class);
            assertThat(table).isNotNull();
            assertThat(db.tableExists(tableName)).isTrue();
            assertThat(db.listTableNames()).contains(tableName);
        } finally {
            // Clean up: drop table, restore default keyspace, drop test keyspace
            if (db.tableExists(tableName)) {
                db.dropTable(tableName);
            }
            db.useKeyspace(DataAPIClientOptions.DEFAULT_KEYSPACE);
            getDatabaseAdmin().dropKeyspace(testKeyspace);
            maxWait = 30;
            while (getDatabaseAdmin().keyspaceExists(testKeyspace) && maxWait-- > 0) {
                Thread.sleep(1000);
            }
        }
    }
}
