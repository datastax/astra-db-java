package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.options.AstraFindAvailableRegionsOptions;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv6;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv7;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.vectorize.SupportModelStatus;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.commands.options.FindEmbeddingProvidersOptions;
import com.datastax.astra.client.databases.commands.options.FindRerankingProvidersOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.dtsx.astra.sdk.utils.JsonUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;

/*
 * Flight DECK for Astra DEV
 *
 * https://flightdeck.dev.cloud-tools.datastax.com/database/f9754177-52e2-4b66-935e-78cfd0be0042
 */
public class TableGAIntegrationTest {

    public static final String ASTRA_DB_TOKEN =
            "AstraCS:PNfjXcyImIviUnrquSdiwlGq:8fa09440482105a921acf86d1ae6e70ebcae9ff2c5e42be62a1b0d451680d055";
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
                .addColumnMap("example_map_column", ColumnTypes.TEXT, ColumnTypes.TEXT)
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

        @Column(name = "videoid", type = ColumnTypes.UUID)
        UUID videoId;

        @Column(name = "rating_counter", type = ColumnTypes.COUNTER)
        Integer ratingCounter;

        @Column(name = "rating_total", type = ColumnTypes.COUNTER)
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

        @Column(name = "id", type = ColumnTypes.TIMEUUID)
        UUIDv6 videoId;

        @Column(name = "col1", type = ColumnTypes.TEXT)
        String col1;

        @Column(name = "col2", type = ColumnTypes.TEXT)
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
    public void should_create_table_with_timeuuid() {
        Table<SampleTable> table3 = getLocalDatabase().getTable(SampleTable.class);

        table3.findAll().forEach(row -> {
            System.out.println("Entity: " + row.getVideoId().getInstant() + " - " + row.col1 + " - " + row.col2);
        });
    }

    @Test
    public void should_create_table_with_counters() {

        Table<VideoRatingTableEntity> table2 = getLocalDatabase()
                .getTable("video_ratings", VideoRatingTableEntity.class);

//        table2.findAll().forEach(entity -> {
//            System.out.println("Entity: " + entity.videoId + " - " + entity.ratingCounter + " - " + entity.ratingTotal);
//        });

        Optional<VideoRatingTableEntity> optional = table2.findOne(
                eq("videoid", UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff"))
        );
        if (optional.isPresent()) {
            VideoRatingTableEntity entity = optional.get();
            System.out.println("Entity: " + entity.videoId + " - " + entity.ratingCounter + " - " + entity.ratingTotal);
        } else {
            System.out.println("No entity found for the given filter.");
        }
    }


}
