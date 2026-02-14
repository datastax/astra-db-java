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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.ReturnDocument;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.Updates;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindCursor;
import com.datastax.astra.client.collections.commands.options.*;
import com.datastax.astra.client.collections.commands.results.CollectionDeleteResult;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.collections.exceptions.TooManyDocumentsToCountException;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.utils.EscapeUtils;
import com.datastax.astra.test.integration.model.ProductString;
import com.datastax.astra.test.integration.utils.TestDataset;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.datastax.astra.test.integration.utils.TestDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract base class for Collection integration tests.
 * Extend this class and add environment-specific annotations.
 * <p>
 * Uses {@code PER_CLASS} lifecycle so that {@code @BeforeAll} can be non-static
 * and access the inherited {@code getDatabase()} helper.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCollectionIT extends AbstractDataAPITest {

    /** Tested collection for documents. */
    protected Collection<Document> collectionSimple;

    /** Tested collection for typed beans with vector. */
    protected Collection<ProductString> collectionVector;

    @BeforeAll
    void setupCollections() {
        dropAllCollections();
        dropAllTables();
        collectionSimple = getDatabase().createCollection(COLLECTION_SIMPLE);
        collectionVector = getDatabase().createCollection(
                COLLECTION_VECTOR,
                COLLECTION_VECTOR_DEF,
                ProductString.class);
        log.info("Initialized collectionSimple='{}' and collectionVector='{}'",
                COLLECTION_SIMPLE, COLLECTION_VECTOR);
    }

    // ========== Collection Metadata ==========

    @Test
    @Order(1)
    void should_getDefinition_returnMetadata() {
        assertThat(collectionSimple.getDefinition()).isNotNull();
        assertThat(collectionSimple.getCollectionName()).isNotNull();
        assertThat(collectionSimple.getDocumentClass()).isNotExactlyInstanceOf(Document.class);
        assertThat(collectionSimple.getKeyspaceName()).isNotNull();
    }

    // ========== insertOne ==========

    @Test
    @Order(2)
    void should_insertOne_document() {
        CollectionInsertOneResult res = collectionSimple
                .insertOne(new Document().append("hello", "world"));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isNotNull();
    }

    @Test
    @Order(3)
    void should_insertOne_typedBean() {
        ProductString product = new ProductString();
        product.setId("ppp");
        product.setName("cool");
        product.setPrice(9.99);
        CollectionInsertOneResult res = collectionVector.insertOne(product);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isEqualTo("ppp");
    }

    @Test
    void should_insertOne_withUUID() {
        collectionSimple.deleteAll();
        UUID uuid = UUID.randomUUID();
        collectionSimple.insertOne(new Document().id(uuid).append("hello", "world"));
        Optional<Document> doc = collectionSimple.findOne(Filters.eq(uuid));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(UUID.class)).isEqualTo(uuid);
    }

    @Test
    @Order(22)
    void should_insertOne_complexDocument() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(TestDataset.COMPLETE_DOCUMENT);

        Document res = collectionSimple.findById("1")
                .orElseThrow(() -> new IllegalStateException("Should have found a document"));

        Instant i = res.getInstant("metadata_instant");
        assertNotNull(i);
        Date d = res.getDate("metadata_date");
        assertNotNull(d);
        Calendar c = res.getCalendar("metadata_calendar");
        assertNotNull(c);
        assertNotNull(res.getInteger("metadata_int"));
        assertNotNull(res.getLong("metadata_long"));
        assertNotNull(res.getDouble("metadata_double"));
        assertNotNull(res.getFloat("metadata_float"));
        assertNotNull(res.getString("metadata_string"));
        assertNotNull(res.getShort("metadata_short"));
        assertNotNull(res.getBoolean("metadata_boolean"));
        assertNotNull(res.getUUID("metadata_uuid"));
        ObjectId oid = res.getObjectId("metadata_objectId");
        assertNotNull(oid);
        assertNotNull(res.getByte("metadata_byte"));
        assertNotNull(res.getCharacter("metadata_character"));
        ProductString p = res.get("metadata_object", ProductString.class);
        assertNotNull(p);
        List<String> l2 = res.getList("metadata_list", String.class);
        assertNotNull(l2);
        Boolean[] ba = res.getArray("metadata_boolean_array", Boolean.class);
        assertNotNull(ba);
    }

    // ========== insertMany ==========

    @Test
    @Order(4)
    void should_insertMany_singlePage() throws TooManyDocumentsToCountException {
        collectionSimple.deleteAll();
        List<Document> docList = generateDocList(10);
        collectionSimple.insertMany(docList);
        assertThat(collectionSimple.countDocuments(100)).isEqualTo(10);
    }

    @Test
    @Order(5)
    void should_insertMany_multiPages() throws TooManyDocumentsToCountException {
        collectionSimple.deleteAll();
        List<Document> docList = generateDocList(25);
        collectionSimple.insertMany(docList);
        assertThat(collectionSimple.countDocuments(100)).isEqualTo(25);
    }

    @Test
    @Order(6)
    void should_insertMany_distributed() throws TooManyDocumentsToCountException {
        collectionSimple.deleteAll();
        List<Document> docList = generateDocList(155);
        collectionSimple.insertMany(docList, new CollectionInsertManyOptions()
                .concurrency(5)
                .chunkSize(20)
                .timeoutOptions(new TimeoutOptions().generalMethodTimeoutMillis(100000)));
        assertThat(collectionSimple.countDocuments(200)).isEqualTo(155);
    }

    // ========== findAll ==========

    @Test
    @Order(7)
    void should_findAll_returnAllDocuments() {
        collectionSimple.deleteAll();
        for (int i = 0; i < 25; i++) {
            collectionSimple.insertOne(Document.create(i).append("indice", i));
        }

        CollectionFindCursor<Document, Document> findIterable = collectionSimple.findAll();
        for (Document document : findIterable) {
            assertThat(document).isNotNull();
        }

        List<Document> documents = collectionSimple.findAll().toList();
        assertThat(documents).hasSize(25);
    }

    @Test
    void should_findAll_iterateTypedBeans() {
        getDatabase().getCollection(COLLECTION_VECTOR).findAll().forEach(System.out::println);
    }

    // ========== findOne ==========

    @Test
    @Order(8)
    void should_findOne_withFilter() {
        collectionVector.deleteAll();
        ProductString product = new ProductString();
        product.setId("1");
        product.setName("cool");
        product.setPrice(9.99);
        collectionVector.insertOne(product);

        Optional<ProductString> doc = collectionVector.findOne(Filters.eq("1"));
        assertThat(doc).isPresent();

        Optional<ProductString> doc2 = collectionVector.findOne(
                Filters.eq("1"),
                new CollectionFindOneOptions().projection(Projection.include("name")));
        assertThat(doc2).isPresent();
    }

    // ========== findById ==========

    @Test
    void should_findById_returnDocument() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("fb1").put("name", "Alice"));

        Optional<Document> doc = collectionSimple.findById("fb1");
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Alice");
    }

    @Test
    void should_findById_returnEmptyWhenMissing() {
        collectionSimple.deleteAll();
        Optional<Document> doc = collectionSimple.findById("nonexistent");
        assertThat(doc).isEmpty();
    }

    // ========== find ==========

    @Test
    @Order(9)
    void should_find_withSkipAndLimit() {
        collectionSimple.deleteAll();
        for (int i = 0; i < 25; i++) {
            collectionSimple.insertOne(Document.create(i).append("indice", i));
        }

        CollectionFindOptions options = new CollectionFindOptions()
                .sort(Sort.ascending("indice"))
                .skip(11)
                .limit(2);
        List<Document> documents = collectionSimple.find(options).toList();
        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).getInteger("indice")).isEqualTo(11);
        assertThat(documents.get(1).getInteger("indice")).isEqualTo(12);
    }

    @Test
    @Order(10)
    void should_find_returnEmptyOnNoMatch() {
        collectionSimple.deleteAll();
        List<Document> okDoc = collectionSimple.findAll().toList();
        assertThat(okDoc).isEmpty();

        Filter filter = Filters.eq("userId", UUID.randomUUID());
        CollectionFindOptions options = new CollectionFindOptions().projection(Projection.include("transactionHash"));
        List<Document> docs = collectionSimple.find(filter, options).toList();
        assertThat(docs).isEmpty();
    }

    @Test
    @Order(11)
    void should_find_withProjectionSlice() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(TestDataset.COMPLETE_DOCUMENT);

        Projection[] ps = Projection.include("metadata_float_array");
        Document doc = collectionSimple.find(null, new CollectionFindOptions()
                        .projection(ps[0], Projection.slice("metadata_string_array", 1, 2)))
                .next();
        String[] strings = doc.getArray("metadata_string_array", String.class);
        assertThat(strings).hasSize(2);
    }

    // ========== find — Projection ==========

    @Test
    void should_find_withProjectionIncludeMultipleFields() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("pi1")
                .put("name", "Alice").put("age", 30).put("city", "Paris").put("secret", "hidden"));

        CollectionFindOptions options = new CollectionFindOptions()
                .projection(Projection.include("name", "city"));
        Optional<Document> doc = collectionSimple.findOne(Filters.eq("pi1"),
                new CollectionFindOneOptions().projection(Projection.include("name", "city")));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Alice");
        assertThat(doc.get().getString("city")).isEqualTo("Paris");
        // Excluded fields should be absent (except _id which is always included)
        assertThat(doc.get().get("age")).isNull();
        assertThat(doc.get().get("secret")).isNull();
    }

    @Test
    void should_find_withProjectionExclude() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("pe1")
                .put("name", "Bob").put("age", 25).put("city", "London").put("secret", "hidden"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("pe1"),
                new CollectionFindOneOptions().projection(Projection.exclude("secret", "age")));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Bob");
        assertThat(doc.get().getString("city")).isEqualTo("London");
        // Excluded fields should be absent
        assertThat(doc.get().get("secret")).isNull();
        assertThat(doc.get().get("age")).isNull();
    }

    @Test
    void should_find_withProjectionOnNestedField() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("pn1")
                .put("profile", Map.of("name", "Charlie", "address", Map.of("city", "Berlin", "zip", "10115")))
                .put("status", "active"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("pn1"),
                new CollectionFindOneOptions().projection(Projection.include("profile.name", "status")));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("status")).isEqualTo("active");
        // profile.name should be included, profile.address should not
        assertThat(doc.get().read("profile.name", String.class)).isEqualTo("Charlie");
        assertThat(doc.get().read("profile.address")).isNull();
    }

    // ========== find — Sort ==========

    @Test
    void should_find_withSortDescending() {
        collectionSimple.deleteAll();
        for (int i = 0; i < 5; i++) {
            collectionSimple.insertOne(Document.create(i).put("rank", i));
        }

        List<Document> docs = collectionSimple.find(new CollectionFindOptions()
                .sort(Sort.descending("rank"))
                .limit(5)).toList();
        assertThat(docs).hasSize(5);
        assertThat(docs.get(0).getInteger("rank")).isEqualTo(4);
        assertThat(docs.get(4).getInteger("rank")).isEqualTo(0);
    }

    @Test
    void should_find_withMultipleSortFields() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document().id("ms1").put("category", "A").put("score", 10),
                new Document().id("ms2").put("category", "B").put("score", 20),
                new Document().id("ms3").put("category", "A").put("score", 30),
                new Document().id("ms4").put("category", "B").put("score", 5)));

        // Sort by category ascending, then by score descending
        List<Document> docs = collectionSimple.find(new CollectionFindOptions()
                .sort(Sort.ascending("category"), Sort.descending("score"))
                .limit(4)).toList();
        assertThat(docs).hasSize(4);
        // A's first (sorted by score desc): A/30, A/10
        assertThat(docs.get(0).getId(String.class)).isEqualTo("ms3");
        assertThat(docs.get(1).getId(String.class)).isEqualTo("ms1");
        // B's next (sorted by score desc): B/20, B/5
        assertThat(docs.get(2).getId(String.class)).isEqualTo("ms2");
        assertThat(docs.get(3).getId(String.class)).isEqualTo("ms4");
    }

    @Test
    void should_findOne_withSortReturnsFirst() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document().id("s1").put("type", "item").put("price", 50),
                new Document().id("s2").put("type", "item").put("price", 10),
                new Document().id("s3").put("type", "item").put("price", 90)));

        // findOne with ascending sort => cheapest item
        Optional<Document> cheapest = collectionSimple.findOne(
                Filters.eq("type", "item"),
                new CollectionFindOneOptions().sort(Sort.ascending("price")));
        assertThat(cheapest).isPresent();
        assertThat(cheapest.get().getId(String.class)).isEqualTo("s2");
        assertThat(cheapest.get().getInteger("price")).isEqualTo(10);

        // findOne with descending sort => most expensive item
        Optional<Document> expensive = collectionSimple.findOne(
                Filters.eq("type", "item"),
                new CollectionFindOneOptions().sort(Sort.descending("price")));
        assertThat(expensive).isPresent();
        assertThat(expensive.get().getId(String.class)).isEqualTo("s3");
        assertThat(expensive.get().getInteger("price")).isEqualTo(90);
    }

    // ========== find — Sort + Projection combined ==========

    @Test
    void should_find_withFilterSortAndProjection() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document().id("c1").put("dept", "eng").put("name", "Alice").put("salary", 90000),
                new Document().id("c2").put("dept", "eng").put("name", "Bob").put("salary", 75000),
                new Document().id("c3").put("dept", "sales").put("name", "Charlie").put("salary", 60000),
                new Document().id("c4").put("dept", "eng").put("name", "Diana").put("salary", 110000)));

        // Filter eng dept, sort by salary desc, project only name and salary
        List<Document> docs = collectionSimple.find(
                Filters.eq("dept", "eng"),
                new CollectionFindOptions()
                        .sort(Sort.descending("salary"))
                        .projection(Projection.include("name", "salary"))
                        .limit(3)).toList();

        assertThat(docs).hasSize(3);
        // Highest salary first
        assertThat(docs.get(0).getString("name")).isEqualTo("Diana");
        assertThat(docs.get(0).getInteger("salary")).isEqualTo(110000);
        assertThat(docs.get(1).getString("name")).isEqualTo("Alice");
        assertThat(docs.get(2).getString("name")).isEqualTo("Bob");
        // dept should be excluded by projection
        assertThat(docs.get(0).get("dept")).isNull();
    }

    @Test
    void should_findOne_withSortAndProjection() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document().id("sp1").put("category", "book").put("title", "Java in Action").put("rating", 4),
                new Document().id("sp2").put("category", "book").put("title", "Clean Code").put("rating", 5),
                new Document().id("sp3").put("category", "book").put("title", "DDIA").put("rating", 5)));

        // Find highest-rated book, but only return title
        Optional<Document> doc = collectionSimple.findOne(
                Filters.eq("category", "book"),
                new CollectionFindOneOptions()
                        .sort(Sort.descending("rating"), Sort.ascending("title"))
                        .projection(Projection.include("title")));
        assertThat(doc).isPresent();
        // Both "Clean Code" and "DDIA" have rating 5, ascending title picks "Clean Code"
        assertThat(doc.get().getString("title")).isEqualTo("Clean Code");
        // category and rating excluded by projection
        assertThat(doc.get().get("category")).isNull();
        assertThat(doc.get().get("rating")).isNull();
    }

    @Test
    void should_find_withSortSkipLimitAndProjection() {
        collectionSimple.deleteAll();
        for (int i = 0; i < 10; i++) {
            collectionSimple.insertOne(Document.create(i)
                    .put("rank", i)
                    .put("label", "item-" + i)
                    .put("extra", "noise"));
        }

        // Sort ascending, skip 3, limit 4, project only rank and label
        List<Document> docs = collectionSimple.find(new CollectionFindOptions()
                .sort(Sort.ascending("rank"))
                .skip(3)
                .limit(4)
                .projection(Projection.include("rank", "label"))).toList();

        assertThat(docs).hasSize(4);
        assertThat(docs.get(0).getInteger("rank")).isEqualTo(3);
        assertThat(docs.get(0).getString("label")).isEqualTo("item-3");
        assertThat(docs.get(3).getInteger("rank")).isEqualTo(6);
        // extra field excluded
        assertThat(docs.get(0).get("extra")).isNull();
    }

    // ========== countDocuments ==========

    @Test
    @Order(12)
    void should_countDocuments_withFilter() throws TooManyDocumentsToCountException {
        final Collection<Document> col = collectionSimple;

        assertThatThrownBy(() -> col.countDocuments(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpperBound");
        assertThatThrownBy(() -> col.countDocuments(2000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UpperBound");

        collectionSimple.deleteAll();
        for (int i = 0; i < 10; i++) {
            collectionSimple.insertOne(new Document().id(i).append("indice", i));
        }
        assertThat(collectionSimple.countDocuments(1000)).isEqualTo(10);

        assertThatThrownBy(() -> collectionSimple.countDocuments(9))
                .isInstanceOf(TooManyDocumentsToCountException.class)
                .hasMessageContaining("upper bound ");

        assertThat(collectionSimple
                .countDocuments(Filters.gt("indice", 3), DataAPIClientOptions.MAX_COUNT))
                .isEqualTo(6);
    }

    // ========== estimatedDocumentCount ==========

    @Test
    @Order(13)
    void should_estimatedDocumentCount_returnEstimate() throws TooManyDocumentsToCountException {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(generateDocList(21));
        assertThat(collectionSimple.countDocuments(1000)).isEqualTo(21);
        collectionSimple.estimatedDocumentCount();
    }

    // ========== updateOne ==========

    @Test
    @Order(14)
    void should_updateOne_returnMatchAndModifyCount() {
        collectionSimple.deleteAll();
        Document doc1 = new Document().id(1)
                .append("name", "val")
                .append("price", 10.1)
                .append("field1", "value1")
                .append("test", 10.1);
        collectionSimple.insertOne(doc1);

        CollectionUpdateResult res = collectionSimple.updateOne(Filters.eq(1), Update.create()
                .set(Document.create().append("name", "doe"))
                .inc("test", 1d)
                .rename("field1", "field2")
                .mul(Map.of("price", 1.1d)));
        assertThat(res.getMatchedCount()).isEqualTo(1);
        assertThat(res.getModifiedCount()).isEqualTo(1);
    }

    @Test
    void should_updateOne_set() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u1").put("name", "Alice").put("age", 30));

        collectionSimple.updateOne(Filters.eq("u1"), Updates.set("name", "Bob"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u1"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Bob");
        assertThat(doc.get().getInteger("age")).isEqualTo(30);
    }

    @Test
    void should_updateOne_unset() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u2").put("name", "Alice").put("temp", "remove_me"));

        collectionSimple.updateOne(Filters.eq("u2"), Updates.unset("temp"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u2"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Alice");
        assertThat(doc.get().get("temp")).isNull();
    }

    @Test
    void should_updateOne_inc() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u3").put("counter", 10));

        collectionSimple.updateOne(Filters.eq("u3"), Updates.inc("counter", 5d));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u3"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getInteger("counter")).isEqualTo(15);
    }

    @Test
    void should_updateOne_incNegative() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u3b").put("counter", 20));

        collectionSimple.updateOne(Filters.eq("u3b"), Updates.inc("counter", -3d));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u3b"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getInteger("counter")).isEqualTo(17);
    }

    @Test
    void should_updateOne_min() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u4").put("score", 80));

        // 50 < 80 => field updated to 50
        collectionSimple.updateOne(Filters.eq("u4"), Updates.min("score", 50d));
        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u4"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("score")).isEqualTo(50d);

        // 90 > 50 => field stays 50
        collectionSimple.updateOne(Filters.eq("u4"), Update.create().min("score", 90d));
        doc = collectionSimple.findOne(Filters.eq("u4"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("score")).isEqualTo(50d);
    }

    @Test
    void should_updateOne_max() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u5").put("highscore", 100));

        // 150 > 100 => field updated to 150
        collectionSimple.updateOne(Filters.eq("u5"), Update.create().max("highscore", 150d));
        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u5"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("highscore")).isEqualTo(150d);

        // 120 < 150 => field stays 150
        collectionSimple.updateOne(Filters.eq("u5"), Update.create().max("highscore", 120d));
        doc = collectionSimple.findOne(Filters.eq("u5"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("highscore")).isEqualTo(150d);
    }

    @Test
    void should_updateOne_mul() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u6").put("price", 10.0).put("quantity", 3.0));

        collectionSimple.updateOne(Filters.eq("u6"), Update.create().mul(Map.of("price", 1.5, "quantity", 2.0)));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u6"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getInteger("price")).isEqualTo(15);
        assertThat(doc.get().getInteger("quantity")).isEqualTo(6);
    }

    @Test
    void should_updateOne_rename() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u7").put("old_name", "value1").put("keep", "value2"));

        collectionSimple.updateOne(Filters.eq("u7"), Updates.rename("old_name", "new_name"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u7"));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("old_name")).isNull();
        assertThat(doc.get().getString("new_name")).isEqualTo("value1");
        assertThat(doc.get().getString("keep")).isEqualTo("value2");
    }

    @Test
    void should_updateOne_push() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u8").put("tags", List.of("java", "python")));

        collectionSimple.updateOne(Filters.eq("u8"), Updates.push("tags", "rust"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u8"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("tags", String.class)).containsExactly("java", "python", "rust");
    }

    @Test
    void should_updateOne_pushCreatesArray() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u8b").put("name", "Alice"));

        collectionSimple.updateOne(Filters.eq("u8b"), Updates.push("tags", "first"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u8b"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("tags", String.class)).containsExactly("first");
    }

    @Test
    void should_updateOne_pushEach() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u9").put("items", List.of("a")));

        collectionSimple.updateOne(Filters.eq("u9"),
                Updates.pushEach("items", List.of("b", "c", "d"), null));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u9"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("items", String.class)).containsExactly("a", "b", "c", "d");
    }

    @Test
    void should_updateOne_pushEachWithPosition() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u9b").put("items", List.of("a", "d")));

        collectionSimple.updateOne(Filters.eq("u9b"),
                Updates.pushEach("items", List.of("b", "c"), 1));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u9b"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("items", String.class)).containsExactly("a", "b", "c", "d");
    }

    @Test
    void should_updateOne_popLast() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u10").put("queue", List.of("first", "middle", "last")));

        collectionSimple.updateOne(Filters.eq("u10"), Updates.pop("queue", 1));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u10"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("queue", String.class)).containsExactly("first", "middle");
    }

    @Test
    void should_updateOne_popFirst() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u10b").put("queue", List.of("first", "middle", "last")));

        collectionSimple.updateOne(Filters.eq("u10b"), Updates.pop("queue", -1));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u10b"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("queue", String.class)).containsExactly("middle", "last");
    }

    @Test
    void should_updateOne_addToSet() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u11").put("tags", List.of("java", "python")));

        // "rust" is new => added
        collectionSimple.updateOne(Filters.eq("u11"), Updates.addToSet("tags", "rust"));
        // "java" already exists => not added
        collectionSimple.updateOne(Filters.eq("u11"), Updates.addToSet("tags", "java"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u11"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getList("tags", String.class)).containsExactly("java", "python", "rust");
    }

    @Test
    void should_updateOne_currentDate() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("u12").put("name", "Alice"));

        collectionSimple.updateOne(Filters.eq("u12"), Updates.updateCurrentDate("lastModified"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u12"));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("lastModified")).isNotNull();
    }

    @Test
    void should_updateOne_setOnInsert() {
        collectionSimple.deleteAll();

        // Upsert: document doesn't exist => $setOnInsert fields are applied
        collectionSimple.updateOne(
                Filters.eq("soi1"),
                Update.create()
                        .set("name", "Alice")
                        .setOnInsert(Map.of("createdBy", "system", "version", 1)),
                new CollectionUpdateOneOptions().upsert(true));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("soi1"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Alice");
        assertThat(doc.get().getString("createdBy")).isEqualTo("system");

        // Update existing: $setOnInsert fields are NOT applied
        collectionSimple.updateOne(
                Filters.eq("soi1"),
                Update.create()
                        .set("name", "Bob")
                        .setOnInsert(Map.of("createdBy", "admin", "version", 2)),
                new CollectionUpdateOneOptions().upsert(true));

        doc = collectionSimple.findOne(Filters.eq("soi1"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Bob");
        // createdBy should remain "system" from the initial insert
        assertThat(doc.get().getString("createdBy")).isEqualTo("system");
    }

    @Test
    void should_updateOne_chainedOperations() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("u13")
                .put("name", "Alice")
                .put("score", 10.0)
                .put("old_field", "remove_me"));

        collectionSimple.updateOne(Filters.eq("u13"), Update.create()
                .set("name", "Bob")
                .inc("score", 5d)
                .rename("old_field", "new_field"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("u13"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("Bob");
        assertThat(doc.get().getInteger("score")).isEqualTo(15);
        assertThat(doc.get().get("old_field")).isNull();
        assertThat(doc.get().getString("new_field")).isEqualTo("remove_me");
    }

    @Test
    void should_updateOne_upsertWhenNoMatch() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document("1")
                .append("isCheckedOut", true)
                .append("numberOfPages", 1)
                .append("color", "blue"));

        Filter filter = Filters.and(
                Filters.eq("isCheckedOut", false),
                Filters.lt("numberOfPages", 10));
        Update update = Updates.set("color", "yellow");
        CollectionUpdateOneOptions options = new CollectionUpdateOneOptions().upsert(true);
        CollectionUpdateResult result = collectionSimple.updateOne(filter, update, options);
        Assertions.assertEquals(0, result.getMatchedCount());
        Assertions.assertEquals(0, result.getModifiedCount());
        Assertions.assertNotNull(result.getUpsertedId());
    }

    // ========== updateMany ==========

    @Test
    void should_updateMany_updateAllMatching() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document().id("um1").put("status", "pending").put("priority", 1),
                new Document().id("um2").put("status", "pending").put("priority", 2),
                new Document().id("um3").put("status", "done").put("priority", 3)));

        CollectionUpdateResult res = collectionSimple.updateMany(
                Filters.eq("status", "pending"),
                Updates.set("status", "processed"));

        assertThat(res.getMatchedCount()).isEqualTo(2);
        assertThat(res.getModifiedCount()).isEqualTo(2);

        // Verify both updated
        Optional<Document> d1 = collectionSimple.findById("um1");
        assertThat(d1).isPresent();
        assertThat(d1.get().getString("status")).isEqualTo("processed");

        Optional<Document> d2 = collectionSimple.findById("um2");
        assertThat(d2).isPresent();
        assertThat(d2.get().getString("status")).isEqualTo("processed");

        // Verify non-matching untouched
        Optional<Document> d3 = collectionSimple.findById("um3");
        assertThat(d3).isPresent();
        assertThat(d3.get().getString("status")).isEqualTo("done");
    }

    @Test
    void should_updateMany_returnZeroWhenNoMatch() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("um4").put("status", "active"));

        CollectionUpdateResult res = collectionSimple.updateMany(
                Filters.eq("status", "nonexistent"),
                Updates.set("status", "updated"));

        assertThat(res.getMatchedCount()).isEqualTo(0);
        assertThat(res.getModifiedCount()).isEqualTo(0);
    }

    // ========== findOneAndUpdate ==========

    @Test
    @Order(15)
    void should_findOneAndUpdate_returnAfter() {
        collectionSimple.deleteOne(Filters.eq(1));
        Document doc1 = new Document().id(1)
                .append("name", "val")
                .append("price", 10.1)
                .append("field1", "value1")
                .append("test", 10.1);
        collectionSimple.insertOne(doc1);

        Optional<Document> doc = collectionSimple.findOneAndUpdate(Filters.eq(1), Update.create()
                        .set(Document.create().append("name", "doe"))
                        .inc("test", 1d)
                        .rename("field1", "field2")
                        .mul(Map.of("price", 1.1d)),
                new CollectionFindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
        assertThat(doc).isPresent();
        assertThat(doc.get().getDouble("test")).isEqualTo(11.1d);
        assertThat(doc.get().getDouble("price")).isEqualTo(11.11d);
        assertThat(doc.get().getString("field2")).isNotNull();
    }

    // ========== findOne — nested and escaping ==========

    @Test
    void should_findOne_filterWithDotInFieldName() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id(1).put("hello.world", "test"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("hello&.world", "test"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("hello.world")).isEqualTo("test");
    }

    @Test
    void should_findOne_filterWithAmpersandInFieldName() {
        collectionSimple.deleteAll();
        String escapedFieldName = EscapeUtils.escapeFieldNames("hello&world");
        collectionSimple.insertOne(new Document().append(escapedFieldName, "test"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq(escapedFieldName, "test"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("hello&world")).isEqualTo("test");
    }

    @Test
    void should_findOne_nestedMap() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("nested-1")
                .put("metadata", Map.of("color", "red", "size", "large")));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("metadata.color", "red"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getId(String.class)).isEqualTo("nested-1");
        assertThat(doc.get().read("metadata.color", String.class)).isEqualTo("red");
        assertThat(doc.get().read("metadata.size", String.class)).isEqualTo("large");
    }

    @Test
    void should_findOne_deeplyNested() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("deep-1")
                .put("level1", Map.of("level2", Map.of("level3", "deep_value"))));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("level1.level2.level3", "deep_value"));
        assertThat(doc).isPresent();
        assertThat(doc.get().read("level1.level2.level3", String.class)).isEqualTo("deep_value");
    }

    @Test
    void should_findOne_nestedWithArray() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("arr-1")
                .put("tags", List.of("java", "python", "rust"))
                .put("scores", Map.of("math", List.of(90, 85, 95))));

        Optional<Document> doc = collectionSimple.findOne(
                Filters.all("tags","java", "python", "rust"));
        assertThat(doc).isPresent();
        assertThat(doc.get().read("tags[0]")).isEqualTo("java");
        assertThat(doc.get().read("tags[2]")).isEqualTo("rust");
        assertThat(doc.get().read("scores.math[0]")).isEqualTo(90);
        assertThat(doc.get().read("scores.math[2]")).isEqualTo(95);
    }

    @Test
    void should_findOne_escapedDotInFieldName() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("dot-1")
                .put("config.v2", "enabled")
                .put("status", "active"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("config&.v2", "enabled"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("config.v2")).isEqualTo("enabled");
        assertThat(doc.get().read("config&.v2", String.class)).isEqualTo("enabled");
    }

    @Test
    void should_findOne_escapedAmpersandInFieldName() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("amp-1")
                .put("Q&A", "yes")
                .put("type", "faq"));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("Q&&A", "yes"));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("Q&A")).isEqualTo("yes");
    }

    @Test
    void should_findOne_mixedNestingAndEscaping() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document()
                .id("mix-1")
                .put("data", Map.of("config.v2", Map.of("enabled", true))));

        Optional<Document> doc = collectionSimple.findOne(Filters.eq("data.config&.v2.enabled", true));
        assertThat(doc).isPresent();
        assertThat(doc.get().read("data.config&.v2.enabled", Boolean.class)).isTrue();
    }

    // ========== distinct ==========

    @Test
    @Order(19)
    void should_distinct_topLevelField() {
        collectionSimple.deleteAll();
        for (int i = 0; i < 25; i++) {
            collectionSimple.insertOne(Document.create(i).append("indice", i % 7));
        }
        Set<Integer> distinctList = collectionSimple.distinct("indice", null, Integer.class);
        assertThat(distinctList).hasSize(7);
    }

    @Test
    @Order(20)
    void should_distinct_iterateValues() {
        int distinct = 22;
        collectionSimple.deleteAll();
        collectionSimple.insertMany(IntStream.range(0, 25)
                .mapToObj(i -> Document.create(i).append("indice", i % distinct))
                .collect(Collectors.toList()));

        List<Integer> values = new ArrayList<>();
        for (Integer integer : collectionSimple.distinct("indice", Integer.class)) {
            values.add(integer);
        }
        assertThat(values).hasSize(distinct);
    }

    @Test
    void should_distinct_nestedField() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document("1").put("metadata", Map.of("animal", "cat", "color", "black")),
                new Document("2").put("metadata", Map.of("animal", "dog", "color", "brown")),
                new Document("3").put("metadata", Map.of("animal", "cat", "color", "white")),
                new Document("4").put("metadata", Map.of("animal", "horse", "color", "brown")),
                new Document("5").put("metadata", Map.of("animal", "dog", "color", "black"))));

        Set<String> animals = collectionSimple.distinct("metadata.animal", String.class);
        assertThat(animals).containsExactlyInAnyOrder("cat", "dog", "horse");
    }

    @Test
    void should_distinct_deeplyNestedField() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document("1").put("a", Map.of("b", Map.of("c", "x"))),
                new Document("2").put("a", Map.of("b", Map.of("c", "y"))),
                new Document("3").put("a", Map.of("b", Map.of("c", "x"))),
                new Document("4").put("a", Map.of("b", Map.of("c", "z")))));

        Set<String> values = collectionSimple.distinct("a.b.c", String.class);
        assertThat(values).containsExactlyInAnyOrder("x", "y", "z");
    }

    @Test
    void should_distinct_withFilter() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document("1").put("category", "A").put("info", Map.of("status", "active")),
                new Document("2").put("category", "B").put("info", Map.of("status", "inactive")),
                new Document("3").put("category", "A").put("info", Map.of("status", "inactive")),
                new Document("4").put("category", "A").put("info", Map.of("status", "active"))));

        Set<String> statuses = collectionSimple.distinct("info.status", Filters.eq("category", "A"), String.class);
        assertThat(statuses).containsExactlyInAnyOrder("active", "inactive");
    }

    @Test
    void should_distinct_escapedDotInFieldName() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document("1").put("config.v2", "on"),
                new Document("2").put("config.v2", "off"),
                new Document("3").put("config.v2", "on")));

        Set<String> values = collectionSimple.distinct("config&.v2", String.class);
        assertThat(values).containsExactlyInAnyOrder("on", "off");
    }

    @Test
    void should_distinct_escapedAmpersandInFieldName() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document("1").put("Q&A", "yes"),
                new Document("2").put("Q&A", "no"),
                new Document("3").put("Q&A", "yes")));

        Set<String> values = collectionSimple.distinct("Q&&A", String.class);
        assertThat(values).containsExactlyInAnyOrder("yes", "no");
    }

    @Test
    void should_distinct_nestedAnimals() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document("1").append("name", "Kittie").append("metadata", Map.of("animal", "cat", "color", "black")),
                new Document("2").append("name", "Lassie").append("metadata", Map.of("animal", "dog", "breed", "Rough Collie")),
                new Document("3").append("name", "Dolly").append("metadata", Map.of("animal", "sheep", "breed", "Finn-Dorset")),
                new Document("4").append("name", "Marjan").append("metadata", Map.of("animal", "lion", "location", "Kabul Zoo")),
                new Document("5").append("name", "Clever Hans").append("metadata", Map.of("animal", "horse", "fame", "math abilities")),
                new Document("6").append("name", "Paul").append("metadata", Map.of("animal", "octopus", "fame", "World Cup predictions")),
                new Document("7").append("name", "Hachiko").append("metadata", Map.of("animal", "dog", "breed", "Akita")),
                new Document("8").append("name", "Balto").append("metadata", Map.of("animal", "dog", "breed", "Siberian Husky")),
                new Document("9").append("name", "Babe").append("metadata", Map.of("animal", "pig", "fame", "movie star")),
                new Document("10").append("name", "Togo").append("metadata", Map.of("animal", "dog", "breed", "Siberian Husky"))));

        Set<String> animals = collectionSimple.distinct("metadata.animal", String.class);
        assertThat(animals).containsExactlyInAnyOrder("cat", "dog", "sheep", "lion", "horse", "octopus", "pig");
    }

    @Test
    void should_distinct_withEscapedFields() {
        collectionSimple.deleteAll();
        Document doc = new Document();
        doc.append("top_string", "value1");
        doc.append("sub.string", "string");
        doc.append(new String[] {"sub", "split"}, true);
        doc.append("sub2.field20&.field21.p2", "hello2");
        doc.append("sub2.lvl2&&lvl3", "value2");
        doc.append("sub.list", List.of("alpha", "beta", "gamma"));
        collectionSimple.insertOne(doc);

        Document doc2 = new Document();
        doc.append("sub2.field20&.field21.p2", "hello1");
        doc.append("sub.list", List.of("toto", "titi", "tata"));
        collectionSimple.insertOne(doc);

        Set<String> values = collectionSimple.distinct("sub2.field20&.field21.p2", String.class);
        Assertions.assertTrue(values.contains("hello1") && values.contains("hello2"));

        Set<String> firstItems = collectionSimple.distinct("sub.list[0]", String.class);
        Assertions.assertTrue(firstItems.contains("toto") && firstItems.contains("alpha"));
    }

    // ========== replaceOne ==========

    @Test
    @Order(16)
    void should_replaceOne_returnAfter() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id(1).append("hello", "world"));
        collectionSimple.insertOne(new Document().id(2).append("bonjour", "monde"));
        CollectionUpdateResult u1 = collectionSimple.replaceOne(
                Filters.eq(1), new Document().append("hello", "welt"));
        assertThat(u1.getMatchedCount()).isEqualTo(1);
        assertThat(u1.getModifiedCount()).isEqualTo(1);
        assertThat(u1.getDocument().get("hello")).isEqualTo("welt");
    }

    @Test
    @Order(16)
    void should_replaceOne_returnBefore() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id(1).append("hello", "world"));
        collectionSimple.insertOne(new Document().id(2).append("bonjour", "monde"));
        CollectionUpdateResult u1 = collectionSimple.replaceOne(
                Filters.eq(1),
                new Document().append("hello", "welt"),
                new CollectionReplaceOneOptions().returnDocument(ReturnDocument.BEFORE));
        assertThat(u1.getMatchedCount()).isEqualTo(1);
        assertThat(u1.getModifiedCount()).isEqualTo(1);
        assertThat(u1.getDocument().get("hello")).isEqualTo("world");

        CollectionUpdateResult u2 = collectionSimple
                .replaceOne(Filters.eq(3), new Document().id(3).append("hello", "world2"));
        assertThat(u2.getMatchedCount()).isZero();
        assertThat(u2.getModifiedCount()).isZero();
        assertThat(u2.getUpsertedId()).isNull();
    }

    @Test
    void should_replaceOne_withSortAndFilter() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(BOOK_HIDDEN_SHADOW.id(1));
        collectionSimple.insertOne(BOOK_ECHOES_IRON_SKY.id(2));
        collectionSimple.insertOne(BOOK_LAST_CARTOGRAPHER.id(3));

        collectionSimple
                .find(Filters.eq("metadata.language", "French"))
                .forEach(book -> log.info("Book found: {}", book.getString("title")));
    }

    // ========== findOneAndReplace ==========

    @Test
    @Order(17)
    void should_findOneAndReplace_returnUpdatedDoc() {
        Collection<Document> col = collectionSimple;
        col.deleteAll();
        col.insertOne(new Document().id(1).append("hello", "world"));
        col.insertOne(new Document().id(2).append("bonjour", "monde"));

        Optional<Document> opt1 = col.findOneAndReplace(Filters.eq(1), new Document().id(1).append("hello", "world2"));
        assertThat(opt1).isPresent();
        assertThat(opt1.get().getDocumentMap()).containsEntry("hello", "world2");

        Optional<Document> opt2 = collectionSimple
                .findOneAndReplace(Filters.eq(3), new Document().id(3).append("hello", "world2"),
                        new CollectionFindOneAndReplaceOptions().upsert(false));
        assertThat(opt2).isEmpty();
    }

    // ========== deleteOne ==========

    @Test
    @Order(18)
    void should_deleteOne_removeMatchingDocument() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(IntStream.range(0, 3)
                .mapToObj(i -> Document.create(i).append("indice", i).append("test", "test"))
                .collect(Collectors.toList()));

        collectionSimple.deleteOne(Filters.eq("indice", 1));
        Map<Integer, Document> results = collectionSimple
                .findAll()
                .stream().collect(Collectors.toMap(doc -> doc.getId(Integer.class), Function.identity()));
        assertThat(results)
                .hasSize(2)
                .containsKey(0)
                .containsKey(2)
                .doesNotContainKey(1);

        // Delete one with sort
        collectionSimple.deleteAll();
        collectionSimple.insertMany(IntStream.range(0, 3)
                .mapToObj(i -> Document.create(i).append("indice", i).append("test", "test"))
                .collect(Collectors.toList()));
        collectionSimple.deleteOne(
                Filters.eq("test", "test"),
                new CollectionDeleteOneOptions().sort(Sort.descending("indice")));
        results = collectionSimple
                .findAll()
                .stream().collect(Collectors.toMap(doc -> doc.getId(Integer.class), Function.identity()));
        assertThat(results).hasSize(2)
                .containsKey(0)
                .containsKey(1)
                .doesNotContainKey(2);
    }

    // ========== deleteMany ==========

    @Test
    void should_deleteMany_removeAllMatching() {
        collectionSimple.deleteAll();
        collectionSimple.insertMany(List.of(
                new Document().id("dm1").put("status", "expired").put("name", "a"),
                new Document().id("dm2").put("status", "expired").put("name", "b"),
                new Document().id("dm3").put("status", "active").put("name", "c")));

        CollectionDeleteResult res = collectionSimple.deleteMany(Filters.eq("status", "expired"));
        assertThat(res.getDeletedCount()).isEqualTo(2);

        List<Document> remaining = collectionSimple.findAll().toList();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId(String.class)).isEqualTo("dm3");
    }

    @Test
    void should_deleteMany_returnZeroWhenNoMatch() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("dm4").put("status", "active"));

        CollectionDeleteResult res = collectionSimple.deleteMany(Filters.eq("status", "nonexistent"));
        assertThat(res.getDeletedCount()).isEqualTo(0);

        // Original document untouched
        assertThat(collectionSimple.findById("dm4")).isPresent();
    }

    // ========== findOneAndDelete ==========

    @Test
    void should_findOneAndDelete_returnDeletedDocument() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("fad1").put("name", "Alice").put("role", "admin"));
        collectionSimple.insertOne(new Document().id("fad2").put("name", "Bob").put("role", "user"));

        Optional<Document> deleted = collectionSimple.findOneAndDelete(Filters.eq("name", "Alice"));
        assertThat(deleted).isPresent();
        assertThat(deleted.get().getString("name")).isEqualTo("Alice");
        assertThat(deleted.get().getString("role")).isEqualTo("admin");

        // Verify it's gone
        assertThat(collectionSimple.findById("fad1")).isEmpty();
        // Other document untouched
        assertThat(collectionSimple.findById("fad2")).isPresent();
    }

    @Test
    void should_findOneAndDelete_returnEmptyWhenNoMatch() {
        collectionSimple.deleteAll();
        collectionSimple.insertOne(new Document().id("fad3").put("name", "Charlie"));

        Optional<Document> deleted = collectionSimple.findOneAndDelete(Filters.eq("name", "nonexistent"));
        assertThat(deleted).isEmpty();

        // Original untouched
        assertThat(collectionSimple.findById("fad3")).isPresent();
    }

    // ========== Typed Collections ==========

    @Test
    @Order(21)
    void should_insertOne_findById_typedBean() {
        ProductString p1 = new ProductString();
        p1.setId("p1");
        p1.setName("p1");
        p1.setPrice(10.1);
        CollectionInsertOneResult res = collectionVector.insertOne(p1);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isEqualTo("p1");

        Optional<ProductString> optRes = collectionVector.findById("p1");
        assertThat(optRes).isPresent();
        assertThat(optRes.get().getId()).isEqualTo("p1");
    }

    // ========== runCommand ==========

    @Test
    @Order(23)
    void should_runCommand_rawCommand() {
        collectionSimple.deleteAll();

        DataAPIResponse res = collectionSimple.runCommand(Command
                .create("insertOne")
                .withDocument(new Document().id(1).append("name", "hello")));
        assertThat(res).isNotNull();
        assertThat(res.getStatus().getInsertedIds().get(0)).isEqualTo(1);

        Command findOne = Command.create("findOne").withFilter(Filters.eq(1));
        res = collectionSimple.runCommand(findOne);
        assertThat(res).isNotNull();
        assertThat(res.getData()).isNotNull();
        assertThat(res.getData().getDocument()).isNotNull();
        Document doc = res.getData().getDocument();
        assertThat(doc.getString("name")).isEqualTo("hello");

        ProductString p1 = collectionSimple.runCommand(findOne, ProductString.class);
        assertThat(p1).isNotNull();
        assertThat(p1.getName()).isEqualTo("hello");
    }

    // ========== Utilities ==========

    protected List<Document> generateDocList(int size) {
        return IntStream.range(0, size)
                .mapToObj(idx -> Document.create(idx).append("indice", idx))
                .collect(Collectors.toList());
    }

}
