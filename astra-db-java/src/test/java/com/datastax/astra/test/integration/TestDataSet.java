package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.ObjectId;
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
public interface TestDataSet {

    String NAMESPACE_NS1       = "ns1";
    String DEFAULT_NAMESPACE   = "default_keyspace";
    String DATABASE_NAME       = "astra_db_client";
    String COLLECTION_SIMPLE   = "collection_simple";
    String COLLECTION_OBJECTID = "collection_objectid";
    String COLLECTION_UUID     = "collection_uuid";
    String COLLECTION_UUID_V6  = "collection_uuidv6";
    String COLLECTION_UUID_V7  = "collection_uuidv7";
    String COLLECTION_VECTOR   = "collection_vector";
    String COLLECTION_DENY     = "collection_deny";
    String COLLECTION_ALLOW    = "collection_allow";

    String ASTRA_DB_APPLICATION_TOKEN      = "ASTRA_DB_APPLICATION_TOKEN";
    String ASTRA_DB_APPLICATION_TOKEN_DEV  = "ASTRA_DB_APPLICATION_TOKEN_DEV";
    String ASTRA_DB_APPLICATION_TOKEN_TEST = "ASTRA_DB_APPLICATION_TOKEN_TEST";

    Document COMPLETE_DOCUMENT = new Document().id("1")
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

    /**
     * Bean to be used for the test suite
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Product<ID> {
        @JsonProperty("_id")
        protected ID     id;
        protected String name;
        protected Double price;
        protected UUID code;
    }

    @NoArgsConstructor
    class ProductString extends Product<String> {
        public ProductString(String id, String name, Double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }

    @NoArgsConstructor
    class ProductObjectId extends Product<ObjectId> {}

    @NoArgsConstructor
    class ProductObjectUUID extends Product<UUID> {}

    /** Create Data Api client for the given environment */
    default DataAPIClient getDataApiClient(AstraEnvironment env) {
        switch (env) {
            case DEV: return DataAPIClients
               .createForAstraDev(Utils.readEnvVariable(ASTRA_DB_APPLICATION_TOKEN_DEV)
               .orElseThrow(() -> new IllegalStateException(ASTRA_DB_APPLICATION_TOKEN_DEV + " env var is missing")));
            case PROD: return DataAPIClients
               .create(Utils.readEnvVariable(ASTRA_DB_APPLICATION_TOKEN)
               .orElseThrow(() -> new IllegalStateException(ASTRA_DB_APPLICATION_TOKEN + " env var is missing")));
            case TEST: return DataAPIClients
               .createForAstraTest(Utils.readEnvVariable(ASTRA_DB_APPLICATION_TOKEN_TEST)
               .orElseThrow(() -> new IllegalStateException(ASTRA_DB_APPLICATION_TOKEN_TEST + " env var is missing")));
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
    }

    /** Create DB Admin for the given environment */
    default AstraDBAdmin getAstraDBAdmin(AstraEnvironment env) {
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
    default Database initializeDatabase(AstraEnvironment env, CloudProviderType cloud, String region) {
        AstraDBAdmin client = getAstraDBAdmin(env);
        String databaseName = env.name().toLowerCase() + "_"
                + cloud.name().toLowerCase() + "_"
                + region.replaceAll("-", "_");
        DatabaseAdmin databaseAdmin = client.createDatabase(databaseName, cloud, region);
        Database db = databaseAdmin.getDatabase();
        // Delete Databaseds if already exists
        db.listCollectionNames().forEach(db::dropCollection);
        return db;
    }

}
