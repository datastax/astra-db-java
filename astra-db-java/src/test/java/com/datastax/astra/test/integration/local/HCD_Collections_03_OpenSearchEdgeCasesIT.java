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
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.OpenSearchOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.opensearch.OpenSearchQuery;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.test.integration.AbstractDataAPITest;
import com.datastax.astra.test.integration.utils.EnabledIfLocalAvailable;
import com.datastax.astra.test.integration.utils.OpenSearchTestClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.datastax.astra.client.core.query.Filters.openSearch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for Collection edge cases with OpenSearch:
 * <ul>
 *   <li>Multi-level nested objects (2+ levels deep)</li>
 *   <li>OpenSearch {@code type: "nested"} arrays of objects</li>
 *   <li>EJSON extension types ({@code $date}, {@code $uuid}, {@code byte[]} as base64 binary)</li>
 *   <li>Co-existence of Data API native vector search and OpenSearch DSL</li>
 * </ul>
 */
@Slf4j
@EnabledIfLocalAvailable
@DisplayName("07. HCD OpenSearch Collection Edge Cases Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HCD_Collections_03_OpenSearchEdgeCasesIT extends AbstractDataAPITest {

    private static final String KEYSPACE        = "clun_opensearch";
    private static final String COL_EDGE_CASES  = "edge_cases";
    private static final String COL_VECTOR_OS   = "vector_os_combo";
    private static final String IDX_EDGE_CASES  = "hcd_clun_opensearch_edge_cases";
    private static final String IDX_VECTOR_OS   = "hcd_clun_opensearch_vector_os_combo";

    private static final String OS_URL = System.getProperty("opensearch.url", "http://localhost:9200");
    private final OpenSearchTestClient osClient = new OpenSearchTestClient(OS_URL);

    @BeforeAll
    void cleanStart() {
        if (!getDatabaseAdmin().keyspaceExists(KEYSPACE)) {
            getDatabaseAdmin().createKeyspace(KEYSPACE);
        }
        getDatabase().useKeyspace(KEYSPACE);

        if (db().collectionExists(COL_EDGE_CASES)) {
            db().dropCollection(COL_EDGE_CASES);
        }
        if (db().collectionExists(COL_VECTOR_OS)) {
            db().dropCollection(COL_VECTOR_OS);
        }
        osClient.deleteIndex(IDX_EDGE_CASES);
        osClient.deleteIndex(IDX_VECTOR_OS);
    }

    @AfterAll
    void cleanEnd() {
        try {
            if (db().collectionExists(COL_EDGE_CASES)) {
                db().dropCollection(COL_EDGE_CASES);
            }
            if (db().collectionExists(COL_VECTOR_OS)) {
                db().dropCollection(COL_VECTOR_OS);
            }
        } finally {
            osClient.deleteIndex(IDX_EDGE_CASES);
            osClient.deleteIndex(IDX_VECTOR_OS);
        }
    }

    // ==================================================================
    //  SCENARIO 1: Nested Types & EJSON Extension Types
    // ==================================================================

    @Test
    @Order(1)
    @DisplayName("E.1 create collection with 2-level nested objects, nested array, and EJSON mappings")
    void e1_should_create_edge_cases_collection() {
        db().createCollection(COL_EDGE_CASES, new CollectionDefinition()
                .openSearch(new OpenSearchOptions()
                        .enabled(true)
                        .mappings(Map.of(
                                "company", Map.of(
                                        "properties", Map.of(
                                                "name", Map.of("type", "text"),
                                                "department", Map.of(
                                                        "properties", Map.of(
                                                                "name", Map.of("type", "text"),
                                                                "budget", Map.of("type", "integer")
                                                        )
                                                )
                                        )
                                ),
                                "reviews", Map.of(
                                        "type", "nested",
                                        "properties", Map.of(
                                                "author", Map.of("type", "keyword"),
                                                "score", Map.of("type", "integer")
                                        )
                                ),
                                "publishedAt", Map.of(
                                        "properties", Map.of(
                                                "$date", Map.of("type", "date")
                                        )
                                ),
                                "userId", Map.of(
                                        "properties", Map.of(
                                                "$uuid", Map.of("type", "keyword")
                                        )
                                ),
                                "avatar", Map.of("type", "binary")
                        ))));

        assertThat(osClient.indexStatus(IDX_EDGE_CASES)).isEqualTo(200);
        JsonNode mappings = osClient.mappings(IDX_EDGE_CASES);
        assertThat(mappings.path("company").path("properties").path("department").path("properties").path("budget").path("type").asText()).isEqualTo("integer");
        assertThat(mappings.path("reviews").path("type").asText()).isEqualTo("nested");
        assertThat(mappings.path("publishedAt").path("properties").path("$date").path("type").asText()).isEqualTo("date");
        assertThat(mappings.path("userId").path("properties").path("$uuid").path("type").asText()).isEqualTo("keyword");
        assertThat(mappings.path("avatar").path("type").asText()).isEqualTo("binary");
    }

    @Test
    @Order(2)
    @DisplayName("E.2 insert documents with nested objects, nested arrays, and EJSON values")
    void e2_should_insert_documents() {
        Collection<Document> collection = db().getCollection(COL_EDGE_CASES);

        Document doc1 = new Document("doc-1")
                .append("company", Map.of(
                        "name", "Acme Corp",
                        "department", Map.of("name", "Engineering", "budget", 500000)
                ))
                .append("reviews", List.of(
                        Map.of("author", "Alice", "score", 5),
                        Map.of("author", "Bob", "score", 3)
                ))
                .append("publishedAt", Instant.ofEpochMilli(1730419200000L))
                .append("userId", UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
                .append("avatar", "hello".getBytes());

        Document doc2 = new Document("doc-2")
                .append("company", Map.of(
                        "name", "Beta Ltd",
                        "department", Map.of("name", "Marketing", "budget", 200000)
                ))
                .append("reviews", List.of(
                        Map.of("author", "Charlie", "score", 4)
                ))
                .append("publishedAt", Instant.ofEpochMilli(1730505600000L))
                .append("userId", UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22"))
                .append("avatar", "world".getBytes());

        collection.insertMany(List.of(doc1, doc2));

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() ->
                assertThat(osClient.count(IDX_EDGE_CASES)).isEqualTo(2));
        osClient.refresh(IDX_EDGE_CASES);
    }

    @Test
    @Order(3)
    @DisplayName("E.3 query 2-level nested objects with dot-notation")
    void e3_should_query_multilevel_nested_objects() {
        Collection<Document> collection = db().getCollection(COL_EDGE_CASES);

        // Match on company.department.name
        List<Document> res1 = collection.find(openSearch(OpenSearchQuery.match("company.department.name", "Engineering"))).toList();
        assertThat(res1).hasSize(1);
        assertThat(res1.get(0).getId(String.class)).isEqualTo("doc-1");

        // Range on company.department.budget
        List<Document> res2 = collection.find(openSearch(OpenSearchQuery.range("company.department.budget", 300000, 600000))).toList();
        assertThat(res2).hasSize(1);
        assertThat(res2.get(0).getId(String.class)).isEqualTo("doc-1");
    }

    @Test
    @Order(4)
    @DisplayName("E.4 query type: nested array preserving element isolation")
    void e4_should_query_nested_array() {
        Collection<Document> collection = db().getCollection(COL_EDGE_CASES);

        // doc-1 has Alice with score 5 and Bob with score 3.
        // Nested query for (author=Alice AND score >= 4) matches doc-1.
        Map<String, Object> matchingQuery = Map.of(
                "nested", Map.of(
                        "path", "reviews",
                        "query", Map.of(
                                "bool", Map.of(
                                        "must", List.of(
                                                Map.of("term", Map.of("reviews.author", "Alice")),
                                                Map.of("range", Map.of("reviews.score", Map.of("gte", 4)))
                                        )
                                )
                        )
                )
        );
        List<Document> res1 = collection.find(openSearch(matchingQuery)).toList();
        assertThat(res1).hasSize(1);
        assertThat(res1.get(0).getId(String.class)).isEqualTo("doc-1");

        // Nested query for (author=Alice AND score <= 3) should return 0 documents
        // because Alice's score is 5 (Bob's score is 3, but nested isolates elements).
        Map<String, Object> nonMatchingQuery = Map.of(
                "nested", Map.of(
                        "path", "reviews",
                        "query", Map.of(
                                "bool", Map.of(
                                        "must", List.of(
                                                Map.of("term", Map.of("reviews.author", "Alice")),
                                                Map.of("range", Map.of("reviews.score", Map.of("lte", 3)))
                                        )
                                )
                        )
                )
        );
        List<Document> res2 = collection.find(openSearch(nonMatchingQuery)).toList();
        assertThat(res2).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("E.5 query EJSON $date range and $uuid term")
    void e5_should_query_ejson_types() {
        Collection<Document> collection = db().getCollection(COL_EDGE_CASES);

        // Date range query on publishedAt.$date
        List<Document> dateRes = collection.find(openSearch(
                OpenSearchQuery.range("publishedAt.$date", 1730400000000L, 1730450000000L))).toList();
        assertThat(dateRes).hasSize(1);
        assertThat(dateRes.get(0).getId(String.class)).isEqualTo("doc-1");

        // UUID term query on userId.$uuid
        List<Document> uuidRes = collection.find(openSearch(
                OpenSearchQuery.term("userId.$uuid", "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))).toList();
        assertThat(uuidRes).hasSize(1);
        assertThat(uuidRes.get(0).getId(String.class)).isEqualTo("doc-1");
    }

    // ==================================================================
    //  SCENARIO 2: Vector Search & OpenSearch Co-Existence
    // ==================================================================

    @Test
    @Order(6)
    @DisplayName("E.6 create collection with both native Vector and OpenSearch options")
    void e6_should_create_vector_and_opensearch_collection() {
        db().createCollection(COL_VECTOR_OS, new CollectionDefinition()
                .vectorDimension(3)
                .vectorSimilarity(SimilarityMetric.COSINE)
                .openSearch(new OpenSearchOptions()
                        .enabled(true)
                        .mappings(Map.of(
                                "title", Map.of("type", "text"),
                                "category", Map.of("type", "keyword")
                        ))));

        Collection<Document> collection = db().getCollection(COL_VECTOR_OS);
        collection.insertMany(List.of(
                new Document("v-1")
                        .vector(new float[]{1.0f, 0.0f, 0.0f})
                        .append("title", "Distributed Systems Guide")
                        .append("category", "engineering"),
                new Document("v-2")
                        .vector(new float[]{0.0f, 1.0f, 0.0f})
                        .append("title", "Machine Learning in Production")
                        .append("category", "ai")
        ));

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() ->
                assertThat(osClient.count(IDX_VECTOR_OS)).isEqualTo(2));
        osClient.refresh(IDX_VECTOR_OS);
    }

    @Test
    @Order(7)
    @DisplayName("E.7 verify native Vector search and OpenSearch DSL both work independently")
    void e7_should_search_vector_and_opensearch_independently() {
        Collection<Document> collection = db().getCollection(COL_VECTOR_OS);

        // 1. Native Data API Vector Search
        List<Document> vectorRes = collection.find(null,
                new CollectionFindOptions().sort(Sort.vector(new float[]{0.9f, 0.1f, 0.0f})).limit(1)).toList();
        assertThat(vectorRes).hasSize(1);
        assertThat(vectorRes.get(0).getId(String.class)).isEqualTo("v-1");

        // 2. OpenSearch DSL Full-Text Search
        List<Document> osRes = collection.find(openSearch(OpenSearchQuery.match("title", "Machine Learning"))).toList();
        assertThat(osRes).hasSize(1);
        assertThat(osRes.get(0).getId(String.class)).isEqualTo("v-2");

        // 3. OpenSearch DSL Term Query
        List<Document> termRes = collection.find(openSearch(OpenSearchQuery.term("category", "engineering"))).toList();
        assertThat(termRes).hasSize(1);
        assertThat(termRes.get(0).getId(String.class)).isEqualTo("v-1");
    }

    private Database db() {
        return getDatabase().useKeyspace(KEYSPACE);
    }
}
