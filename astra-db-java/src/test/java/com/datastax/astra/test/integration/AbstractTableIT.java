package com.datastax.astra.test.integration;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.client.core.vectorize.VectorServiceOptions;
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
import com.datastax.astra.client.tables.commands.options.TableDeleteManyOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOneOptions;
import com.datastax.astra.client.tables.commands.options.TableFindOptions;
import com.datastax.astra.client.tables.commands.options.TableInsertManyOptions;
import com.datastax.astra.client.tables.commands.results.TableInsertManyResult;
import com.datastax.astra.client.tables.commands.results.TableInsertOneResult;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDuration;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDescriptor;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.test.integration.model.TableCompositeAnnotatedRow;
import com.datastax.astra.test.integration.model.TableCompositeRow;
import com.datastax.astra.test.integration.model.TableCompositeRowGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.datastax.astra.client.core.query.Sort.ascending;
import static com.datastax.astra.client.core.query.Sort.descending;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;
import static com.datastax.astra.client.tables.commands.options.DropTableOptions.IF_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for Table integration tests.
 * Extend this class and add environment-specific annotations.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableIT extends AbstractDataAPITest {

    // Table names
    public static final String TABLE_SIMPLE = "table_simple";
    public static final String TABLE_COMPOSITE = "table_composite_pk";
    public static final String TABLE_TYPES = "table_types";
    public static final String TABLE_CASSIO = "table_cassio";
    public static final String TABLE_ALL_RETURNS = "table_all_returns";

    // Index names
    public static final String INDEX_COUNTRY = "country_index";
    public static final String INDEX_ALL_RETURNS_VECTOR = "idx_all_returns_pvector";
    public static final String INDEX_ALL_RETURNS_PTEXT = "idx_all_returns_ptext";

    // ------------------------------------------
    // Setup
    // ------------------------------------------

    @BeforeAll
    void setupCleanKeyspace() {
        dropAllCollections();
        dropAllTables();
    }

    @Test
    @Order(1)
    @DisplayName("01. Should initialize database and drop existing tables")
    public void shouldInitiateDatabase() {
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_COMPOSITE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_CASSIO)).isFalse();
    }

    // ------------------------------------------
    // Table Creation
    // ------------------------------------------

    @Test
    @Order(2)
    @DisplayName("02. Should create simple table with index")
    public void shouldCreateTableSimple() {
        Table<Row> tableSimple = getDatabase()
                .createTable(TABLE_SIMPLE, new TableDefinition()
                        .addColumnText("email")
                        .addColumnInt("age")
                        .addColumnText("name")
                        .addColumnText("country")
                        .addColumnBoolean("human")
                        .partitionKey("email"));
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isTrue();

        // Create index on country
        tableSimple.createIndex(INDEX_COUNTRY, new TableRegularIndexDefinition()
                        .column("country")
                        .options(new TableIndexDefinitionOptions()
                                .ascii(true)
                                .caseSensitive(true)
                                .normalize(true)),
                CreateIndexOptions.IF_NOT_EXISTS);
    }

    @Test
    @Order(3)
    @DisplayName("03. Should create table with composite primary key")
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
    @DisplayName("04. Should create table with all column types")
    public void shouldCreateTableAllReturns() {
        Table<Row> tableAllReturns = getDatabase().createTable(TABLE_ALL_RETURNS, new TableDefinition()
                        .addColumn("p_ascii", TableColumnTypes.ASCII)
                        .addColumn("p_bigint", TableColumnTypes.BIGINT)
                        .addColumn("p_blob", TableColumnTypes.BLOB)
                        .addColumn("p_boolean", TableColumnTypes.BOOLEAN)
                        .addColumn("p_date", TableColumnTypes.DATE)
                        .addColumn("p_decimal", TableColumnTypes.DECIMAL)
                        .addColumn("p_tinyint", TableColumnTypes.TINYINT)
                        .addColumn("p_double", TableColumnTypes.DOUBLE)
                        .addColumn("p_duration", TableColumnTypes.DURATION)
                        .addColumn("p_duration2", TableColumnTypes.DURATION)
                        .addColumn("p_float", TableColumnTypes.FLOAT)
                        .addColumn("p_int", TableColumnTypes.INT)
                        .addColumn("p_inet", TableColumnTypes.INET)
                        .addColumn("p_smallint", TableColumnTypes.SMALLINT)
                        .addColumn("p_text", TableColumnTypes.TEXT)
                        .addColumn("p_text_nulled", TableColumnTypes.TEXT)
                        .addColumn("p_text_omitted", TableColumnTypes.TEXT)
                        .addColumn("p_time", TableColumnTypes.TIME)
                        .addColumn("p_timestamp", TableColumnTypes.TIMESTAMP)
                        .addColumn("p_uuid", TableColumnTypes.UUID)
                        .addColumn("p_varint", TableColumnTypes.VARINT)
                        .addColumnVector("p_vector", new TableColumnDefinitionVector()
                                .dimension(3)
                                .metric(COSINE))
                        .addColumnList("p_list_int", TableColumnTypes.INT)
                        .addColumnSet("p_set_int", TableColumnTypes.INT)
                        .addColumnMap("p_map_text_text", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                        .addColumn("p_double_minf", TableColumnTypes.DOUBLE)
                        .addColumn("p_double_pinf", TableColumnTypes.DOUBLE)
                        .addColumn("p_float_nan", TableColumnTypes.FLOAT)
                        .partitionKey("p_ascii", "p_bigint")
                        .clusteringColumns(ascending("p_int"), descending("p_boolean")),
                new CreateTableOptions().ifNotExists(true));
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isTrue();

        // Create vector index
        tableAllReturns.createVectorIndex(INDEX_ALL_RETURNS_VECTOR,
                new TableVectorIndexDefinition()
                        .column("p_vector")
                        .options(new TableVectorIndexDefinitionOptions().metric(COSINE)),
                new CreateVectorIndexOptions().ifNotExists(true));

        // Create text index
        tableAllReturns.createIndex(INDEX_ALL_RETURNS_PTEXT, new TableRegularIndexDefinition()
                        .column("p_text")
                        .options(new TableIndexDefinitionOptions()
                                .ascii(true)
                                .caseSensitive(true)
                                .normalize(true)),
                CreateIndexOptions.IF_NOT_EXISTS);
    }

    @Test
    @Order(5)
    @DisplayName("05. Should create CassIO-style table")
    public void shouldCreateTableCassio() {
        assertThat(getDatabase().tableExists(TABLE_CASSIO)).isFalse();
        getDatabase().createTable(TABLE_CASSIO, new TableDefinition()
                .addColumn("partition_id", TableColumnTypes.TEXT)
                .addColumn("attributes_blob", TableColumnTypes.TEXT)
                .addColumn("body_blob", TableColumnTypes.TEXT)
                .addColumn("row_id", TableColumnTypes.UUID)
                .addColumnMap("metadata_s", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                .addColumnVector("vector", new TableColumnDefinitionVector()
                        .dimension(1536).metric(COSINE))
                .partitionKey("partition_id")
                .clusteringColumns(Sort.descending("row_id")));
        assertThat(getDatabase().tableExists(TABLE_CASSIO)).isTrue();
    }

    // ------------------------------------------
    // List Tables
    // ------------------------------------------

    @Test
    @Order(6)
    @DisplayName("06. Should list tables")
    public void shouldListTables() {
        getDatabase().listTables()
                .forEach(t -> log.info("Table: {} - pk={}",
                        t.getName(),
                        t.getDefinition().getPrimaryKey().getPartitionBy()));
        assertThat(getDatabase().listTables()).isNotEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("07. Should list table names")
    public void shouldListTableNames() {
        assertThat(getDatabase().listTableNames()).isNotNull();
        assertThat(getDatabase().listTableNames()).contains(TABLE_SIMPLE);
    }

    @Test
    @Order(8)
    @DisplayName("08. Should list indexes on table")
    public void shouldListIndexes() {
        for (TableIndexDescriptor<?> tid : getDatabase().getTable(TABLE_SIMPLE).listIndexes()) {
            log.info("Index: {}", tid.getName());
        }
    }

    @Test
    @Order(9)
    @DisplayName("09. Should access table and get definition")
    public void shouldAccessTableSimple() {
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isTrue();
        Table<Row> tableSimple = getDatabase().getTable(TABLE_SIMPLE);
        assertThat(tableSimple).isNotNull();
        assertThat(tableSimple.getTableName()).isEqualTo(TABLE_SIMPLE);

        TableDefinition desc = tableSimple.getDefinition();
        assertThat(desc).isNotNull();
        assertThat(desc.getPrimaryKey().getPartitionBy()).isEqualTo(List.of("email"));
    }

    // ------------------------------------------
    // InsertOne
    // ------------------------------------------

    @Test
    @Order(10)
    @DisplayName("10. Should insert one row in simple table")
    public void shouldInsertOneTableSimple() {
        Table<Row> table = getDatabase().getTable(TABLE_SIMPLE);
        Row row = new Row()
                .addBoolean("human", true)
                .addInt("age", 42)
                .addText("name", "John")
                .addText("country", "France")
                .addText("email", "cedrick@datastax.com");
        TableInsertOneResult res = table.insertOne(row);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isNotNull();
        assertThat(res.getInsertedIdAsRow().get("email")).isEqualTo("cedrick@datastax.com");
    }

    @Test
    @Order(11)
    @DisplayName("11. Should insert one row in composite table")
    public void shouldInsertOneTableComposite() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "John")
                .addText("id", "John");
        TableInsertOneResult res = table.insertOne(row);
        assertThat(res.getInsertedId().size()).isEqualTo(2);
        assertThat(res.getInsertedIdAsRow().getText("name")).isEqualTo("John");
    }

    @Test
    @Order(12)
    @DisplayName("12. Should insert one row using bean")
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
    @Order(13)
    @DisplayName("13. Should insert many rows")
    public void shouldInsertManyTableComposite() {
        Table<TableCompositeRow> table = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableInsertManyResult res = table.insertMany(List.of(
                        new TableCompositeRow(42, "InsertMany1", "Test"),
                        new TableCompositeRow(41, "InsertMany2", "Test")),
                new TableInsertManyOptions());
        assertThat(res).isNotNull();
        assertThat(res.getInsertedIds()).hasSize(2);
    }

    @Test
    @Order(14)
    @DisplayName("14. Should insert many with returnDocumentResponses")
    public void shouldInsertManyWithResponses() {
        // TODO FIX
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Row row1 = new Row().addInt("age", 22).addText("name", "John").addText("id", "Connor");
        Row row2 = new Row().addInt("age", 50).addText("name", "Sara").addText("id", "Connor");
        Row row3 = new Row().addInt("age", 50).addText("name", "Doctor").addText("id", "Silberman");

        TableInsertManyResult res = table.insertMany(
                List.of(row1, row2, row3),
                new TableInsertManyOptions()
                        .ordered(false)
                        .timeout(10000L)
                        .returnDocumentResponses(true));

        assertThat(res.getDocumentResponses()).isNotNull();
        assertThat(res.getPrimaryKeySchema()).isNotNull();
    }

    // ------------------------------------------
    // FindOne
    // ------------------------------------------

    @Test
    @Order(15)
    @DisplayName("15. Should find one row")
    public void shouldFindOneTableComposite() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Filter johnFilter = new Filter(Map.of("id", "Lunven", "name", "Cedrick"));
        Optional<Row> res = table.findOne(johnFilter,
                new TableFindOneOptions().projection(Projection.include("id", "age")));
        assertThat(res).isPresent();
    }

    // ------------------------------------------
    // DeleteOne
    // ------------------------------------------

    @Test
    @Order(16)
    @DisplayName("16. Should delete one row")
    public void shouldDeleteOneTableComposite() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);
        Filter johnFilter = new Filter(Map.of("id", "Lunven", "name", "Cedrick"));
        table.deleteOne(johnFilter);
        // Verify deletion
        Optional<Row> res = table.findOne(johnFilter);
        assertThat(res).isEmpty();
    }

    // ------------------------------------------
    // Insert and Find All Types
    // ------------------------------------------

    @Test
    @Order(17)
    @DisplayName("17. Should insert row with all column types")
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
                .addVarInt("p_varint", new BigInteger("444"))
                .addTinyInt("p_tinyint", (byte) 17)
                .addInet("p_inet", InetAddress.getByAddress(new byte[]{12, 34, 56, 78}))
                .addDouble("p_double", 987.6543d)
                .addFloat("p_float", 66.55f)
                .addFloat("p_float_nan", Float.NaN)
                .addTimeStamp("p_timestamp", Instant.now())
                .addTime("p_time", localTime)
                .addUUID("p_uuid", java.util.UUID.fromString("9c5b94b1-35ad-49bb-b118-8e8fc24abf80"))
                .addLocalDate("p_date", LocalDate.of(2015, 5, 3))
                .addDecimal("p_decimal", new BigDecimal("123.45"))
                .addVector("p_vector", new DataAPIVector(new float[]{.1f, 0.2f, 0.3f}))
                .addList("p_list_int", List.of(4, 17, 34))
                .addSet("p_set_int", Set.of(9, 81))
                .addDuration("p_duration", Duration.ofHours(12).plusMinutes(48))
                .addTableDuration("p_duration2", TableDuration.of(
                        Period.ofDays(3),
                        Duration.ofHours(12).plusMinutes(48)));

        getDatabase().getTable(TABLE_ALL_RETURNS).insertOne(row);
    }

    @Test
    @Order(18)
    @DisplayName("18. Should find row with all column types")
    public void shouldFindOneAllReturns() {
        Table<Row> tableAllReturns = getDatabase().getTable(TABLE_ALL_RETURNS);
        Filter filter = new Filter(Map.of(
                "p_ascii", "abc",
                "p_bigint", 10002L));
        Row row = tableAllReturns
                .findOne(filter)
                .orElseThrow(() -> new IllegalArgumentException("Row not found"));
        assertThat(row.getText("p_ascii")).isEqualTo("abc");
        assertThat(row.getBigInt("p_bigint")).isEqualTo(10002L);
    }

    // ------------------------------------------
    // Create Table from Bean
    // ------------------------------------------

    @Test
    @Order(19)
    @DisplayName("19. Should create table from bean definition")
    public void shouldCreateTableFromBeanDefinition() {
        getDatabase().createTable(TableCompositeAnnotatedRow.class, IF_NOT_EXISTS);
        assertThat(getDatabase().tableExists("table_composite_pk_annotated")).isTrue();
    }

    // ------------------------------------------
    // Alter Table
    // ------------------------------------------

    @Test
    @Order(20)
    @DisplayName("20. Should alter table - add and drop columns")
    public void shouldAlterAddColumns() {
        Table<Row> t = getDatabase().getTable(TABLE_SIMPLE);

        // Add text column
        t.alter(new AlterTableAddColumns().addColumnText("new_column"));
        assertThat(t.getDefinition().getColumns().containsKey("new_column")).isTrue();

        // Add vector column
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isFalse();
        t.alter(new AlterTableAddColumns().addColumnVector("vv",
                new TableColumnDefinitionVector().dimension(1024).metric(COSINE)));
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isTrue();

        // Add vectorize
        t.alter(new AlterTableAddVectorize().columns(
                Map.of("vv", new VectorServiceOptions()
                        .modelName("mistral-embed")
                        .provider("mistral"))));

        // Drop vectorize
        t.alter(new AlterTableDropVectorize("vv"));

        // Drop columns
        t.alter(new AlterTableDropColumns("vv", "new_column"));
        assertThat(t.getDefinition().getColumns().containsKey("vv")).isFalse();
        assertThat(t.getDefinition().getColumns().containsKey("new_column")).isFalse();
    }

    // ------------------------------------------
    // UpdateOne
    // ------------------------------------------

    @Test
    @Order(21)
    @DisplayName("21. Should update one row")
    public void shouldUpdateOne() {
        Table<Row> table = getDatabase().getTable(TABLE_COMPOSITE);

        Row row = new Row()
                .addInt("age", 42)
                .addText("name", "UpdateTest")
                .addText("id", "UpdateId");
        table.insertOne(row);

        Filter filter = new Filter(Map.of("id", "UpdateId", "name", "UpdateTest"));
        assertThat(table.findOne(filter)).isPresent();

        // Update the row
        table.updateOne(filter, new TableUpdateOperation().set("age", 43));

        // Verify update
        Optional<Row> updated = table.findOne(filter);
        assertThat(updated).isPresent();
        assertThat(updated.get().getInteger("age")).isEqualTo(43);
    }

    // ------------------------------------------
    // Find with Cursor
    // ------------------------------------------

    @Test
    @Order(22)
    @DisplayName("22. Should work with cursors and pagination")
    public void shouldWorkWithCursors() {
        Table<Row> tableCities = getDatabase().createTable("cities", new TableDefinition()
                .addColumnText("country")
                .addColumnText("city")
                .addColumnInt("population")
                .partitionKey("country")
                .clusteringColumns(Sort.ascending("city")), IF_NOT_EXISTS);
        tableCities.deleteAll();

        // Insert French cities
        List<Row> rowsFrance = new ArrayList<>();
        rowsFrance.add(new Row().addText("country", "france").addText("city", "paris").addInt("population", 2148271));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "marseille").addInt("population", 861635));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "lyon").addInt("population", 513275));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "toulouse").addInt("population", 471941));
        rowsFrance.add(new Row().addText("country", "france").addText("city", "nice").addInt("population", 341032));
        tableCities.insertMany(rowsFrance);

        // Insert English cities
        List<Row> rowsEngland = new ArrayList<>();
        rowsEngland.add(new Row().addText("country", "england").addText("city", "london").addInt("population", 8908081));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "birmingham").addInt("population", 1141816));
        rowsEngland.add(new Row().addText("country", "england").addText("city", "leeds").addInt("population", 789194));
        tableCities.insertMany(rowsEngland);

        // Test cursor with filter
        List<Row> frenchCities = tableCities
                .find(Filters.eq("country", "france"), new TableFindOptions())
                .toList();
        assertThat(frenchCities).hasSize(5);

        // Test findAll
        assertThat(tableCities.findAll().toList().size()).isGreaterThanOrEqualTo(8);
    }

    // ------------------------------------------
    // Work with returnedResponses
    // ------------------------------------------

    @Test
    @Order(23)
    @DisplayName("23. Should work with returnDocumentResponses disabled")
    public void shouldWorkWithReturnedResponses() {
        Table<TableCompositeRow> t = getDatabase().getTable(TABLE_COMPOSITE, TableCompositeRow.class);
        TableInsertManyResult res = t.insertMany(TableCompositeRowGenerator.generateUniqueRandomRows(3),
                new TableInsertManyOptions()
                        .ordered(false)
                        .returnDocumentResponses(false));
        assertThat(res.getInsertedIds().size()).isEqualTo(3);
        assertThat(res.getPrimaryKeySchema()).isNotNull();
        assertThat(res.getDocumentResponses().size()).isEqualTo(0);
    }

    // ------------------------------------------
    // DeleteMany with Options
    // ------------------------------------------

    @Test
    @Order(24)
    @DisplayName("24. Should delete many rows with options")
    public void shouldDeleteMany() {
        String tableName = "table_delete_many";
        getDatabase().dropTable(tableName, IF_EXISTS);

        Table<Row> table = getDatabase().createTable(tableName, new TableDefinition()
                .addColumnText("id")
                .addColumnInt("value")
                .partitionKey("id"), IF_NOT_EXISTS);

        // Insert rows in batches
        for (int batch = 0; batch < 10; batch++) {
            List<Row> rows = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                rows.add(new Row()
                        .addText("id", "id_" + (batch * 10 + i))
                        .addInt("value", batch * 10 + i));
            }
            table.insertMany(rows);
        }

        // Delete all rows with custom options (retries + timeout)
        TableDeleteManyOptions deleteManyOptions = new TableDeleteManyOptions()
                .httpClientOptions(new HttpClientOptions()
                        .httpRetries(3, Duration.ofMillis(100)))
                .timeout(30000L);
        table.deleteMany(null, deleteManyOptions);

        // Verify all rows deleted
        assertThat(table.findAll().toList()).isEmpty();
        log.info("Deleted all rows from '{}' with custom retry/timeout options", tableName);
    }

    // ------------------------------------------
    // Null collection columns
    // ------------------------------------------

    @Test
    @Order(25)
    public void should_findOne_with_null_collections() {
        String tableName = "table_null_collections";
        getDatabase().dropTable(tableName, IF_EXISTS);

        // Create table with UUID pk, a scalar, and LIST / SET / MAP columns
        Table<Row> table = getDatabase().createTable(tableName, new TableDefinition()
                .addColumn("id", TableColumnTypes.UUID)
                .addColumnText("label")
                .addColumnList("tags", TableColumnTypes.TEXT)
                .addColumnSet("categories", TableColumnTypes.TEXT)
                .addColumnMap("properties", TableColumnTypes.TEXT, TableColumnTypes.INT)
                .partitionKey("id"), IF_NOT_EXISTS);

        // Insert a row with ONLY the pk and scalar — list, set, map are omitted (null in Cassandra)
        UUID rowId = UUID.randomUUID();
        table.insertOne(new Row()
                .addUUID("id", rowId)
                .addText("label", "row_with_nulls"));

        // --- findOne: null collection columns should come back as empty, not null ---
        Optional<Row> result = table.findOne(Filters.eq("id", rowId));
        assertThat(result).isPresent();
        Row row = result.get();
        assertThat(row.getText("label")).isEqualTo("row_with_nulls");

        // LIST column → empty list (not null)
        Object tags = row.getColumnMap().get("tags");
        assertThat(tags).isNotNull().isInstanceOf(List.class);
        assertThat((List<?>) tags).isEmpty();

        // SET column → empty set (not null)
        Object categories = row.getColumnMap().get("categories");
        assertThat(categories).isNotNull().isInstanceOf(Set.class);
        assertThat((Set<?>) categories).isEmpty();

        // MAP column → empty map (not null)
        Object properties = row.getColumnMap().get("properties");
        assertThat(properties).isNotNull().isInstanceOf(Map.class);
        assertThat((Map<?, ?>) properties).isEmpty();

        log.info("findOne: null LIST/SET/MAP returned as empty collections");

        // --- findPage (via find cursor): same behavior ---
        List<Row> rows = table.find(Filters.eq("id", rowId)).toList();
        assertThat(rows).hasSize(1);
        Row pageRow = rows.get(0);
        assertThat(pageRow.getColumnMap().get("tags")).isNotNull().isInstanceOf(List.class);
        assertThat((List<?>) pageRow.getColumnMap().get("tags")).isEmpty();
        assertThat(pageRow.getColumnMap().get("categories")).isNotNull().isInstanceOf(Set.class);
        assertThat((Set<?>) pageRow.getColumnMap().get("categories")).isEmpty();
        assertThat(pageRow.getColumnMap().get("properties")).isNotNull().isInstanceOf(Map.class);
        assertThat((Map<?, ?>) pageRow.getColumnMap().get("properties")).isEmpty();

        log.info("find (cursor): null LIST/SET/MAP returned as empty collections");

        // --- Row with actual values should still come back correctly ---
        UUID rowId2 = UUID.randomUUID();
        table.insertOne(new Row()
                .addUUID("id", rowId2)
                .addText("label", "row_with_values")
                .addList("tags", List.of("java", "sdk"))
                .addSet("categories", Set.of("open-source", "database"))
                .addMap("properties", Map.of("priority", 1, "version", 2)));

        Optional<Row> result2 = table.findOne(Filters.eq("id", rowId2));
        assertThat(result2).isPresent();
        Row row2 = result2.get();
        @SuppressWarnings("unchecked")
        List<String> tagValues = (List<String>) row2.getColumnMap().get("tags");
        assertThat(tagValues).containsExactlyInAnyOrder("java", "sdk");
        // API returns SET as JSON array (ArrayList); verify contents via new Set
        @SuppressWarnings("unchecked")
        List<String> categoryRaw = (List<String>) row2.getColumnMap().get("categories");
        assertThat(new HashSet<>(categoryRaw)).containsExactlyInAnyOrder("open-source", "database");
        @SuppressWarnings("unchecked")
        Map<String, Integer> propValues = (Map<String, Integer>) row2.getColumnMap().get("properties");
        assertThat(propValues).containsEntry("priority", 1).containsEntry("version", 2);

        log.info("findOne: non-null LIST/SET/MAP returned with correct values");

        // Cleanup
        getDatabase().dropTable(tableName, IF_EXISTS);
    }
}
