package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projections;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.commands.TableDeleteResult;
import com.datastax.astra.client.tables.commands.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.TableInsertManyResult;
import com.datastax.astra.client.tables.commands.TableInsertOneResult;
import com.datastax.astra.client.tables.index.IndexDefinition;
import com.datastax.astra.client.tables.index.IndexDefinitionOptions;
import com.datastax.astra.client.tables.index.IndexOptions;
import com.datastax.astra.client.tables.index.VectorIndexDefinition;
import com.datastax.astra.client.tables.index.VectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.index.VectorIndexOptions;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.IntrospectedBean;
import com.datastax.astra.client.tables.mapping.IntrospectedField;
import com.datastax.astra.client.tables.mapping.Table;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.test.integration.AbstractTableITTest;
import com.datastax.astra.test.model.TableSimpleRow;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.datastax.astra.client.core.query.Sorts.ascending;
import static com.datastax.astra.client.core.query.Sorts.descending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test Operation Locally with Docker and DSE/HCD
 */
public class LocalTableITTest extends AbstractTableITTest {

    public static final String TABLE_SIMPLE       = "table_simple";
    public static final String TABLE_COMPOSITE    = "table_composite_pk";
    public static final String TABLE_TYPES        = "table_types";
    public static final String TABLE_CASSIO       = "table_cassio";
    public static final String TABLE_ALL_RETURNS  = "table_all_returns";

    public static final String INDEX_COUNTRY      = "country_index";
    public static final String INDEX_ALL_RETURNS_VECTOR = "idx_all_returns_pvector";
    public static final String INDEX_ALL_RETURNS_PTEXT  = "idx_all_returns_ptext";

    @Override
    protected AstraEnvironment getAstraEnvironment() { return null; }
    @Override
    protected CloudProviderType getCloudProvider() { return null; }
    @Override
    protected String getRegion() { return "";}

    @Override
    protected Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.createDefaultLocalDatabase();
        }
        return database;
    }

    @Test
    @Order(1)
    public void shouldInitiateDatabase() {
        if (getDatabase().tableExists(TABLE_SIMPLE)) {
            getDatabase()
                    .getTable(TABLE_SIMPLE)
                    .dropIndex(INDEX_COUNTRY);
            getDatabase().dropTable(TABLE_SIMPLE);
        }
        if (getDatabase().tableExists(TABLE_CASSIO)) {
            getDatabase().dropTable(TABLE_COMPOSITE);
        }
        if (getDatabase().tableExists(TABLE_ALL_RETURNS)) {
            getDatabase().dropTable(TABLE_ALL_RETURNS);
        }
        if (getDatabase().tableExists(TABLE_CASSIO)) {
            getDatabase().dropTable(TABLE_CASSIO);
        }
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_COMPOSITE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_CASSIO)).isFalse();
    }

    @Test
    @Order(2)
    public void shouldCreateTableSimple() {
        // Simple
        com.datastax.astra.client.tables.Table<Row> tableSimple = getDatabase()
                .createTable(TABLE_SIMPLE, new TableDefinition()
                .addColumnText("email")
                .addColumnInt("age")
                .addColumnText("name")
                .addColumnText("country")
                .addColumnBoolean("human")
                .withPartitionKey("email"));
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isTrue();

        // Create Index Simple
        tableSimple.createIndex(INDEX_COUNTRY, new IndexDefinition()
                    .column("country")
                    .options(new IndexDefinitionOptions()
                            .ascii(true)
                            .caseSensitive(true)
                            .normalize(true)),
                new IndexOptions().ifNotExists());
    }

    @Test
    @Order(3)
    public void shouldCreateTableComposite() {
        getDatabase().createTable(TABLE_COMPOSITE, new TableDefinition()
                        .addColumnText("id")
                        .addColumnInt("age")
                        .addColumnText("name")
                        .withPartitionKey("id", "name"));
        assertThat(getDatabase().tableExists(TABLE_COMPOSITE)).isTrue();
    }

    @Test
    @Order(4)
    public void shouldCreateTableAllReturns() {
        com.datastax.astra.client.tables.Table<Row> tableAllReturns = getDatabase().createTable(TABLE_ALL_RETURNS, new TableDefinition()
                .addColumn("p_ascii", ColumnTypes.ASCII)
                .addColumn("p_bigint", ColumnTypes.BIGINT)
                .addColumn("p_blob", ColumnTypes.BLOB)
                .addColumn("p_boolean", ColumnTypes.BOOLEAN)
                .addColumn("p_date", ColumnTypes.DATE)
                .addColumn("p_decimal", ColumnTypes.DECIMAL)
                .addColumn("p_tinyint", ColumnTypes.TINYINT)
                .addColumn("p_double", ColumnTypes.DOUBLE)
                .addColumn("p_duration", ColumnTypes.DURATION)
                .addColumn("p_float", ColumnTypes.FLOAT)
                .addColumn("p_int", ColumnTypes.INT)
                .addColumn("p_inet", ColumnTypes.INET)
                .addColumn("p_smallint", ColumnTypes.SMALLINT)
                .addColumn("p_text", ColumnTypes.TEXT)
                .addColumn("p_text_nulled", ColumnTypes.TEXT)
                .addColumn("p_text_omitted", ColumnTypes.TEXT)
                .addColumn("p_time", ColumnTypes.TIME)
                .addColumn("p_timestamp", ColumnTypes.TIMESTAMP)
                .addColumn("p_tinyint", ColumnTypes.TINYINT)
                .addColumn("p_uuid", ColumnTypes.UUID)
                .addColumn("p_varint", ColumnTypes.VARINT)
                .addColumnVector("p_vector", 3, COSINE)
                .addColumnList("p_list_int", ColumnTypes.INT)
                .addColumnSet("p_set_int", ColumnTypes.INT)
                .addColumnMap("p_map_text_text", ColumnTypes.TEXT, ColumnTypes.TEXT)
                .addColumn("p_double_minf", ColumnTypes.DOUBLE)
                .addColumn("p_double_pinf", ColumnTypes.DOUBLE)
                .addColumn("p_float_nan", ColumnTypes.FLOAT)
                .withPartitionKey("p_ascii", "p_bigint")
                .withClusteringColumns(ascending("p_int"), descending("p_boolean")),
                new TableOptions().ifNotExists());
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isTrue();

        tableAllReturns
                .createVectorIndex(INDEX_ALL_RETURNS_VECTOR,
                        new VectorIndexDefinition()
                        .column("p_vector")
                        .options(new VectorIndexDefinitionOptions().metric(COSINE)),
                        new VectorIndexOptions().ifNotExists());

        tableAllReturns.createIndex(INDEX_ALL_RETURNS_PTEXT, new IndexDefinition()
                        .column("p_text")
                        .options(new IndexDefinitionOptions()
                                .ascii(true)
                                .caseSensitive(true)
                                .normalize(true)),
                        new IndexOptions().ifNotExists());
    }

    @Test
    @Order(5)
    public void shouldCreateTableAllCassio() {
        assertThat(getDatabase().tableExists(TABLE_CASSIO)).isFalse();
        getDatabase().createTable(TABLE_CASSIO, new TableDefinition()
                        .addColumn("partition_id", ColumnTypes.TEXT)
                        .addColumn("attributes_blob", ColumnTypes.TEXT)
                        .addColumn("body_blob", ColumnTypes.TEXT)
                        .addColumn("row_id", ColumnTypes.UUID)
                        .addColumnMap("metadata_s", ColumnTypes.TEXT, ColumnTypes.TEXT)
                        .addColumnVector("vector", 1536, COSINE)
                        .withPartitionKey("partition_id")
                        .withClusteringColumns(descending("row_id")));
        assertThat(getDatabase().tableExists("table_cassio")).isTrue();
    }

    @Test
    @Order(6)
    public void shouldListTables() {
        getDatabase()
                .listTables()
                .forEach(t -> System.out.println("Table: " +  t.getName() + " - pk="
                        + t.getDefinition().getPrimaryKey().getPartitionBy()));
    }

    @Test
    @Order(7)
    public void shouldListTableNames() {
        assertThat(getDatabase()
                .listTableNames()
                .collect(Collectors.toList()))
                .isNotNull();
    }

    @Test
    @Order(8)
    public void shouldAccessTableSimple() {
        String tableName = "table_simple";
        assertThat(getDatabase().tableExists(tableName)).isTrue();
        // Accessing the Table
        com.datastax.astra.client.tables.Table<Row> tableSimple = getDatabase().getTable(tableName);
        assertThat(tableSimple).isNotNull();
        assertThat(tableSimple.getTableName()).isEqualTo(tableName);
        // Accessing the Table Definitions
        TableDefinition desc = tableSimple.getDefinition();
        assertThat(desc).isNotNull();
        assertThat(desc.getPrimaryKey().getPartitionBy()).isEqualTo(List.of("email"));
    }

    // ------------------------------------------
    // InsertOne
    // ------------------------------------------

    @Test
    public void shouldInsertOneTableSimple() {
        com.datastax.astra.client.tables.Table<Row> table = getDatabase().getTable(TABLE_SIMPLE);
        Row row = new Row()
                .addBoolean("human", true)
                .addInt("age", 42)
                .addText("name", "John")
                .addText("country", "France")
                .addText("email",  "cedrick@datastax.com");
        TableInsertOneResult res = table.insertOne(row);
        System.out.println(res.getInsertedId());
        System.out.println(res.getInsertedIdAsRow().get("email"));
    }

    @Test
    public void shouldInsertOneTableComposite() {
        com.datastax.astra.client.tables.Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id", "John");
        TableInsertOneResult res = table.insertOne(row);
        assertThat(res.getInsertedId().size()).isEqualTo(1);
        assertThat(res.getInsertedIdAsRow().size()).isEqualTo(1);
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    private static class TableCompositeRow {
        private int age;
        private String name;
        private String id;
    }

    @Test
    public void shouldInsertOneTableCompositeBean() {
        com.datastax.astra.client.tables.Table<TableCompositeRow> table = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableCompositeRow row = new TableCompositeRow(42, "Cedrick", "Lunven");
        TableInsertOneResult res = table.insertOne(row);
        assertThat(res.getInsertedId().size()).isEqualTo(2);
        assertThat(res.getInsertedIdAsRow().getText("name")).isEqualTo("Cedrick");
    }

    @Data
    @Table
    @NoArgsConstructor @AllArgsConstructor
    private static class TableCompositeRowAnnotation {

        @Column(name = "age", type = ColumnTypes.INT)
        private int agex;

        @Column(name = "name", type = ColumnTypes.TEXT)
        private String namex;

        @Column(name = "idx", type = ColumnTypes.TEXT)
        private String idx;
    }

    @Test
    public void shouldInsertOneTableAnnotatedBean() {
        com.datastax.astra.client.tables.Table<TableCompositeRowAnnotation> table = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRowAnnotation.class);
        TableCompositeRowAnnotation row = new TableCompositeRowAnnotation(42, "Cedrick", "Lunven");
        TableInsertOneResult res = table.insertOne(row);
        assertThat(res.getInsertedId().size()).isEqualTo(1);
        assertThat(res.getInsertedIdAsRow().size()).isEqualTo(1);
    }

    // ------------------------------------------
    // InsertMany
    // ------------------------------------------

    @Test
    public void shouldInsertManyTableComposite() {
        com.datastax.astra.client.tables.Table<TableCompositeRow> table = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableCompositeRow row = new TableCompositeRow(42, "Cedrick", "Lunven");
        TableInsertManyResult res = table.insertMany(List.of(
                new TableCompositeRow(42, "Cedrick", "Lunven"),
                new TableCompositeRow(41, "Hind", "Lunven")),
                new TableInsertManyOptions());
    }


    @Test
    public void shouldFindOneTableComposite() {
        com.datastax.astra.client.tables.Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id", "John");
        Filter johnFilter = new Filter(Map.of("id", "John","name", "John"));
        Optional<Row> res = table.findOne(johnFilter,
                new TableFindOneOptions().projection(Projections.include("id", "age")));
        assertThat(res).isPresent();
    }

    @Test
    public void shouldDeleteOneTableComposite() {
        com.datastax.astra.client.tables.Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id", "John");
        Filter johnFilter = new Filter(Map.of("id", "John","name", "John"));
        TableDeleteResult res = table.deleteOne(johnFilter);
        assertThat(res.getDeletedCount()).isEqualTo(1);

    }



    @Test
    public void shouldInsertOneAllReturns() throws UnknownHostException {
        String timeString = "13:30:54.234";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime localTime = LocalTime.parse(timeString, formatter);

        Row row = new Row()
                .addAscii("p_ascii", "abc")
                .addBigInt("p_bigint", 10002L)
                .addInt("p_int", 987)
                .addBoolean("p_boolean", false)
                .addText("p_text", "Ålesund")
                .addText("p_text_omitted", null)
                .addDouble("p_double_pinf", Double.MAX_VALUE)
                .addDouble("p_double_minf", Double.NEGATIVE_INFINITY)
                .addBlob("p_blob", "blob".getBytes())
                .addSmallInt("p_smallint", (short) 200)
                .addVarInt("p_varint",new BigInteger("444"))
                .addTinyInt("p_tinyint",(byte) 17)
                .addDuration("p_duration", Duration.ofHours(12).plusMinutes(48))
                .addInet("p_inet", InetAddress.getByAddress(new byte[]{12, 34, 56, 78}))
                .addDouble("p_double", 987.6543d)
                .addFloat("p_float", 66.55f)
                .addFloat("p_float_nan", Float.NaN)
                .addTimeStamp("p_timestamp", Instant.now())
                .addTime("p_time", localTime)
                .addUUID("p_uuid", java.util.UUID.fromString("9c5b94b1-35ad-49bb-b118-8e8fc24abf80"))
                .addDate("p_date", LocalDate.of(2015,5,3))
                .addDecimal("p_decimal", new BigDecimal("123.45"))
                .addVector("p_vector", DataAPIVector.of(.1f, 0.2f, 0.3f))
                .addList("p_list_int", List.of(4, 17, 34))
                .addSet("p_set_int",  Set.of(9, 81));

                // .add("p_vector", new float[] {.1f, .2f, .3f})
                //.addTableDuration("p_duration", TableDuration.of(
                //        Period.ofDays(3),
                //        Duration.ofHours(12).plusMinutes(48)));
        getDatabase().getTable(TABLE_ALL_RETURNS).insertOne(row);
    }


    @Test
    public void shouldFindOneAllReturns() throws UnknownHostException {
        com.datastax.astra.client.tables.Table<Row> tableAllReturns = getDatabase().getTable(TABLE_ALL_RETURNS);

        Filter top = new Filter(Map.of(
                "p_ascii", "abc",
                "p_bigint", 10002L));
        Row row = tableAllReturns
                .findOne(top)
                .orElseThrow(() -> new IllegalArgumentException("Row not found"));
        assertThat(row.getText("p_ascii")).isEqualTo( "abc");
        assertThat(row.getBigInt("p_bigint")).isEqualTo( 10002L);

        //Row row2 = tableAllReturns.findOne(
        //        and(eq( "p_ascii", "abc"),
        //            eq("p_bigint", 10002L)
        //        )).orElseThrow(() -> new IllegalArgumentException("Row not found"));

        //assertThat(row2.getText("p_ascii")).isEqualTo( "abc");
        //assertThat(row2.getBigInt("p_bigint")).isEqualTo( 10002L);
        /*
        .addAscii("p_ascii", "abc")
                .addBigInt("p_bigint", 10002L)
                .addInt("p_int", 987)
                .addBoolean("p_boolean", false)
                .addText("p_text", "Ålesund")
                .addText("p_text_omitted", null)
                .addDouble("p_double_pinf", Double.MAX_VALUE)
                .addDouble("p_double_minf", Double.NEGATIVE_INFINITY)
                .addBlob("p_blob", "blob".getBytes())
                .addSmallInt("p_smallint", (short) 200)
                .addVarInt("p_varint",444)
                .addTinyInt("p_tinyint",(short) 17)
                .addDuration("p_duration", Duration.ofHours(12).plusMinutes(48))
                .addInet("p_inet", InetAddress.getByAddress(new byte[]{12, 34, 56, 78}))
                .addDouble("p_double", 987.6543d)
                .addFloat("p_float", 66.55f)
                .addFloat("p_float_nan", Float.NaN)
                .addTimeStamp("p_timestamp", Instant.now())
                .addTime("p_time", localTime)
                .addUUID("p_uuid", java.util.UUID.fromString("9c5b94b1-35ad-49bb-b118-8e8fc24abf80"))
                .addDate("p_date", LocalDate.of(2015,5,3))
                .addDecimal("p_decimal", new BigDecimal("123.45"))
                .addVector("p_vector", DataAPIVector.of(.1f, 0.2f, 0.3f))
                .addList("p_list_int", List.of(4, 17, 34))
                .addSet("p_set_int",  Set.of(9, 81));

                //.addTableDuration("p_duration", TableDuration.of(
                //        Period.ofDays(3),
                //        Duration.ofHours(12).plusMinutes(48)));
         */
    }




}
