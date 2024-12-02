package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Constants for the test suite
 */
public abstract class AbstractDataAPITest {

    public static String NAMESPACE_NS1       = "ns1";
    public static String DEFAULT_NAMESPACE   = "default_keyspace";
    public static String DATABASE_NAME       = "astra_db_client";
    public static String COLLECTION_SIMPLE   = "collection_simple";
    public static String COLLECTION_OBJECTID = "collection_objectid";
    public static String COLLECTION_UUID     = "collection_uuid";
    public static String COLLECTION_UUID_V6  = "collection_uuidv6";
    public static String COLLECTION_UUID_V7  = "collection_uuidv7";
    public static String COLLECTION_VECTOR   = "collection_vector";
    public static String COLLECTION_DENY     = "collection_deny";
    public static String COLLECTION_ALLOW    = "collection_allow";

    public static String ASTRA_DB_APPLICATION_TOKEN      = "ASTRA_DB_APPLICATION_TOKEN";
    public static String ASTRA_DB_APPLICATION_TOKEN_DEV  = "ASTRA_DB_APPLICATION_TOKEN_DEV";
    public static String ASTRA_DB_APPLICATION_TOKEN_TEST = "ASTRA_DB_APPLICATION_TOKEN_TEST";

    public static Document COMPLETE_DOCUMENT = new Document().id("1")
            .append("metadata_instant", Instant.now())
            .append("metadata_date", new Date())
            .append("metadata_calendar", Calendar.getInstance())
            .append("metadata_int", 1)
            .append("metadata_objectId", new ObjectId())
            .append("metadata_long", 1232123323L)
            .append("metadata_double", 1213.343243d)
            .append("metadata_float", 1.1232434543f)
            .append("metadata_string", "hello")
            .append("metadata_short", Short.valueOf("1"))
            .append("metadata_string_array", new String[]{"a", "b", "c"})
            .append("metadata_int_array", new Integer[]{1, 2, 3})
            .append("metadata_long_array", new Long[]{1L, 2L, 3L})
            .append("metadata_double_array", new Double[]{1d, 2d, 3d})
            .append("metadata_float_array", new Float[]{1f, 2f, 3f})
            .append("metadata_short_array", new Short[]{1, 2, 3})
            .append("metadata_boolean", true)
            .append("metadata_boolean_array", new Boolean[]{true, false, true})
            .append("metadata_uuid", UUID.randomUUID())
            .append("metadata_uuid_array", new UUID[]{UUID.randomUUID(), UUID.randomUUID()})
            .append("metadata_map", Map.of("key1", "value1", "key2", "value2"))
            .append("metadata_list", List.of("value1", "value2"))
            .append("metadata_byte", Byte.valueOf("1"))
            .append("metadata_character", 'c')
            .append("metadata_enum", AstraDBAdmin.FREE_TIER_CLOUD)
            .append("metadata_enum_array", new CloudProviderType[]{AstraDBAdmin.FREE_TIER_CLOUD, CloudProviderType.AWS})
            .append("metadata_object", new ProductString("p1", "name", 10.1));

    protected static DatabaseAdmin databaseAdmin;

    /** Reference to working DataApiNamespace. */
    protected static Database database;

    protected static AstraDBAdmin astraDbAdmin;

    protected Database getDatabase() {
        if (database == null) {
            AbstractCollectionITTest.database =
                    initializeDatabase(getAstraEnvironment(), getCloudProvider(), getRegion());
        }
        return database;
    }

    public DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            AbstractDatabaseAdminITTest.databaseAdmin =
                    initializeDatabase(getAstraEnvironment(), getCloudProvider(), getRegion()).getDatabaseAdmin();
        }
        return databaseAdmin;
    }

    protected AstraDBAdmin getAstraDbAdmin() {
        if (astraDbAdmin == null) {
            astraDbAdmin = getAstraDBAdmin(getAstraEnvironment());
        }
        return astraDbAdmin;
    }

    /**
     * Return the Astra Environment.
     *
     * @return
     *      astra environment
     */
    protected abstract AstraEnvironment getAstraEnvironment();

    /**
     * Cloud Provider
     *
     * @return
     *      astra environment
     */
    protected abstract CloudProviderType getCloudProvider();

    /**
     * Return Value with the Region
     * @return
     */
    protected abstract String getRegion();

    /**
     * Bean to be used for the test suite
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product<ID> {
        @JsonProperty("_id")
        protected ID     id;
        protected String name;
        protected Double price;
        protected UUID code;
    }

    @NoArgsConstructor
    public static class ProductString extends Product<String> {
        public ProductString(String id, String name, Double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    @NoArgsConstructor
    public static class ProductObjectId extends Product<ObjectId> {}

    @NoArgsConstructor
    public static class ProductObjectUUID extends Product<UUID> {}

    /** Create Data Api client for the given environment */
    protected DataAPIClient getDataApiClient(AstraEnvironment env) {
        switch (env) {
            case DEV: return DataAPIClients
               .astraDev(Utils.readEnvVariable(ASTRA_DB_APPLICATION_TOKEN_DEV)
               .orElseThrow(() -> new IllegalStateException(ASTRA_DB_APPLICATION_TOKEN_DEV + " env var is missing")));
            case PROD: return DataAPIClients
               .astra(Utils.readEnvVariable(ASTRA_DB_APPLICATION_TOKEN)
               .orElseThrow(() -> new IllegalStateException(ASTRA_DB_APPLICATION_TOKEN + " env var is missing")));
            case TEST: return DataAPIClients
               .astraTest(Utils.readEnvVariable(ASTRA_DB_APPLICATION_TOKEN_TEST)
               .orElseThrow(() -> new IllegalStateException(ASTRA_DB_APPLICATION_TOKEN_TEST + " env var is missing")));
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
    }

    /** Create DB Admin for the given environment */
    protected AstraDBAdmin getAstraDBAdmin(AstraEnvironment env) {
        return  getDataApiClient(env).getAdmin();
    }

    /**
     * Initializing a Database for this Test if needed.
     *
     * @param env
     *      astra environment
     * @param cloud
     *      astra cloud provider
     * @param region
     *      astra region
     * @return
     *      current database
     */
    protected Database initializeDatabase(AstraEnvironment env, CloudProviderType cloud, String region) {
        AstraDBAdmin client = getAstraDBAdmin(env);
        String databaseName = env.name().toLowerCase() + "_"
                + cloud.name().toLowerCase() + "_"
                + region.replaceAll("-", "_");
        DatabaseAdmin databaseAdmin = client.createDatabase(databaseName, cloud, region);
        Database db = databaseAdmin.getDatabase();
        // Delete Database if already exists
        db.listCollectionNames().forEach(db::dropCollection);
        return db;
    }

}
