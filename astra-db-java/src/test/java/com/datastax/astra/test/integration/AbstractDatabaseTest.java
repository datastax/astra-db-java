package com.datastax.astra.test.integration;

import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.InsertManyResult;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.ObjectId;
import com.datastax.astra.client.model.UUIDv6;
import com.datastax.astra.client.model.UUIDv7;
import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.api.ApiResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.astra.client.model.CollectionIdTypes.OBJECT_ID;
import static com.datastax.astra.client.model.CollectionIdTypes.UUIDV6;
import static com.datastax.astra.client.model.CollectionIdTypes.UUIDV7;
import static com.datastax.astra.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Super Class to run Tests against Data API.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public abstract class AbstractDatabaseTest implements TestDataSet {

    /**
     * Reference to working DataApiNamespace
     */
    public static Database database;

    /**
     * Initialization of the DataApiNamespace.
     *
     * @return
     *      the instance of Data ApiNamespace
     */
    protected abstract Database initDatabase();

    /**
     * Initialization of the working Namespace.
     *
     * @return
     *      current Namespace
     */
    public Database getDatabase() {
        if (database == null) {
            AbstractDatabaseTest.database = initDatabase();
        }
        return database;
    }

    // ------------------------------------
    // --------- Collections --------------
    // ------------------------------------

    @Test
    @Order(1)
    public void shouldCreateCollectionSimple() {
        // When
        getDatabase().createCollection(COLLECTION_SIMPLE);
        assertThat(getDatabase().collectionExists(COLLECTION_SIMPLE)).isTrue();
        // When
        Collection<Document> collection_simple = getDatabase().getCollection(COLLECTION_SIMPLE);
        assertThat(collection_simple).isNotNull();
        assertThat(collection_simple.getName()).isEqualTo(COLLECTION_SIMPLE);

        Collection<Document> c1 = getDatabase().createCollection(COLLECTION_SIMPLE, Document.class);
        assertThat(c1).isNotNull();
        assertThat(c1.getName()).isEqualTo(COLLECTION_SIMPLE);
    }

    @Test
    @Order(2)
    public void shouldCreateCollectionsVector() {
        Collection<Document> collectionVector = getDatabase().createCollection(COLLECTION_VECTOR,
                CollectionOptions.builder()
                        .vectorDimension(14)
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .build());
        assertThat(collectionVector).isNotNull();
        assertThat(collectionVector.getName()).isEqualTo(COLLECTION_VECTOR);

        CollectionOptions options = collectionVector.getOptions();
        assertThat(options.getVector()).isNotNull();
        assertThat(options.getVector().getDimension()).isEqualTo(14);
    }

    @Test
    @Order(3)
    public void shouldCreateCollectionsAllows() {
        Collection<Document> collectionAllow = getDatabase().createCollection(COLLECTION_ALLOW,
                CollectionOptions.builder()
                        .indexingAllow("a", "b", "c")
                        .build());
        assertThat(collectionAllow).isNotNull();
        CollectionOptions options = collectionAllow.getOptions();
        assertThat(options.getIndexing()).isNotNull();
        assertThat(options.getIndexing().getAllow()).isNotNull();
    }

    @Test
    @Order(4)
    public void shouldCreateCollectionsDeny() {
        Collection<Document> collectionDeny = getDatabase().createCollection(COLLECTION_DENY,
                CollectionOptions.builder()
                        .indexingDeny("a", "b", "c")
                        .build());
        assertThat(collectionDeny).isNotNull();
        CollectionOptions options = collectionDeny.getOptions();
        assertThat(options.getIndexing()).isNotNull();
        assertThat(options.getIndexing().getDeny()).isNotNull();
    }

    @Test
    @Order(5)
    public void shouldListCollections() {
        shouldCreateCollectionSimple();
        assertThat(getDatabase().listCollectionNames().collect(Collectors.toList())).isNotNull();
    }

    @Test
    @Order(6)
    public void shouldDropCollectionAllow() {
        // Given
        shouldCreateCollectionsAllows();
        assertThat(getDatabase().collectionExists(COLLECTION_ALLOW)).isTrue();
        // When
        getDatabase().dropCollection(COLLECTION_ALLOW);
        // Then
        assertThat(getDatabase().collectionExists(COLLECTION_ALLOW)).isFalse();
    }

    @Test
    @Order(6)
    public void shouldDropCollectionsDeny() {
        // Given
        Collection<Document> collectionDeny = getDatabase().createCollection(COLLECTION_DENY,
                CollectionOptions.builder()
                        .indexingDeny("a", "b", "c")
                        .build());
        assertThat(getDatabase().collectionExists(COLLECTION_DENY)).isTrue();
        // When
        collectionDeny.drop();
        // Then
        assertThat(getDatabase().collectionExists(COLLECTION_DENY)).isFalse();
    }

    @Test
    @Order(7)
    public void shouldRunCommand() {
        // Create From String
        ApiResponse res = getDatabase().runCommand(
               Command.create("createCollection").append("name", "collection_simple"));
        assertThat(res).isNotNull();
        assertThat(res.getStatus().getInteger("ok")).isEqualTo(1);
    }

    @Test
    @Order(8)
    public void shouldRunCommandTyped() {
        // Given
        Command listCollectionNames = Command.create("findCollections");
        Document doc = getDatabase().runCommand(listCollectionNames, null, Document.class);
        assertThat(doc).isNotNull();
        assertThat(doc.getList("collections", String.class)).isNotNull();
    }

    @Test
    @Order(8)
    public void shouldErrorGetIfCollectionDoesNotExists() {
        // Given
        Collection<Document> collection = getDatabase().getCollection("invalid");
        assertThat(collection).isNotNull();
        assertThat(getDatabase().collectionExists("invalid")).isFalse();
        assertThatThrownBy(collection::getOptions)
                .isInstanceOf(DataApiException.class)
                .hasMessageContaining("COLLECTION_NOT_EXIST");
        assertThatThrownBy(collection::getOptions)
                .isInstanceOf(DataApiException.class)
                .hasMessageContaining("COLLECTION_NOT_EXIST")
                .extracting("errorCode")  // Extract the errorCode attribute
                .isEqualTo("COLLECTION_NOT_EXIST");  // Replace EXPECTED_ERROR_CODE with the expected error code
    }

    @Test
    @Order(9)
    public void shouldErrorDropIfCollectionDoesNotExists() {
        assertThat(getDatabase().collectionExists("invalid")).isFalse();
        Collection<Document> invalid = getDatabase().getCollection("invalid");
        assertThat(invalid).isNotNull();
        final Document doc = new Document().append("hello", "world");
        assertThatThrownBy(() -> invalid.insertOne(doc))
                .isInstanceOf(DataApiException.class)
                .hasMessageContaining("COLLECTION_NOT_EXIST");
    }

    /**
     * Bean to be used for the test suite
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Product<ID> {
        @JsonProperty("_id")
        private ID     id;
        private String name;
        private Double price;
        private UUID   code;
    }

    /**
     * Bean to be used for the test suite
     */
    static class ProductObjectId extends Product<ObjectId> {
        public ProductObjectId() { super(); }
    }

    @Test
    @Order(10)
    public void shouldCollectionWorkWithUUIDs() {
        // When
        Collection<Document> collectionUUID = getDatabase()
                .createCollection(COLLECTION_UUID, CollectionOptions.builder()
                .defaultIdType(CollectionIdTypes.UUID)
                .build());
        collectionUUID.deleteAll();
        UUID uid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // Insert One
        InsertOneResult res = collectionUUID.insertOne(new Document().id(uid).append("sample", uid));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUID.class);
        Optional<Document> doc = collectionUUID.findOne(eq(uid));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isEqualTo(uid);
        assertThat(doc.get().getId(UUID.class)).isInstanceOf(UUID.class);

        // Insert Many
        List<Document> docs = List.of(
                new Document().append("idx", 1),
                new Document().append("idx", 2),
                new Document().append("idx", 3));
        InsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUID.class));
    }



    @Test
    @Order(11)
    public void shouldCollectionWorkWithObjectIds() {
        // When
        Collection<Document> collectionUUID = getDatabase()
                .createCollection(COLLECTION_OBJECTID, CollectionOptions.builder()
                        .defaultIdType(OBJECT_ID)
                        .build());
        collectionUUID.deleteAll();

        // Insert One
        ObjectId id1 = new ObjectId();
        InsertOneResult res = collectionUUID.insertOne(new Document().id(id1).append("sample", UUID.randomUUID()));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(ObjectId.class);
        Optional<Document> doc = collectionUUID.findOne(eq(id1));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isNotNull();
        assertThat(doc.get().getId(ObjectId.class)).isInstanceOf(ObjectId.class);

        // Insert Many
        List<Document> docs = List.of(
                new Document().append("idx", 1),
                new Document().append("idx", 2),
                new Document().append("idx", 3));
        InsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(ObjectId.class));

        // Use Bean
        ProductObjectId product = new ProductObjectId();
        product.setId(new ObjectId());
        product.setName("name");
        product.setCode(UUID.randomUUID());
        product.setPrice(0d);
        Collection<ProductObjectId> collectionObjectId = getDatabase()
                .createCollection(COLLECTION_OBJECTID, CollectionOptions.builder()
                        .defaultIdType(OBJECT_ID)
                        .build(), ProductObjectId.class);
        collectionObjectId.deleteAll();
        collectionObjectId.insertOne(product);
        Optional<ProductObjectId> productObjectId = collectionObjectId.findOne(eq(product.getId()));
        assertThat(productObjectId).isPresent();
    }

    @Test
    @Order(12)
    public void shouldCollectionWorkWithUUIDv6() {
        // When
        Collection<Document> collectionUUID = getDatabase()
                .createCollection(COLLECTION_UUID_V6, CollectionOptions.builder()
                        .defaultIdType(UUIDV6)
                        .build());
        collectionUUID.deleteAll();

        // Insert One
        UUIDv6 id1 = new UUIDv6();
        InsertOneResult res = collectionUUID.insertOne(new Document().id(id1).append("sample", UUID.randomUUID()));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUIDv6.class);
        Optional<Document> doc = collectionUUID.findOne(eq(id1));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isNotNull();
        assertThat(doc.get().getId(UUIDv6.class)).isInstanceOf(UUIDv6.class);

        // Insert Many
        List<Document> docs = List.of(
                new Document().append("idx", 1),
                new Document().append("idx", 2),
                new Document().append("idx", 3));
        InsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUIDv6.class));
    }

    @Test
    @Order(13)
    public void shouldCollectionWorkWithUUIDv7() {
        // When
        Collection<Document> collectionUUID = getDatabase()
                .createCollection(COLLECTION_UUID_V7, CollectionOptions.builder()
                        .defaultIdType(UUIDV7)
                        .build());
        collectionUUID.deleteAll();

        // Insert One
        UUIDv7 id1 = new UUIDv7();
        InsertOneResult res = collectionUUID.insertOne(new Document().id(id1).append("sample", UUID.randomUUID()));
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isInstanceOf(UUIDv7.class);
        Optional<Document> doc = collectionUUID.findOne(eq(id1));
        assertThat(doc).isPresent();
        assertThat(doc.get().get("sample", UUID.class)).isNotNull();
        assertThat(doc.get().getId(UUIDv7.class)).isInstanceOf(UUIDv7.class);

        // Insert Many
        List<Document> docs = List.of(
                new Document().append("idx", 1),
                new Document().append("idx", 2),
                new Document().append("idx", 3));
        InsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUIDv7.class));
    }

}
