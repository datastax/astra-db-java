package com.datastax.astra.test.integration.local;

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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.OpenSearchOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.test.integration.AbstractDataAPITest;
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HCD OpenSearch-backed collections.
 * <p>
 * These tests exercise the {@code openSearch} option block added to
 * {@code createCollection} (§4.1) and the {@code deleteCollection} behaviour when
 * OpenSearch is enabled (§4.8) — the Data API must drop the OpenSearch custom index
 * before dropping the Cassandra table.
 * <p>
 * Tests require a local HCD instance running at {@code http://localhost:8181} with
 * the OpenSearch integration enabled. They are skipped automatically when the local
 * instance is not available.
 * <p>
 * Start with: {@code docker-compose up -d}
 */
@Slf4j
@EnabledIfLocalAvailable
@DisplayName("05. HCD OpenSearch Collection Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HCD_Collections_02_OpenSearchIT extends AbstractDataAPITest {

    private static final String COLLECTION_OS      = "os_persons";
    private static final String COLLECTION_OS_NAMED = "os_articles";

    // ------------------------------------------------------------------
    //  createCollection — openSearch.enabled:true with default index name
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("createCollection with openSearch enabled (default index name)")
    void should_create_collection_with_open_search_default_index_name() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        // Clean up any leftover from a previous run
        if (db.collectionExists(COLLECTION_OS)) {
            db.dropCollection(COLLECTION_OS);
        }

        CollectionDefinition definition = new CollectionDefinition()
                .openSearch(new OpenSearchOptions()
                        .enabled(true)
                        .mappings(Map.of(
                                "firstName", Map.of("type", "text"),
                                "lastName",  Map.of("type", "text"),
                                "city",      Map.of("type", "text"),
                                "age",       Map.of("type", "integer")
                        )));

        Collection<Document> collection = db.createCollection(COLLECTION_OS, definition);

        assertThat(collection).isNotNull();
        assertThat(collection.getCollectionName()).isEqualTo(COLLECTION_OS);
        assertThat(db.collectionExists(COLLECTION_OS)).isTrue();
    }

    // ------------------------------------------------------------------
    //  findCollections — openSearch block round-tripped in explain response
    // ------------------------------------------------------------------

    @Test
    @Order(2)
    @DisplayName("findCollections with explain returns openSearch block")
    void should_return_open_search_block_in_find_collections() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        // getDefinition() calls findCollections with explain:true internally
        CollectionDefinition def = db.getCollection(COLLECTION_OS).getDefinition();

        assertThat(def).isNotNull();
        assertThat(def.getOpenSearch()).isNotNull();
        assertThat(def.getOpenSearch().isEnabled()).isTrue();
        assertThat(def.getOpenSearch().getMappings()).isNotNull();
        assertThat(def.getOpenSearch().getMappings()).containsKey("firstName");
        log.info("openSearch definition round-tripped: {}", def.getOpenSearch().getMappings());
    }

    // ------------------------------------------------------------------
    //  createCollection — openSearch with explicit OS index name
    // ------------------------------------------------------------------

    @Test
    @Order(3)
    @DisplayName("createCollection with openSearch and explicit indexName")
    void should_create_collection_with_explicit_index_name() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        if (db.collectionExists(COLLECTION_OS_NAMED)) {
            db.dropCollection(COLLECTION_OS_NAMED);
        }

        CollectionDefinition definition = new CollectionDefinition()
                .openSearch(new OpenSearchOptions()
                        .enabled(true)
                        .indexName("articles-full-text")
                        .mappings(Map.of(
                                "title",  Map.of("type", "text", "analyzer", "english"),
                                "body",   Map.of("type", "text", "analyzer", "english"),
                                "author", Map.of("type", "keyword")
                        )));

        Collection<Document> collection = db.createCollection(COLLECTION_OS_NAMED, definition);

        assertThat(collection).isNotNull();
        assertThat(db.collectionExists(COLLECTION_OS_NAMED)).isTrue();

        // Verify the resolved indexName is round-tripped
        CollectionDefinition def = collection.getDefinition();
        assertThat(def.getOpenSearch()).isNotNull();
        assertThat(def.getOpenSearch().getIndexName()).isEqualTo("articles-full-text");
        log.info("indexName round-tripped: {}", def.getOpenSearch().getIndexName());
    }

    // ------------------------------------------------------------------
    //  insertMany — documents written to OS-backed collection
    // ------------------------------------------------------------------

    @Test
    @Order(4)
    @DisplayName("insertMany into openSearch-enabled collection succeeds")
    void should_insert_documents_into_open_search_collection() {
        Collection<Document> collection =
                getDatabase().useKeyspace(DEFAULT_KEYSPACE).getCollection(COLLECTION_OS);

        List<Document> docs = List.of(
                new Document().id("p1").append("firstName", "Alice").append("lastName", "Smith").append("city", "London").append("age", 30),
                new Document().id("p2").append("firstName", "Bob").append("lastName", "Jones").append("city", "Paris").append("age", 25),
                new Document().id("p3").append("firstName", "Carol").append("lastName", "Brown").append("city", "Berlin").append("age", 42)
        );

        CollectionInsertManyResult result = collection.insertMany(docs);

        assertThat(result).isNotNull();
        assertThat(result.getInsertedIds()).hasSize(3);
        log.info("Inserted {} documents into OpenSearch-backed collection", result.getInsertedIds().size());
    }

    // ------------------------------------------------------------------
    //  deleteCollection — drops OpenSearch index before dropping table
    // ------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("deleteCollection on openSearch-enabled collection succeeds")
    void should_drop_collection_with_open_search_enabled() throws InterruptedException {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        assertThat(db.collectionExists(COLLECTION_OS)).isTrue();

        // dropCollection must drop the OpenSearch custom index first (§4.8),
        // then drop the table — this should complete without error
        db.dropCollection(COLLECTION_OS);

        Thread.sleep(1000);
        assertThat(db.collectionExists(COLLECTION_OS)).isFalse();
        log.info("OpenSearch-backed collection {} dropped successfully", COLLECTION_OS);
    }

    @Test
    @Order(6)
    @DisplayName("deleteCollection on openSearch-enabled collection with explicit indexName succeeds")
    void should_drop_collection_with_explicit_open_search_index_name() throws InterruptedException {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        assertThat(db.collectionExists(COLLECTION_OS_NAMED)).isTrue();

        db.dropCollection(COLLECTION_OS_NAMED);

        Thread.sleep(1000);
        assertThat(db.collectionExists(COLLECTION_OS_NAMED)).isFalse();
        log.info("OpenSearch-backed collection {} dropped successfully", COLLECTION_OS_NAMED);
    }
}
