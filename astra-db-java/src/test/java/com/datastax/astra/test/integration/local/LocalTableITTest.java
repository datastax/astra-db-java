package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.results.CollectionUpdateResult;
import com.datastax.astra.client.core.paging.TableCursor;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projections;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.options.TableFindOneOptions;
import com.datastax.astra.client.tables.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.results.TableInsertManyResult;
import com.datastax.astra.client.tables.results.TableInsertOneResult;
import com.datastax.astra.client.tables.ddl.AlterTableAddColumns;
import com.datastax.astra.client.tables.ddl.AlterTableAddVectorize;
import com.datastax.astra.client.tables.ddl.AlterTableDropColumns;
import com.datastax.astra.client.tables.ddl.AlterTableDropVectorize;
import com.datastax.astra.client.tables.ddl.CreateIndexOptions;
import com.datastax.astra.client.tables.ddl.CreateTableOptions;
import com.datastax.astra.client.tables.ddl.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.ddl.DropTableIndexOptions;
import com.datastax.astra.client.tables.index.IndexDefinition;
import com.datastax.astra.client.tables.index.IndexDefinitionOptions;
import com.datastax.astra.client.tables.index.VectorIndexDefinition;
import com.datastax.astra.client.tables.index.VectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.client.tables.row.TableUpdate;
import com.datastax.astra.test.integration.AbstractTableITTest;
import com.datastax.astra.test.model.TableCompositeAnnotatedRow;
import com.datastax.astra.test.model.TableCompositeRow;
import com.datastax.astra.test.model.TableCompositeRowGenerator;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.assertj.core.api.Assertions;
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

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.query.Sort.descending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.client.tables.ddl.CreateTableOptions.IF_NOT_EXISTS;
import static com.datastax.astra.client.tables.ddl.DropTableOptions.IF_EXISTS;
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
            database = DataAPIClients.defaultLocalDatabase();
        }
        return database;
    }

    @Test
    @Order(1)
    public void shouldInitiateDatabase() {
        Database db = getDatabase();
        db.dropTableIndex(INDEX_COUNTRY, DropTableIndexOptions.IF_EXISTS);
        db.dropTableIndex(INDEX_ALL_RETURNS_PTEXT, DropTableIndexOptions.IF_EXISTS);
        db.dropTableIndex(INDEX_ALL_RETURNS_VECTOR, DropTableIndexOptions.IF_EXISTS);

        db.dropTable(TABLE_SIMPLE, IF_EXISTS);
        db.dropTable(TABLE_COMPOSITE, IF_EXISTS);
        db.dropTable(TABLE_ALL_RETURNS, IF_EXISTS);
        db.dropTable(TABLE_CASSIO, IF_EXISTS);

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
                CreateIndexOptions.IF_NOT_EXISTS);
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
                .addColumnVector("p_vector", new ColumnDefinitionVector()
                        .dimension(3)
                        .metric(COSINE))
                .addColumnList("p_list_int", ColumnTypes.INT)
                .addColumnSet("p_set_int", ColumnTypes.INT)
                .addColumnMap("p_map_text_text", ColumnTypes.TEXT, ColumnTypes.TEXT)
                .addColumn("p_double_minf", ColumnTypes.DOUBLE)
                .addColumn("p_double_pinf", ColumnTypes.DOUBLE)
                .addColumn("p_float_nan", ColumnTypes.FLOAT)
                .withPartitionKey("p_ascii", "p_bigint")
                .withClusteringColumns(ascending("p_int"), descending("p_boolean")),
                new CreateTableOptions().ifNotExists());
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isTrue();

        tableAllReturns
                .createVectorIndex(INDEX_ALL_RETURNS_VECTOR,
                        new VectorIndexDefinition()
                        .column("p_vector")
                        .options(new VectorIndexDefinitionOptions().metric(COSINE)),
                        new CreateVectorIndexOptions().ifNotExists());

        tableAllReturns.createIndex(INDEX_ALL_RETURNS_PTEXT, new IndexDefinition()
                        .column("p_text")
                        .options(new IndexDefinitionOptions()
                                .ascii(true)
                                .caseSensitive(true)
                                .normalize(true)),
                        CreateIndexOptions.IF_NOT_EXISTS);
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
                        .addColumnVector("vector", new ColumnDefinitionVector()
                                .dimension(1536).metric(COSINE))
                        .withPartitionKey("partition_id")
                        .withClusteringColumns(Sort.descending("row_id")));
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
    @Order(9)
    public void shouldInsertOneTableSimple() {
        Table<Row> table = getDatabase().getTable(TABLE_SIMPLE);
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
    @Order(10)
    public void shouldInsertOneTableComposite() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id", "John");
        TableInsertOneResult res = table.insertOne(row);
        System.out.println(res.getInsertedId());
        // Contains name and id
        assertThat(res.getInsertedId().size()).isEqualTo(2);
        // Converted as a MAP
        assertThat(res.getInsertedIdAsRow().getText("name")).isEqualTo("John");
    }

    @Test
    @Order(11)
    public void shouldInsertOneTableCompositeBean() {
        Table<TableCompositeRow> table = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableCompositeRow row = new TableCompositeRow(42, "Cedrick", "Lunven");
        TableInsertOneResult res = table.insertOne(row);
        assertThat(res.getInsertedId().size()).isEqualTo(2);
        assertThat(res.getInsertedIdAsRow().getText("name")).isEqualTo("Cedrick");
    }

    // ------------------------------------------
    // InsertMany
    // ------------------------------------------

    @Test
    @Order(12)
    public void shouldInsertManyTableComposite() {
        Table<TableCompositeRow> table = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableCompositeRow row = new TableCompositeRow(42, "Cedrick", "Lunven");
        TableInsertManyResult res = table.insertMany(List.of(
                new TableCompositeRow(42, "Cedrick", "Lunven"),
                new TableCompositeRow(41, "Hind", "Lunven")),
                new TableInsertManyOptions());
    }

    @Test
    @Order(13)
    public void shouldFindOneTableComposite() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id", "John");
        Filter johnFilter = new Filter(Map.of("id", "Lunven","name", "Cedrick"));
        Optional<Row> res = table.findOne(johnFilter,
                new TableFindOneOptions().projection(Projections.include("id", "age")));
        assertThat(res).isPresent();
    }

    @Test
    @Order(14)
    public void shouldDeleteOneTableComposite() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Filter johnFilter = new Filter(Map.of("id", "Lunven","name", "Cedrick"));
        table.deleteOne(johnFilter);
    }

    @Test
    @Order(16)
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
    @Order(17)
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

    @Test
    public void shouldCreateTableFromBeanDefinition() {
        getDatabase().createTable(TableCompositeAnnotatedRow.class, IF_NOT_EXISTS);
        assertThat(getDatabase().tableExists("table_composite_pk_annotated")).isTrue();
    }

    @Test
    public void shouldAlterAddColumns() {
        Table<Row> t = getDatabase().getTable(TABLE_SIMPLE);
        // Add Column (simple)
        t.alter(new AlterTableAddColumns().addColumnText("new_column"));
        assertThat(t.getDefinition().getColumns().containsKey("new_column")).isTrue();

        // Add Column (Vector)
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isFalse();
        t.alter(new AlterTableAddColumns().addColumnVector("vv",
                new ColumnDefinitionVector().dimension(1024).metric(COSINE)));
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isTrue();

        // Add Vectorize
        t.alter(new AlterTableAddVectorize().columns(
                Map.of("vv", new VectorServiceOptions()
                        .modelName("mistral-embed")
                        .provider("mistral"))))
        ;

        // Drop Vectorize
        t.alter(new AlterTableDropVectorize("vv"));

        // Drop Columns
        t.alter(new AlterTableDropColumns("vv", "new_column"));
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isFalse();
        assertThat(t.getDefinition().getColumns().containsKey("new_column")).isFalse();
    }

    @Test
    public void should_insert_many() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);

        // Creating a few records
        Row row1 = new Row().addInt("age", 22).addText("name", "John").addText("id", "Connor");
        Row row2 = new Row().addInt("age", 50).addText("name", "Sara").addText("id", "Connor");
        Row row3 = new Row().addInt("age", 50).addText("name", "Doctor").addText("id", "Silberman");
        TableInsertManyResult res = table.insertMany(
                List.of(row1, row2, row3), new TableInsertManyOptions()
                .ordered(false)
                .returnDocumentResponses(true));
        System.out.println(res.getInsertedIds());
        System.out.println(res.getPrimaryKeySchema());
        System.out.println(res.getDocumentResponses());
    }

    @Test
    public void should_delete_many() {
        Table<TableCompositeRow> t = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        t.insertMany(TableCompositeRowGenerator.generateUniqueRandomRows(75));

        t.deleteMany(new Filter().where("id").isEqualsTo("lunven"));
    }

    @Test
    public void should_work_with_returnedResponses() {
        Table<TableCompositeRow> t = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableInsertManyResult res = t.insertMany(TableCompositeRowGenerator.generateUniqueRandomRows(3),
                new TableInsertManyOptions()
                        .ordered(false)
                        .returnDocumentResponses(false));
        assertThat(res.getInsertedIds().size()).isEqualTo(3);
        assertThat(res.getPrimaryKeySchema()).isNotNull();
        assertThat(res.getDocumentResponses().size()).isEqualTo(0);

    }

    @Test
    public void should_work_with_cursors() {
        Table<TableCompositeRow> t = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        t.deleteAll();
        t.insertMany(TableCompositeRowGenerator.generateUniqueRandomRows(175));

        TableCursor<TableCompositeRow> cursor = t.findAll();
        while (cursor.hasNext()) {
            TableCompositeRow row = cursor.next();
            System.out.println(row);
        }
    }

    @Test
    public void should_updateOne() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        table.deleteAll();

        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "Cedrick")
                .addText("id", "Lunven");
        table.insertOne(row);
        Filter johnFilter = new Filter(Map.of("id", "Lunven","name", "Cedrick"));
        assertThat(table.findOne(johnFilter)).isPresent();

        // Update the document
        CollectionUpdateResult u1 = table.updateOne(johnFilter, TableUpdate.create()
                .set("name", "new"));
                //.updateMul(Map.of("price", 1.1d)));
        Assertions.assertThat(u1.getMatchedCount()).isEqualTo(1);
        Assertions.assertThat(u1.getModifiedCount()).isEqualTo(1);
    }


}
