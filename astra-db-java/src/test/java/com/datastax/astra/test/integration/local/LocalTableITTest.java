package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.AlterTableAddColumns;
import com.datastax.astra.client.tables.commands.AlterTableAddVectorize;
import com.datastax.astra.client.tables.commands.AlterTableDropColumns;
import com.datastax.astra.client.tables.commands.AlterTableDropVectorize;
import com.datastax.astra.client.tables.commands.TableUpdateOperation;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.commands.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.commands.results.TableInsertOneResult;
import com.datastax.astra.client.tables.commands.results.TableUpdateResult;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDuration;
import com.datastax.astra.client.tables.definition.columns.ColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.ColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDescriptor;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
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
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.query.Sort.descending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;
import static com.datastax.astra.client.tables.commands.options.DropTableOptions.IF_EXISTS;
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
            database = DataAPIClients.localDbWithDefaultKeyspace();
        }
        return database;
    }

    @Test
    @Order(1)
    public void shouldInitiateDatabase() throws Exception {
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
                .partitionKey("email"));
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isTrue();

        // Create Index Simple
        tableSimple.createIndex(INDEX_COUNTRY, new TableIndexDefinition()
                    .column("country")
                    .options(new TableIndexDefinitionOptions()
                            .ascii(true)
                            .caseSensitive(true)
                            .normalize(true)),
                CreateIndexOptions.IF_NOT_EXISTS);
    }

    @Test
    public void listIndex() {
        for (TableIndexDescriptor tid : getDatabase().getTable(TABLE_SIMPLE).listIndexes()) {
            System.out.println(tid.getName());
        }
    }

    @Test
    @Order(3)
    public void shouldCreateTableComposite() {
        getDatabase().createTable(TABLE_COMPOSITE, new TableDefinition()
                        .addColumnText("id")
                        .addColumnInt("age")
                        .addColumnText("name")
                        .partitionKey("id", "name"));
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
                .addColumn("p_duration2", ColumnTypes.DURATION)
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
                .partitionKey("p_ascii", "p_bigint")
                .clusteringColumns(ascending("p_int"), descending("p_boolean")),
                new CreateTableOptions().ifNotExists(true));
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isTrue();

        tableAllReturns
                .createVectorIndex(INDEX_ALL_RETURNS_VECTOR,
                        new TableVectorIndexDefinition()
                        .column("p_vector")
                        .options(new TableVectorIndexDefinitionOptions().metric(COSINE)),
                        new CreateVectorIndexOptions().ifNotExists(true));

        tableAllReturns.createIndex(INDEX_ALL_RETURNS_PTEXT, new TableIndexDefinition()
                        .column("p_text")
                        .options(new TableIndexDefinitionOptions()
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
                        .partitionKey("partition_id")
                        .clusteringColumns(Sort.descending("row_id")));
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
                .listTableNames())
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
                new TableFindOneOptions().projection(Projection.include("id", "age")));
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

        DataAPIClientOptions.getSerdesOptions().disableEncodeDataApiVectorsAsBase64();

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
                .addInet("p_inet", InetAddress.getByAddress(new byte[]{12, 34, 56, 78}))
                .addDouble("p_double", 987.6543d)
                .addFloat("p_float", 66.55f)
                .addFloat("p_float_nan", Float.NaN)
                .addTimeStamp("p_timestamp", Instant.now())
                .addTime("p_time", localTime)
                .addUUID("p_uuid", java.util.UUID.fromString("9c5b94b1-35ad-49bb-b118-8e8fc24abf80"))
                .addLocalDate("p_date", LocalDate.of(2015,5,3))
                .addDecimal("p_decimal", new BigDecimal("123.45"))
                .addVector("p_vector", new DataAPIVector(new float[] {.1f, 0.2f, 0.3f}))
                .addList("p_list_int", List.of(4, 17, 34))
                .addSet("p_set_int",  Set.of(9, 81))
                .addDuration("p_duration", Duration.ofHours(12).plusMinutes(48))
                .addTableDuration("p_duration2", TableDuration.of(
                        Period.ofDays(3),
                        Duration.ofHours(12).plusMinutes(48)));
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
    }

    @Test
    @Order(18)
    public void shouldCreateTableFromBeanDefinition() {
        getDatabase().createTable(TableCompositeAnnotatedRow.class, IF_NOT_EXISTS);
        assertThat(getDatabase().tableExists("table_composite_pk_annotated")).isTrue();
    }

    @Test
    @Order(19)
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
                        .provider("mistral"))));

        // Drop Vectorize
        t.alter(new AlterTableDropVectorize("vv"));

        // Drop Columns
        t.alter(new AlterTableDropColumns("vv", "new_column"));
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isFalse();
        assertThat(t.getDefinition().getColumns().containsKey("new_column")).isFalse();

    }

    @Test
    @Order(20)
    public void should_insert_many() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);

        // Creating a few records
        Row row1 = new Row().addInt("age", 22).addText("name", "John").addText("id", "Connor");
        Row row2 = new Row().addInt("age", 50).addText("name", "Sara").addText("id", "Connor");
        Row row3 = new Row().addInt("age", 50).addText("name", "Doctor").addText("id", "Silberman");
        TableInsertManyResult res = table.insertMany(
                List.of(row1, row2, row3), new TableInsertManyOptions()
                .ordered(false)
                .timeout(10000L)
                .returnDocumentResponses(true));
        System.out.println(res.getInsertedIds());
        System.out.println(res.getPrimaryKeySchema());
        System.out.println(res.getDocumentResponses());
    }

    @Test
    public void should_delete_many() {
        Table<TableCompositeRow> t = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        t.insertMany(
                new TableCompositeRow(10, "a", "b"),
                new TableCompositeRow(20, "a", "b"),
                new TableCompositeRow(30, "a", "b"));

        t.deleteMany(new Filter()
                .where("name").isEqualsTo("a")
                .where("id").isEqualsTo("b"));
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
        Table<Row> tableCities = getDatabase().createTable("cities", new TableDefinition()
                .addColumnText("country")
                .addColumnText("city")
                .addColumnInt("population")
                .partitionKey("country")
                .clusteringColumns(Sort.ascending("city")), IF_NOT_EXISTS);
        tableCities.deleteAll();

        List<Row> rowsFrance = new ArrayList<>();
        rowsFrance.add(new Row().addText("country", "france").addText("city", "paris").addInt("population", 2000000));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "paris").addInt("population", 2148271));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "marseille").addInt("population", 861635));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "lyon").addInt("population", 513275));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "toulouse").addInt("population", 471941));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "nice").addInt("population", 341032));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "nantes").addInt("population", 303382));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "strasbourg").addInt("population", 277270));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "montpellier").addInt("population", 277639));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "bordeaux").addInt("population", 252040));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "lille").addInt("population", 232741));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "rennes").addInt("population", 216815));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "reims").addInt("population", 182592));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "le havre").addInt("population", 170147));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "saint-étienne").addInt("population", 171483));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "toulon").addInt("population", 171953));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "grenoble").addInt("population", 158454));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "dijon").addInt("population", 155090));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "angers").addInt("population", 151229));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "nîmes").addInt("population", 150672));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "villeurbanne").addInt("population", 147712));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "saint-denis").addInt("population", 147931));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "aix-en-provence").addInt("population", 143006));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "le mans").addInt("population", 143599));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "clermont-ferrand").addInt("population", 141569));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "brest").addInt("population", 139163));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "tours").addInt("population", 136565));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "amiens").addInt("population", 133448));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "limoges").addInt("population", 132175));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "annecy").addInt("population", 125694));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "perpignan").addInt("population", 120605));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "boulogne-billancourt").addInt("population", 120071));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "metz").addInt("population", 116581));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "besançon").addInt("population", 116353));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "orléans").addInt("population", 114286));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "rouen").addInt("population", 110145));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "mulhouse").addInt("population", 108312));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "caen").addInt("population", 106538));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "nancy").addInt("population", 104321));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "argenteuil").addInt("population", 104962));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "montreuil").addInt("population", 104770));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "saint-paul").addInt("population", 103884));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "roubaix").addInt("population", 96990));
        tableCities.insertMany(rowsFrance);

        List<Row> rowsEngland= new ArrayList<>();
        rowsEngland.add(new Row().addText("country", "england").addText("city", "london").addInt("population", 8908081));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "birmingham").addInt("population", 1141816));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "leeds").addInt("population", 789194));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "sheffield").addInt("population", 584853));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "manchester").addInt("population", 547627));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "liverpool").addInt("population", 494814));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "bristol").addInt("population", 463400));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "nottingham").addInt("population", 331069));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "leicester").addInt("population", 355218));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "coventry").addInt("population", 366785));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "kingston upon hull").addInt("population", 260645));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "stoke-on-trent").addInt("population", 256375));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "wolverhampton").addInt("population", 254406));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "plymouth").addInt("population", 263100));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "derby").addInt("population", 255394));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "southampton").addInt("population", 253651));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "reading").addInt("population", 161780));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "northampton").addInt("population", 212100));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "luton").addInt("population", 213528));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "portsmouth").addInt("population", 205056));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "newcastle upon tyne").addInt("population", 300196));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "sunderland").addInt("population", 275506));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "warrington").addInt("population", 209547));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "york").addInt("population", 210618));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "oxford").addInt("population", 154600));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "peterborough").addInt("population", 202259));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "cambridge").addInt("population", 123867));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "doncaster").addInt("population", 109805));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "milton keynes").addInt("population", 229941));
        tableCities.insertMany(rowsEngland);

        List<Row> rowsGermany= new ArrayList<>();
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "berlin").addInt("population", 3644826));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "hamburg").addInt("population", 1841179));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "munich").addInt("population", 1471508));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "cologne").addInt("population", 1085664));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "frankfurt").addInt("population", 753056));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "stuttgart").addInt("population", 634830));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "düsseldorf").addInt("population", 619294));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "dortmund").addInt("population", 588462));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "essen").addInt("population", 583109));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "leipzig").addInt("population", 587857));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "bremen").addInt("population", 567559));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "dresden").addInt("population", 556780));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "hanover").addInt("population", 538068));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "nuremberg").addInt("population", 518370));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "duisburg").addInt("population", 498590));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "bochum").addInt("population", 365587));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "wuppertal").addInt("population", 355100));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "bielefeld").addInt("population", 334195));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "bonn").addInt("population", 329673));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "mannheim").addInt("population", 309370));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "karlsruhe").addInt("population", 313092));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "münster").addInt("population", 314319));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "wiesbaden").addInt("population", 278342));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "augsburg").addInt("population", 295135));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "gelsenkirchen").addInt("population", 260368));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "monchengladbach").addInt("population", 261454));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "braunschweig").addInt("population", 248292));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "chemnitz").addInt("population", 247237));
        rowsGermany.add(new Row().addText("country", "germany").addText("city", "kiel").addInt("population", 246306));
        tableCities.insertMany(rowsGermany);

        // Cities in Italy
        List<Row> rowsItaly = new ArrayList<>();
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "rome").addInt("population", 2873000));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "milan").addInt("population", 1372000));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "naples").addInt("population", 962000));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "turin").addInt("population", 870000));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "palermo").addInt("population", 657561));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "genoa").addInt("population", 580097));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "bologna").addInt("population", 389261));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "florence").addInt("population", 382258));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "bari").addInt("population", 325183));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "catania").addInt("population", 311584));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "venice").addInt("population", 261321));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "verona").addInt("population", 257353));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "messina").addInt("population", 231708));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "padua").addInt("population", 210440));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "trieste").addInt("population", 204234));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "taranto").addInt("population", 195227));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "brescia").addInt("population", 196745));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "prato").addInt("population", 194590));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "parma").addInt("population", 195687));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "modena").addInt("population", 185273));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "reggio calabria").addInt("population", 181447));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "reggio emilia").addInt("population", 171944));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "perugia").addInt("population", 166676));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "livorno").addInt("population", 158371));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "ravenna").addInt("population", 159115));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "cagliari").addInt("population", 154083));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "foggia").addInt("population", 151372));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "rimini").addInt("population", 149403));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "salerno").addInt("population", 133970));
        rowsItaly.add(new Row().addText("country", "italy").addText("city", "ferrara").addInt("population", 132009));
        tableCities.insertMany(rowsItaly);
        System.out.println(tableCities.findAll().toList().size());
        tableCities
                .find(Filters.eq("country", "france"), new TableFindOptions())
                .forEach(row -> System.out.println(row.get("city")));

    }

    @Test
    public void should_updateOne() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        table.deleteAll();

        TableInsertManyOptions options = new TableInsertManyOptions()
                .returnDocumentResponses(true);

        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "Cedrick")
                .addText("id", "Lunven");
        table.insertOne(row);
        Filter johnFilter = new Filter(Map.of("id", "Lunven","name", "Cedrick"));
        assertThat(table.findOne(johnFilter)).isPresent();

        // Update the document
        table.updateOne(johnFilter, new TableUpdateOperation().set("age", 43));

    }

    @Test
    public void revampingOptions() {
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA)
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("myKey"))
                .addCaller("myCaller", "ssss")
                .httpClientOptions(new HttpClientOptions()
                        .httpVersion(HttpClient.Version.HTTP_2)
                        .httpRedirect(HttpClient.Redirect.NORMAL))
                .timeoutOptions(new TimeoutOptions()
                        .requestTimeoutMillis(1000));

        DataAPIClient client = new DataAPIClient("token", options);

        Database database1 = client.getDatabase("endpoint");

        Database database2 = client.getDatabase("endpoints",
                new DatabaseOptions("token" , options).keyspace("otherKeyspace"));

        database2.getOptions().getDataAPIClientOptions();
        Table<Row> table = database1.getTable("table");
    }

}
