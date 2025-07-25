package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.options.AstraFindAvailableRegionsOptions;
import com.datastax.astra.client.collections.definition.documents.types.TimeUUID;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.vectorize.SupportModelStatus;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.options.FindEmbeddingProvidersOptions;
import com.datastax.astra.client.databases.commands.options.FindRerankingProvidersOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTypeAddFields;
import com.datastax.astra.client.tables.commands.AlterTypeRenameFields;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateTypeOptions;
import com.datastax.astra.client.tables.commands.options.DropTypeOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldDefinitionList;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.test.model.SampleUdtAddress;
import com.dtsx.astra.sdk.utils.JsonUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

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

    public static final String ASTRA_DB_TOKEN =
            "REDACTED";
    public static final String DB_URL_NON_VECTOR =
            "https://f9754177-52e2-4b66-935e-78cfd0be0042-us-west-2.apps.astra-dev.datastax.com";
    public static final String DB_URL_VECTOR =
            "https://853cb027-ae65-4bfa-94a3-5e1931268372-us-west-2.apps.astra-dev.datastax.com";

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
        getLocalDatabase().createType("udt_one", new TableUserDefinedTypeDefinition()
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
        getLocalDatabase().createType(SampleUdtAddress.class, null);
    }

    @Test
    public void should_create_table_withudts() {
//        getLocalDatabase().createTable("demo_table_with_udts",)
//                new TableDefinition()
//                        .addColumnText("email")
//                        .addColumnText("name")
//                        //.addColumnUserDefinedType("address", "udt_one")
//                        .partitionKey("email"), IF_NOT_EXISTS);
    }


}
