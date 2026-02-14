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
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv6;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv7;
import com.datastax.astra.client.core.query.Filters;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.test.integration.utils.TestDataset.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract integration tests for collections with different default ID types.
 * <p>
 * Tests cover UUID, ObjectId, UUIDv6, and UUIDv7 as default ID types,
 * verifying insertOne, insertMany, findOne, and ID type coercion.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractCollectionDefaultIdIT extends AbstractDataAPITest {

    protected Collection<Document> collectionUuid;
    protected Collection<Document> collectionObjectId;
    protected Collection<Document> collectionUuidV6;
    protected Collection<Document> collectionUuidV7;

    @BeforeAll
    void setupDefaultIdCollections() {
        dropAllCollections();
        dropAllTables();
        collectionUuid = getDatabase().createCollection(COLLECTION_UUID,
                new CollectionDefinition().defaultId(CollectionDefaultIdTypes.UUID));
        collectionObjectId = getDatabase().createCollection(COLLECTION_OBJECT_ID,
                new CollectionDefinition().defaultId(CollectionDefaultIdTypes.OBJECT_ID));
        collectionUuidV6 = getDatabase().createCollection(COLLECTION_UUID_V6,
                new CollectionDefinition().defaultId(CollectionDefaultIdTypes.UUIDV6));
        collectionUuidV7 = getDatabase().createCollection(COLLECTION_UUID_V7,
                new CollectionDefinition().defaultId(CollectionDefaultIdTypes.UUIDV7));
        log.info("Initialized default-id collections: UUID, ObjectId, UUIDv6, UUIDv7");
    }

    // ========== UUID ==========

    @Test
    @Order(1)
    void should_insertOne_withUuidDefaultId() {
        collectionUuid.deleteAll();
        UUID uid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        CollectionInsertOneResult res = collectionUuid.insertOne(
                new Document().id(uid).put("sample", uid));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUID.class);

        Optional<Document> doc = collectionUuid.findOne(Filters.eq(uid));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isEqualTo(uid);
        assertThat(doc.get().getId(UUID.class)).isInstanceOf(UUID.class);
    }

    @Test
    @Order(2)
    void should_insertMany_withUuidDefaultId_generateIds() {
        collectionUuid.deleteAll();
        List<Document> docs = List.of(
                new Document().put("idx", 1),
                new Document().put("idx", 2),
                new Document().put("idx", 3));

        CollectionInsertManyResult result = collectionUuid.insertMany(docs);
        assertThat(result).isNotNull();
        assertThat(result.getInsertedIds()).hasSize(3);
        result.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUID.class));
    }

    @Test
    @Order(3)
    void should_insertOne_withUuidDefaultId_autoGenerate() {
        collectionUuid.deleteAll();
        // Insert without explicit id — server should auto-generate a UUID
        CollectionInsertOneResult res = collectionUuid.insertOne(
                new Document().put("name", "auto-uuid"));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUID.class);

        Optional<Document> doc = collectionUuid.findOne(Filters.eq(res.getInsertedId()));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("auto-uuid");
    }

    // ========== ObjectId ==========

    @Test
    @Order(4)
    void should_insertOne_withObjectIdDefaultId() {
        collectionObjectId.deleteAll();
        ObjectId id1 = new ObjectId();

        CollectionInsertOneResult res = collectionObjectId.insertOne(
                new Document().id(id1).put("sample", UUID.randomUUID()));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(ObjectId.class);

        Optional<Document> doc = collectionObjectId.findOne(Filters.eq(id1));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isNotNull();
        assertThat(doc.get().getId(ObjectId.class)).isInstanceOf(ObjectId.class);
    }

    @Test
    @Order(5)
    void should_insertMany_withObjectIdDefaultId_generateIds() {
        collectionObjectId.deleteAll();
        List<Document> docs = List.of(
                new Document().put("idx", 1),
                new Document().put("idx", 2),
                new Document().put("idx", 3));

        CollectionInsertManyResult result = collectionObjectId.insertMany(docs);
        assertThat(result).isNotNull();
        assertThat(result.getInsertedIds()).hasSize(3);
        result.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(ObjectId.class));
    }

    @Test
    @Order(6)
    void should_insertOne_withObjectIdDefaultId_autoGenerate() {
        collectionObjectId.deleteAll();
        CollectionInsertOneResult res = collectionObjectId.insertOne(
                new Document().put("name", "auto-objectid"));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(ObjectId.class);

        Optional<Document> doc = collectionObjectId.findOne(Filters.eq(res.getInsertedId()));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("auto-objectid");
    }

    // ========== UUIDv6 ==========

    @Test
    @Order(7)
    void should_insertOne_withUuidV6DefaultId() {
        collectionUuidV6.deleteAll();
        UUIDv6 id1 = new UUIDv6();

        CollectionInsertOneResult res = collectionUuidV6.insertOne(
                new Document().id(id1).put("sample", UUID.randomUUID()));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUIDv6.class);

        Optional<Document> doc = collectionUuidV6.findOne(Filters.eq(id1));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isNotNull();
        assertThat(doc.get().getId(UUIDv6.class)).isInstanceOf(UUIDv6.class);
    }

    @Test
    @Order(8)
    void should_insertMany_withUuidV6DefaultId_generateIds() {
        collectionUuidV6.deleteAll();
        List<Document> docs = List.of(
                new Document().put("idx", 1),
                new Document().put("idx", 2),
                new Document().put("idx", 3));

        CollectionInsertManyResult result = collectionUuidV6.insertMany(docs);
        assertThat(result).isNotNull();
        assertThat(result.getInsertedIds()).hasSize(3);
        result.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUIDv6.class));
    }

    @Test
    @Order(9)
    void should_insertOne_withUuidV6DefaultId_autoGenerate() {
        collectionUuidV6.deleteAll();
        CollectionInsertOneResult res = collectionUuidV6.insertOne(
                new Document().put("name", "auto-uuidv6"));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUIDv6.class);

        Optional<Document> doc = collectionUuidV6.findOne(Filters.eq(res.getInsertedId()));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("auto-uuidv6");
    }

    // ========== UUIDv7 ==========

    @Test
    @Order(10)
    void should_insertOne_withUuidV7DefaultId() {
        collectionUuidV7.deleteAll();
        UUIDv7 id1 = new UUIDv7();

        CollectionInsertOneResult res = collectionUuidV7.insertOne(
                new Document().id(id1).put("sample", UUID.randomUUID()));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUIDv7.class);

        Optional<Document> doc = collectionUuidV7.findOne(Filters.eq(id1));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isNotNull();
        assertThat(doc.get().getId(UUIDv7.class)).isInstanceOf(UUIDv7.class);
    }

    @Test
    @Order(11)
    void should_insertMany_withUuidV7DefaultId_generateIds() {
        collectionUuidV7.deleteAll();
        List<Document> docs = List.of(
                new Document().put("idx", 1),
                new Document().put("idx", 2),
                new Document().put("idx", 3));

        CollectionInsertManyResult result = collectionUuidV7.insertMany(docs);
        assertThat(result).isNotNull();
        assertThat(result.getInsertedIds()).hasSize(3);
        result.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUIDv7.class));
    }

    @Test
    @Order(12)
    void should_insertOne_withUuidV7DefaultId_autoGenerate() {
        collectionUuidV7.deleteAll();
        CollectionInsertOneResult res = collectionUuidV7.insertOne(
                new Document().put("name", "auto-uuidv7"));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUIDv7.class);

        Optional<Document> doc = collectionUuidV7.findOne(Filters.eq(res.getInsertedId()));
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("name")).isEqualTo("auto-uuidv7");
    }

    // ========== Cross-type: find by generated ID ==========

    @Test
    @Order(13)
    void should_findById_withUuidDefaultId() {
        collectionUuid.deleteAll();
        CollectionInsertOneResult res = collectionUuid.insertOne(
                new Document().put("color", "blue"));
        UUID generatedId = (UUID) res.getInsertedId();

        Optional<Document> doc = collectionUuid.findById(generatedId);
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("color")).isEqualTo("blue");
    }

    @Test
    @Order(14)
    void should_findById_withObjectIdDefaultId() {
        collectionObjectId.deleteAll();
        CollectionInsertOneResult res = collectionObjectId.insertOne(
                new Document().put("color", "red"));
        ObjectId generatedId = (ObjectId) res.getInsertedId();

        Optional<Document> doc = collectionObjectId.findById(generatedId);
        assertThat(doc).isPresent();
        assertThat(doc.get().getString("color")).isEqualTo("red");
    }

    @Test
    @Order(15)
    void should_deleteOne_withUuidDefaultId() {
        collectionUuid.deleteAll();
        CollectionInsertOneResult res = collectionUuid.insertOne(
                new Document().put("temp", "data"));
        UUID generatedId = (UUID) res.getInsertedId();

        collectionUuid.deleteOne(Filters.eq(generatedId));
        assertThat(collectionUuid.findById(generatedId)).isEmpty();
    }

    @Test
    @Order(16)
    void should_deleteOne_withObjectIdDefaultId() {
        collectionObjectId.deleteAll();
        CollectionInsertOneResult res = collectionObjectId.insertOne(
                new Document().put("temp", "data"));
        ObjectId generatedId = (ObjectId) res.getInsertedId();

        collectionObjectId.deleteOne(Filters.eq(generatedId));
        assertThat(collectionObjectId.findById(generatedId)).isEmpty();
    }
}
