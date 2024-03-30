package com.datastax.astra.test.integration.admin;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class AstraDBAdminITTest {

/*


    // -------------------------------------
    // ----------- CONNECTION --------------
    // -------------------------------------

    @Test
    @Order(2)
    public void shouldConnectToDatabase() {
        if (databaseId == null) shouldCreateDatabase();
        // Given
        Assertions.assertNotNull(databaseId);
        Assertions.assertTrue(astraDbAdmin.isDatabaseExists(TEST_DBNAME));
        Assertions.assertNotNull(astraDbAdmin.getDataApiClient(databaseId));
        // When
        astraDB = astraDbAdmin.getDatabase(databaseId);
        Assertions.assertNotNull(astraDbAdmin);
        // When
        astraDB = astraDbAdmin.getDatabase(TEST_DBNAME);
        Assertions.assertNotNull(astraDB);
    }

    @Test
    @Order(3)
    public void shouldConnectToDatabaseWithEndpoint() {
        if (databaseId == null) shouldCreateDatabase();
        // Given
        Assertions.assertNotNull(astraDB);
        // When
        AstraDB astraDbClient2 = new AstraDB(astraDbAdmin.getToken(), astraDB.getApiEndpoint());
        // Then
        Assertions.assertNotNull(astraDbClient2);
        Assertions.assertNotNull(astraDbClient2.findAllCollections());
    }

    @Test
    @Order(4)
    public void shouldConnectToDatabaseWithEndpointAndKeyspace() {
        // When initializing with a keyspace
        AstraDB astraDbClient3 = new AstraDB(astraDbAdmin.getToken(), astraDB.getApiEndpoint(), AstraDBAdmin.DEFAULT_KEYSPACE);
        // Then
        Assertions.assertNotNull(astraDbClient3);
        Assertions.assertNotNull(astraDbClient3.findAllCollections());
        // When initializing with an INVALID keyspace
        Assertions.assertThrows(DataApiNamespaceNotFoundException.class, () -> new AstraDB(astraDbAdmin.getToken(), astraDB.getApiEndpoint(), "invalid_keyspace"));
    }

    // ------------------------------------------
    // ----------- WORKING WITH DB --------------
    // ------------------------------------------

    @Test
    @Order(5)
    public void shouldFindDatabaseById() {
        if (databaseId == null) shouldCreateDatabase();
        // When
        Optional<Database> opt = astraDbAdmin.getDatabaseInformations(databaseId);
        // Then
        Assertions.assertNotNull(opt);
        Assertions.assertTrue(opt.isPresent());
        Assertions.assertEquals(TEST_DBNAME, opt.get().getInfo().getName());
    }

    @Test
    @Order(6)
    public void shouldFindDatabaseByName() {
        if (databaseId == null) shouldCreateDatabase();
        // Given
        Assertions.assertTrue(astraDbAdmin.isDatabaseExists(TEST_DBNAME));
        // When
        Stream<Database> dbs = astraDbAdmin.getDatabaseInformations(TEST_DBNAME);
        // Then
        Assertions.assertNotNull(dbs);
        dbs.findFirst().ifPresent(db -> {
            Assertions.assertEquals(TEST_DBNAME, db.getInfo().getName());
            Assertions.assertEquals(databaseId.toString(), db.getId());
        });
    }

    @Test
    @Order(7)
    @DisplayName("04. Find all databases")
    public void shouldFindAllDatabases() {
        // Given
        Assertions.assertTrue(astraDbAdmin.isDatabaseExists(TEST_DBNAME));
        // When
        Assertions.assertTrue(astraDbAdmin
                .listDatabases()
                .anyMatch(db -> db.getInfo().getName().equals(TEST_DBNAME)));
    }

    @Test
    @Order(8)
    @Disabled("slow")
    public void shouldDeleteDatabaseByName() throws InterruptedException {
        String dbName = "test_delete_db_by_name";
        // Given
        Assertions.assertFalse(astraDbAdmin.isDatabaseExists(dbName));
        // When
        UUID dbId = astraDbAdmin.createDatabase(dbName, targetCloud, targetRegion);
        Assertions.assertTrue(astraDbAdmin.isDatabaseExists(dbName));
        // When
        boolean isDeleted = astraDbAdmin.dropDatabase(dbName);
        // Then
        Thread.sleep(5000);
        Assertions.assertTrue(isDeleted);
    }

    @Test
    @Order(8)
    @Disabled("slow")
    public void shouldDeleteDatabaseById() throws InterruptedException {
        String dbName = "test_delete_db_by_id";
        // Given
        Assertions.assertFalse(astraDbAdmin.isDatabaseExists(dbName));
        // When
        UUID tmpDbId = astraDbAdmin.createDatabase(dbName, targetCloud, targetRegion);
        Assertions.assertTrue(astraDbAdmin.isDatabaseExists(dbName));
        boolean isDeleted = astraDbAdmin.dropDatabase(tmpDbId);
        Thread.sleep(5000);
        Assertions.assertTrue(isDeleted);



    }
    */
}