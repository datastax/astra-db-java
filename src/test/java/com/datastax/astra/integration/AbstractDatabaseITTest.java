package com.datastax.astra.integration;

import com.datastax.astra.TestConstants;
import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.api.ApiResponse;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.client.model.find.SimilarityMetric;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Super Class to run Tests against Data API.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractDatabaseITTest implements TestConstants {

    /**
     * Reference to working DataApiNamespace
     */
    public static Database database;

    /**
     * Initialization of the working Namespace.
     *
     * @return
     *      current Namespace
     */
    public Database getDatabase() {
        if (database == null) {
            AbstractDatabaseITTest.database = initDatabase();
        }
        return database;
    }

    /**
     * Initialization of the DataApiNamespace.
     *
     * @return
     *      the instance of Data ApiNamespace
     */
    protected abstract Database initDatabase();

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
                        .withVectorDimension(14)
                        .withVectorSimilarityMetric(SimilarityMetric.cosine)
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
                        .withIndexingAllow("a", "b", "c")
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
                        .withIndexingDeny("a", "b", "c")
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
                        .withIndexingDeny("a", "b", "c")
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
        Document doc = getDatabase().runCommand(listCollectionNames, Document.class);
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
    }

    @Test
    @Order(9)
    public void shouldErrorDropIfCollectionDoesNotExists() {
        assertThat(getDatabase().collectionExists("invalid")).isFalse();
        Collection<Document> invalid = getDatabase().getCollection("invalid");
        assertThat(invalid).isNotNull();
        assertThatThrownBy(() -> invalid.insertOne(new Document().append("hello", "world")))
                .isInstanceOf(DataApiException.class)
                .hasMessageContaining("COLLECTION_NOT_EXIST");
    }

}
