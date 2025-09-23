package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.options.AstraFindAvailableRegionsOptions;
import com.datastax.astra.client.collections.definition.documents.types.TimeUUID;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.vectorize.SupportModelStatus;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.options.FindEmbeddingProvidersOptions;
import com.datastax.astra.client.databases.commands.options.FindRerankingProvidersOptions;
import com.datastax.astra.client.tables.DataAPIPair;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTableAddColumns;
import com.datastax.astra.client.tables.commands.AlterTypeAddFields;
import com.datastax.astra.client.tables.commands.AlterTypeRenameFields;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.commands.options.DropTypeOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.cursor.TableFindCursor;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldDefinition;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.internal.serdes.tables.DataAPIPairArrayDeserializerToMap;
import com.datastax.astra.internal.serdes.tables.MapToDataApiPairArraySerializer;
import com.datastax.astra.test.model.SampleUdtAddress;
import com.datastax.astra.test.unit.QuickStartTablesLocal;
import com.dtsx.astra.sdk.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.Address;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;
import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.BIGINT;
import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.BLOB;
import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.BOOLEAN;
import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.INT;
import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.TEXT;
import static com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldTypes.TIMESTAMP;

/*
 * Flight DECK for Astra DEV
 *
 * https://flightdeck.dev.cloud-tools.datastax.com/database/f9754177-52e2-4b66-935e-78cfd0be0042
 */
public class TableGAIntegrationTest {

    public static final String ASTRA_DB_TOKEN = System.getenv("ASTRA_DB_APPLICATION_TOKEN");

    public static final String DB_URL_NON_VECTOR =
            "https://f9754177-52e2-4b66-935e-78cfd0be0042-us-west-2.apps.astra-dev.datastax.com";
    public static final String DB_URL_VECTOR =
            "https://b70fa426-5288-4437-aa82-32f9f1a169cc-us-east1.apps.astra.datastax.com";

    private DataAPIClient getAstraDevDataApiClient() {
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA_DEV)
                .rerankAPIKey(ASTRA_DB_TOKEN)
                .embeddingAPIKey(ASTRA_DB_TOKEN)
                .logRequests();
        return new DataAPIClient(ASTRA_DB_TOKEN, options);
    }
    private Database getDatabaseNonVector() {
        return getAstraDevDataApiClient().getDatabase(DB_URL_NON_VECTOR);
    }
    private Database getDatabaseVector() {
        return getAstraDevDataApiClient().getDatabase(DB_URL_VECTOR);
    }
    private Database getLocalDatabase() {
        return DataAPIClients.localDbWithDefaultKeyspace();
    }
    private Database getQuickStartDatabase() {
        return getLocalDatabase("http://localhost:8181", "cassandra", "cassandra", "quickstart_keyspace");
    }

    private Database getLocalDatabase(String url, String username, String password, String keyspace) {
        String authToken = new UsernamePasswordTokenProvider(username, password)
                .getToken();
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.HCD)
                .enableFeatureFlagTables()
                .logRequests();
        DataAPIClient client = new DataAPIClient(authToken,options);
        return client.getDatabase(url, keyspace);
    }

    @Test
    public void should_get_database_different_keyspace() {
        getQuickStartDatabase().listTableNames().stream().forEach(System.out::println);
    }

    /**
     * Collection Name
     */
    @Test
    public void should_backwardCompatible_findEmbeddingProviders() {
        getDatabaseVector().getDatabaseAdmin()
                .findEmbeddingProviders()
                .getEmbeddingProviders()
                .forEach((k,v) -> {
                    v.getModels().forEach((model) -> {
                        System.out.println("  Model: " + model.getName() + " - " + model.getApiModelSupport());
                    });
                });
    }

    @Test
    public void should_findEmbeddingProviders() {
        // No Model Status will return all models
        getLocalDatabase()
         .getDatabaseAdmin()
         .findEmbeddingProviders()
         .getEmbeddingProviders()
         .forEach((k,v) -> {
            System.out.println("Provider: " + k + " - " + v.getDisplayName());
            v.getModels().forEach((model) -> {
                System.out.println("  Model: " + model.getName() + " - " + model.getApiModelSupport());
            });
         });

        // filter on SUPPORTED
        FindEmbeddingProvidersOptions options = new FindEmbeddingProvidersOptions()
                .filterModelStatus(SupportModelStatus.SUPPORTED);
        getLocalDatabase()
                .getDatabaseAdmin()
                .findEmbeddingProviders(options)
                .getEmbeddingProviders()
                .forEach((k,v) -> {
                    System.out.println("Provider: " + k + " - " + v.getDisplayName());
                    v.getModels().forEach((model) -> {
                        System.out.println("  Model: " + model.getName() + " - " + model.getApiModelSupport());
                    });
                });
    }

    /**
     * Collection Name
     */
    @Test
    public void should_backwardCompatible_findRerankingProviders() {
        getLocalDatabase().getDatabaseAdmin()
                .findRerankingProviders(new FindRerankingProvidersOptions()
                        .filterModelStatus(SupportModelStatus.SUPPORTED))
                .getRerankingProviders()
                .forEach((k,v) -> {
                    v.getModels().forEach((model) -> {
                        System.out.println("  Model: " + model.getName() + " - " + model.getApiModelSupport());
                    });
                });
    }

    @Test
    public void should_findAvailableRegions() {
        getDatabaseVector()
                .getAdmin()
                .findAvailableRegions(new AstraFindAvailableRegionsOptions().onlyOrgEnabledRegions(false))
                .forEach(region -> System.out.println(JsonUtils.marshall(region)));
    }

    @Test
    public void shouldCreateTableIndex() {
        Table<Row> table = getLocalDatabase()
                .createTable("demo_table_index", new TableDefinition()
                .addColumnText("email")
                .addColumnText("name")
                .addColumnMap("example_map_column", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                .partitionKey("email"), IF_NOT_EXISTS);


        table.createIndex("idx_name", new TableRegularIndexDefinition()
                        .column("example_map_column", TableIndexMapTypes.KEYS),
                CreateIndexOptions.IF_NOT_EXISTS);
    }

    /**
     * CREATE TABLE killrvideo.video_ratings (
     *     videoid uuid PRIMARY KEY,
     *     rating_counter counter,
     *     rating_total counter
     * );
     */
    @Data
    @EntityTable("video_ratings")
    public static final class VideoRatingTableEntity {

        @Column(name = "videoid", type = TableColumnTypes.UUID)
        UUID videoId;

        @Column(name = "rating_counter", type = TableColumnTypes.COUNTER)
        Integer ratingCounter;

        @Column(name = "rating_total", type = TableColumnTypes.COUNTER)
        Integer ratingTotal;
    }

    /**
     * CREATE TABLE default_keyspace.sample_table (
     *     id timeuuid PRIMARY KEY,
     *     col1 text,
     *     col2 text
     * );
     */
    @Data
    @EntityTable("sample_table")
    public static final class SampleTable {

        @Column(name = "id", type = TableColumnTypes.TIMEUUID)
        TimeUUID videoId;

        @Column(name = "col1", type = TableColumnTypes.TEXT)
        String col1;

        @Column(name = "col2", type = TableColumnTypes.TEXT)
        String col2;
    }

    /**
     * CREATE TABLE killrvideo.video_ratings (
     *     videoid uuid PRIMARY KEY,
     *     rating_counter counter,
     *     rating_total counter
     * );
     */

    @Test
    public void should_read_table_with_timeuuid() {
        Table<SampleTable> table3 = getLocalDatabase().getTable(SampleTable.class);

        // Read
        table3.findAll().forEach(row -> {
            System.out.println("Entity: " + row.getVideoId().readInstant() + " - " + row.col1 + " - " + row.col2);
        });

        // Insert
        SampleTable sample = new SampleTable();
        sample.setVideoId(new TimeUUID());
        sample.setCol1("Sample Column 1");
        sample.setCol2("Sample Column 2");
        table3.insertOne(sample);
    }

    @Test
    public void should_read_table_with_counters() {

        Table<VideoRatingTableEntity> table2 = getLocalDatabase()
                .getTable("video_ratings", VideoRatingTableEntity.class);

//        table2.findAll().forEach(entity -> {
//            System.out.println("Entity: " + entity.videoId + " - " + entity.ratingCounter + " - " + entity.ratingTotal);
//        });

        Optional<VideoRatingTableEntity> optional = table2.findOne(
                eq("videoid", java.util.UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff"))
        );

        // Insert


        if (optional.isPresent()) {
            VideoRatingTableEntity entity = optional.get();
            System.out.println("Entity: " + entity.videoId + " - " + entity.ratingCounter + " - " + entity.ratingTotal);
        } else {
            System.out.println("No entity found for the given filter.");
        }
    }

    @Test
    public void should_create_type() {
        getDatabaseVector().createType("udt_one", new TableUserDefinedTypeDefinition()
                .addFieldUuid("f_uuid")
                .addFieldText("f_text")
                .addFieldAscii("f_ascii")
                .addFieldBigInt("f_bigint")
                .addFieldBlob("f_blob")
                .addFieldBoolean("f_boolean")
                .addFieldTimestamp("f_timestamp")
                .addFieldInt("f_int")
            , CreateTypeOptions.IF_NOT_EXISTS);
    }

    @Test
    public void should_drop_type() {
        getLocalDatabase().dropType("udt_one",DropTypeOptions.IF_EXISTS);
    }

    @Test
    public void should_alter_type_rename_fields() {
        getLocalDatabase().alterType("udt_one",
            new AlterTypeRenameFields().addField("f_bigint", "f_bigint2"));
    }

    @Test
    public void should_alter_type_add_fields() {
        getLocalDatabase().alterType("udt_one",
                new AlterTypeAddFields()
                        .addField("x_bigint2",     new TableUserDefinedTypeFieldDefinition(BIGINT))
                        .addField("x_text2",       new TableUserDefinedTypeFieldDefinition(TEXT))
                        .addField("x_blob2",       new TableUserDefinedTypeFieldDefinition(BLOB))
                        .addField("x_boolean2",    new TableUserDefinedTypeFieldDefinition(BOOLEAN))
                        .addField("x_timestamp3",  new TableUserDefinedTypeFieldDefinition(TIMESTAMP))
                        .addField("x_int2",        new TableUserDefinedTypeFieldDefinition(INT)));
    }

    @Test
    public void should_create_type_bean() {
        getQuickStartDatabase().createType(SampleUdtAddress.class, null);
    }

    @Test
    public void should_create_table_with_udts() {
        getQuickStartDatabase().createTable("demo_table_with_udts_3",
                new TableDefinition()
                        .addColumnText("email")
                        .addColumnText("name")
                        .addColumnUserDefinedType("my_member", "member")
                        .addColumnListUserDefinedType("my_member_list", "member")
                        .addColumnSetUserDefinedType("my_member_set", "member")
                        .addColumnMapUserDefinedType("my_member_set", "member", TableColumnTypes.TEXT)
                        .partitionKey("email"), IF_NOT_EXISTS);

    }

    @Test
    public void should_alter_table_with_udts() {
        getQuickStartDatabase()
                .getTable("demo_table_with_udts_3")
                .alter(new AlterTableAddColumns()
                        .addColumnUserDefinedType("member_x", "member")
                        .addColumnListUserDefinedType("my_member_list_x", "member")
                );
    }

    @Test
    public void should_list_tables_with_udt() {
        getQuickStartDatabase().listTables().forEach(System.out::println);
    }


    @Test
    public void should_create_index_on_map() {
        // Create a Table with a Map
        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("email")
                .addColumnMap("example_map_column", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                .partitionKey("email");

        Table<Row> table = getQuickStartDatabase()
                .createTable("example_index_table", tableDefinition);

        // Index a column
        table.createIndex("example_index_name", "example_map_column", TableIndexMapTypes.KEYS);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonBean{
        private String name;
        private PersonAddress address;
        @JsonProperty("address_list")
        private List<PersonAddress> addressList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonAddress{
        private String city;
        private int zipcode;
    }

    @Test
    public void should_insert_table_with_udts() {

        Database db = getDatabaseVector();

        db.createType("udt_address", new TableUserDefinedTypeDefinition()
                .addFieldText("city")
                .addFieldInt("zipcode"), CreateTypeOptions.IF_NOT_EXISTS);

        Table<Row> table = db.createTable("person", new TableDefinition()
                .addColumnText("name")
                .addColumnUserDefinedType("address", "udt_address")
                .addColumnListUserDefinedType("address_list", "udt_address")
                .addColumnSetUserDefinedType("address_set", "udt_address")
                .addColumnMapUserDefinedType("address_map", "udt_address", TableColumnTypes.TEXT)
                .addPartitionBy("name"), IF_NOT_EXISTS);

        table.insertOne(new Row()
                        .add("name", "cedrick")
                        .add("address", Map.of("zipcode", 12345, "city", "Paris")));

        PersonBean sara =
          new PersonBean("sara", new PersonAddress("Paris", 75018), null);

        table.insertOne(new Row()
                        .add("name", "cedrick")
                        .add("address", Map.of("zipcode", 12345, "city", "Paris")));

        db.getTable("person", PersonBean.class).insertOne(sara);
    }

    @Test
    public void should_update_table_with_udts() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        table.updateOne(eq("name", "sara"),
          new TableUpdateOperation().set("address", Map.of("city","Marseille")));
    }

    @Test
    public void should_update_table_with_udts_2() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        table.updateOne(eq("name", "sara"),
                new TableUpdateOperation().set("address", Map
                        .of("city","Marseille",
                                "zipcode",13000)));
    }

    @Test
    public void should_update_table_with_udts_list_3() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        Map<?,?> updated = Map.of("city","Marseille","zipcode",13000);
        Map<?,?> updated2 = Map.of("city","Paris","zipcode",1111);
        Map<?,?> updated3 = Map.of();

        table.updateOne(eq("name", "sara"),
           new TableUpdateOperation()
                   .set("address_list", List.of(updated, updated2, updated3))
                   .set("address_map", Map.of("key1", updated))
        );
    }

    @Test
    public void should_read_table_with_udts() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        table.findAll().forEach(row -> {
            System.out.println("Row: " + row);
        });

        db.getTable("person", PersonBean.class).findAll().forEach(p -> {
            System.out.println("Person: " + p.getAddress().getCity());
        });
    }

    @Test
    public void should_projection_table_with_udts() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        TableFindCursor<Row, Row> r = table.find(new TableFindOptions().projection(
                Projection.include("address_list", "address_map")));
        r.forEach(row -> {
            System.out.println("Row: " + row);
        });

    }

    @Test
    public void should_projection_table_with_sub_udt() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        TableFindCursor<Row, Row> r = table.find(new TableFindOptions().projection(
                Projection.include("address.country")));
        r.forEach(row -> {
            System.out.println("Row: " + row);
        });

    }

    @Test
    public void should_filter_all_udt() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        TableFindCursor<Row, Row> r = table.find(Filters
                        .all("address_list", Map.of("city","Marseille","zipcode",13000)));
        r.forEach(row -> {
            System.out.println("Row: " + row);
        });

    }

    @Test
    public void should_filter_keys_udtmap() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");

//        Filter filter1 = new Filter();
//        Map values = new HashMap();
//        values.put("$values",
//          Map.of("$in",
//            List.of(Map.of("city","Marseille","zipcode",13000))
//          )
//        );
//        filter1.put("address_map", values);
//        table.find(filter1).forEach(row -> {
//            System.out.println("Row: " + row);
//        });

        Map<?,?> innerConditions = Map.of("city","Marseille","zipcode",13000);
        Filter filterValues = Filters.values("address_map", Filters.in(innerConditions).documentMap);
        table.find(filterValues).forEach(row -> {
            System.out.println("Row: " + row);
        });


    }

    @Test
    public void should_filter_all() {
        Database db = getDatabaseVector();
        Table<Row> table = db.getTable("person");
        table.findAll().forEach(row -> {
            System.out.println("Row: " + row);
        });
    }

    @Test
    public void should_non_string_keys() {
        Database db = getDatabaseVector();

        Table<Row> tableExo = db.createTable("exotic", new TableDefinition()
                .addColumnText("name")
                .addColumnMap("exotic_map_1", TableColumnTypes.INT, TableColumnTypes.TEXT)
                .addColumnMap("normal_map", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                .addPartitionBy("name"), IF_NOT_EXISTS);

        Row entry1 = new Row().addText("name", "entry1")
                .addMap("exotic_map_1", Map.of(1, "one", 2, "two"))
//                .add("exotic_map_2", Map.of(new BigInteger("1"), "one", new BigInteger("2"), "two"))
//                .add("exotic_map_3", Map.of(true, "yes", false, "no"))
//                .add("exotic_map_4", Map.of(UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff"), "uuid1",
//                                            UUID.fromString("7f9619ff-8b86-d011-b42d-00cf4fc964ff"), "uuid2"))
                .add("normal_map", Map.of("k1", "v1", "k2", "v2"));
        tableExo.insertOne(entry1);

    }

    @Data
    @AllArgsConstructor
    public static final class ExoticTableRow {
        String name;
        @JsonProperty("normal_map")
        Map<String, String> normalMap;
        @JsonProperty("exotic_map_1")
        Map<Integer, String> exoticMap;
    }

    @Test
    public void should_add1() {
        ExoticTableRow row1 = new ExoticTableRow("demo",
                Map.of("k2","v2"),
                Map.of(1,"one", 2,"two"));
        getDatabaseVector()
          .getTable("exotic", ExoticTableRow.class)
          .insertOne(row1);
    }

    @Test
    public void should_filter_all1() {
        getDatabaseVector().getTable("exotic").findAll().forEach(System.out::println);
    }

    @Test
    public void should_filter_all2() {
        Database db = getDatabaseVector();
        Table<ExoticTableRow> table = db.getTable("exotic", ExoticTableRow.class);
        table.findAll().forEach(row -> {
            System.out.println(row);
            System.out.println("Row: " + row.getExoticMap());
            System.out.println("Row: " + row.getNormalMap());
        });
    }


}
