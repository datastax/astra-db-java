package com.datastax.astra.test.integration.database;

import com.datastax.astra.test.TestConstants;
import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.ApiResponse;
import com.datastax.astra.internal.LoggingCommandObserver;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Super Class to run Tests against Data API.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
abstract class AbstractDatabaseTest implements TestConstants {

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

            database.dropCollection(COLLECTION_SIMPLE);
            database.dropCollection(COLLECTION_VECTOR);

            database.dropCollection(COLLECTION_ALLOW);
            database.dropCollection(COLLECTION_DENY);

            database.dropCollection(COLLECTION_UUID);
            database.dropCollection(COLLECTION_UUID_V6);
            database.dropCollection(COLLECTION_UUID_V7);
            database.dropCollection(COLLECTION_OBJECTID);

        }
        return database;
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
        UUID databaseId =  client.createDatabase(DATABASE_NAME, cloud, region);
        log.info("Working with api Endpoint '{}'", ApiLocator.getApiJsonEndpoint(env, databaseId.toString(), region));
        Database db =  client.getDatabase(databaseId);
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
                return DataAPIClients.astraDev(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_DEV'")))
                        .getAdmin();
            case PROD:
                return DataAPIClients.astra(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN'")))
                        .getAdmin();
            case TEST:
                return DataAPIClients.astraTest(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_TEST")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_TEST'")))
                        .getAdmin();
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
    }


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
