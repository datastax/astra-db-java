package com.datastax.astra.test.integration;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.DropCollectionOptions;
import com.datastax.astra.client.collections.commands.results.CollectionInsertManyResult;
import com.datastax.astra.client.collections.commands.results.CollectionInsertOneResult;
import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv6;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv7;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.ListTablesOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.test.model.TableEntityGameWithAnnotation;
import com.datastax.astra.test.model.TableEntityGameWithAnnotationAllHints;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes.OBJECT_ID;
import static com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes.UUIDV6;
import static com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes.UUIDV7;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_ALLOW;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_DENY;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_OBJECT_ID;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_SIMPLE;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_UUID;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_UUID_V6;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_UUID_V7;
import static com.datastax.astra.test.model.TestDataset.COLLECTION_VECTOR;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
/**
 * Super Class to run Tests against Data API.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public abstract class AbstractDatabaseTest extends AbstractDataAPITest {

    // ------------------------------------
    // --------- Collections --------------
    // ------------------------------------

    public void cleanupCollections() {
        // Removing a few collections to test mores elements
        getDatabase().listCollectionNames().forEach(c -> {
            System.out.println("Dropping collection ..." + c);
            getDatabase().dropCollection(c);
        });
    }

    @Test
    @Order(1)
    public void shouldCreateCollectionSimple() {
        cleanupCollections();
        // When
        getDatabase().createCollection(COLLECTION_SIMPLE);
        assertThat(getDatabase().collectionExists(COLLECTION_SIMPLE)).isTrue();
        // When
        Collection<Document> collection_simple = getDatabase().getCollection(COLLECTION_SIMPLE);
        assertThat(collection_simple).isNotNull();
        assertThat(collection_simple.getCollectionName()).isEqualTo(COLLECTION_SIMPLE);

        Collection<Document> c1 = getDatabase().createCollection(COLLECTION_SIMPLE, Document.class);
        assertThat(c1).isNotNull();
        assertThat(c1.getCollectionName()).isEqualTo(COLLECTION_SIMPLE);
    }

    @Test
    @Order(2)
    public void shouldCreateCollectionsVector() {
        getDatabase().dropCollection(COLLECTION_VECTOR, new DropCollectionOptions());
        Collection<Document> collectionVector = getDatabase().createCollection(COLLECTION_VECTOR,
                new CollectionDefinition()
                        .disableLexical()
                        .disableRerank()
                        .vector(14, SimilarityMetric.COSINE));

        assertThat(collectionVector).isNotNull();
        assertThat(collectionVector.getCollectionName()).isEqualTo(COLLECTION_VECTOR);

        CollectionDefinition colDefinition = collectionVector.getDefinition();
        assertThat(colDefinition.getVector()).isNotNull();
        assertThat(colDefinition.getVector().getDimension()).isEqualTo(14);
    }

    @Test
    @Order(3)
    public void shouldCreateCollectionsAllows() {
        Collection<Document> collectionAllow = getDatabase().createCollection(COLLECTION_ALLOW,
                new CollectionDefinition().indexingAllow("a", "b", "c"));
        assertThat(collectionAllow).isNotNull();
        CollectionDefinition options = collectionAllow.getDefinition();
        assertThat(options.getIndexing()).isNotNull();
        assertThat(options.getIndexing().getAllow()).isNotNull();
    }

    @Test
    @Order(4)
    public void shouldCreateCollectionsDeny() {
        Collection<Document> collectionDeny = getDatabase().createCollection(COLLECTION_DENY,
                new CollectionDefinition().indexingDeny("a", "b", "c"));
        assertThat(collectionDeny).isNotNull();
        CollectionDefinition options = collectionDeny.getDefinition();
        assertThat(options.getIndexing()).isNotNull();
        assertThat(options.getIndexing().getDeny()).isNotNull();
    }

    @Test
    @Order(5)
    public void shouldListCollections() {
        shouldCreateCollectionSimple();
        assertThat(getDatabase().listCollectionNames()).isNotNull();
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
                new CollectionDefinition().indexingDeny("a", "b", "c"));
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
        DataAPIResponse res = getDatabase().runCommand(
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
        assertThatThrownBy(collection::getDefinition)
                .isInstanceOf(DataAPIException.class)
                .hasMessageContaining("COLLECTION_NOT_EXIST");
        assertThatThrownBy(collection::getDefinition)
                .isInstanceOf(DataAPIException.class)
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
                .isInstanceOf(DataAPIException.class)
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
                .createCollection(COLLECTION_UUID, new CollectionDefinition().defaultId(CollectionDefaultIdTypes.UUID));
        collectionUUID.deleteAll();
        UUID uid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // Insert One
        CollectionInsertOneResult res = collectionUUID.insertOne(new Document().id(uid).append("sample", uid));
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
        CollectionInsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUID.class));
    }

    @Test
    @Order(11)
    public void shouldCollectionWorkWithObjectIds() {
        // When
        Collection<Document> collectionUUID = getDatabase().createCollection(COLLECTION_OBJECT_ID,
                new CollectionDefinition().defaultId(OBJECT_ID));
        collectionUUID.deleteAll();

        // Insert One
        ObjectId id1 = new ObjectId();
        CollectionInsertOneResult res = collectionUUID.insertOne(new Document().id(id1).append("sample", UUID.randomUUID()));
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
        CollectionInsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(ObjectId.class));

        // Use Bean
        ProductObjectId product = new ProductObjectId();
        product.setId(new ObjectId());
        product.setName("name");
        product.setCode(UUID.randomUUID());
        product.setPrice(0d);
        Collection<ProductObjectId> collectionObjectId = getDatabase().createCollection(COLLECTION_OBJECT_ID,
                new CollectionDefinition().defaultId(OBJECT_ID), ProductObjectId.class);
        collectionObjectId.deleteAll();
        collectionObjectId.insertOne(product);
        Optional<ProductObjectId> productObjectId = collectionObjectId.findOne(eq(product.getId()));
        assertThat(productObjectId).isPresent();
    }


    @Test
    @Order(12)
    public void shouldCollectionWorkWithUUIDv6() {
        // When
        Collection<Document> collectionUUID = getDatabase().createCollection(COLLECTION_UUID_V6,
                        new CollectionDefinition().defaultId(UUIDV6));
        collectionUUID.deleteAll();

        // Insert One
        UUIDv6 id1 = new UUIDv6();
        CollectionInsertOneResult res = collectionUUID
                .insertOne(new Document().id(id1).append("sample", UUID.randomUUID()));
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
        CollectionInsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUIDv6.class));
    }

    @Test
    @Order(13)
    public void shouldCollectionWorkWithUUIDv7() {
        cleanupCollections();
        // When
        Collection<Document> collectionUUID = getDatabase()
                .createCollection(COLLECTION_UUID_V7, new CollectionDefinition()
                        .defaultId(UUIDV7));
        collectionUUID.deleteAll();

        // Insert One
        UUIDv7 id1 = new UUIDv7();
        CollectionInsertOneResult res = collectionUUID.insertOne(new Document().id(id1).append("sample", UUID.randomUUID()));
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
        CollectionInsertManyResult resultsList = collectionUUID.insertMany(docs);
        assertThat(resultsList).isNotNull();
        resultsList.getInsertedIds().forEach(id -> assertThat(id).isInstanceOf(UUIDv7.class));
    }

    // === TABLES ===

    @Test
    @Order(14)
    public void shouldCreateTables() {
        cleanupCollections();
        // Definition of the table in fluent style
        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("match_id")
                .addColumnInt("round")
                .addColumnVector("m_vector", new ColumnDefinitionVector().dimension(3).metric(COSINE))
                .addColumn("score", ColumnTypes.INT)
                .addColumn("when",  ColumnTypes.TIMESTAMP)
                .addColumn("winner",  ColumnTypes.TEXT)
                .addColumnSet("fighters", ColumnTypes.UUID)
                .addPartitionBy("match_id")
                .addPartitionSort(ascending("round"));
        assertThat(tableDefinition).isNotNull();

        // Minimal creation
        // One can add options to setup the creation with finer grained:
        CreateTableOptions createTableOptions = new CreateTableOptions()
                .ifNotExists(true)
                .timeout(ofSeconds(5));
        Table<Row> table1 = getDatabase().createTable("game1", tableDefinition, createTableOptions);
        assertThat(table1).isNotNull();
        assertThat(getDatabase().tableExists("game1")).isTrue();

        // Creation with a fully annotated bean
        String tableXName = getDatabase().getTableName(TableEntityGameWithAnnotation.class);
        Table<TableEntityGameWithAnnotation> tableX = getDatabase().createTable(
                TableEntityGameWithAnnotation.class,
                CreateTableOptions.IF_NOT_EXISTS);
        assertThat(getDatabase().tableExists(tableXName)).isTrue();

        // Minimal creation
        String tableYName = getDatabase().getTableName(TableEntityGameWithAnnotationAllHints.class);
        Table<TableEntityGameWithAnnotationAllHints> tableY = getDatabase().createTable(
                TableEntityGameWithAnnotationAllHints.class);
        assertThat(getDatabase().tableExists(tableYName)).isTrue();
    }

    @Test
    @Order(15)
    public void shouldListTables() {
        ListTablesOptions options = new ListTablesOptions().timeout(Duration.ofMillis(1000));
        List<String> tableNames = getDatabase().listTableNames();
        assertThat(tableNames).isNotEmpty();
        assertThat(tableNames).contains("game1");

        assertThat(getDatabase().listTableNames(options)).isNotEmpty();
        List<TableDescriptor> tables = getDatabase().listTables();
        assertThat(getDatabase().listTables(options)).isNotEmpty();
    }


}
