package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableDescriptor;
import com.datastax.astra.client.tables.TableOptions;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.commands.TableInsertOneResult;
import com.datastax.astra.client.tables.index.IndexDefinition;
import com.datastax.astra.client.tables.index.IndexDefinitionOptions;
import com.datastax.astra.client.tables.index.IndexOptions;
import com.datastax.astra.client.tables.index.VectorIndexDefinition;
import com.datastax.astra.client.tables.index.VectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.index.VectorIndexOptions;
import com.datastax.astra.client.tables.mapping.IntrospectedBean;
import com.datastax.astra.client.tables.mapping.IntrospectedField;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.test.integration.AbstractTableITTest;
import com.datastax.astra.test.model.TableSimpleRow;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
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
        getDatabase().dropTable(TABLE_COMPOSITE);
        getDatabase().dropTable(TABLE_ALL_RETURNS);
        getDatabase().dropTable(TABLE_CASSIO);
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_COMPOSITE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_ALL_RETURNS)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_CASSIO)).isFalse();
    }

    @Test
    @Order(2)
    public void shouldCreateTableSimple() {
        // Simple
        Table<Row> tableSimple = getDatabase()
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
    public void shouldCreateTableAllTypes() {
        Table<Row> tableAllReturns = getDatabase().createTable(TABLE_ALL_RETURNS, new TableDefinition()
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
                //.addColumnMap("p_map_text_text", ColumnTypes.TEXT, ColumnTypes.TEXT)
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
        getDatabase().dropTable(TABLE_CASSIO);
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
        Table<Row> tableSimple = getDatabase().getTable(tableName);
        assertThat(tableSimple).isNotNull();
        assertThat(tableSimple.getTableName()).isEqualTo(tableName);
        // Accessing the Table Definitions
        TableDescriptor desc = tableSimple.getDefinition();
        assertThat(desc).isNotNull();
        assertThat(desc.getName()).isEqualTo(tableName);
        assertThat(desc.getDefinition()).isNotNull();
        assertThat(desc.getDefinition().getPrimaryKey().getPartitionBy()).isEqualTo(List.of("email"));
    }

    @Test
    public void shouldMapTableSimple() {
        Table<Row> table = getDatabase().getTable(TABLE_SIMPLE);
        Row row = new Row()
                .addBoolean("human", true)
                .addInt("age", 42)
                .addText("name", "John")
                .addText("country", "France")
                .addText("email",  "cedrick@datastax.com");
        TableInsertOneResult res = table.insertOne(row);
        System.out.println(res.insertedIds());

        //Table<TableSimpleRow> table2 = getDatabase().getTable(TableSimpleRow.class);
        //table2.insertOne(new TableSimpleRow());

    }

    @Test
    public void shouldIntrospectBean() {
        IntrospectedBean<TableSimpleRow> rowDecorator = new IntrospectedBean<>(TableSimpleRow.class);
        System.out.println("Table Name: " + rowDecorator.getName());
        for (IntrospectedField field : rowDecorator.getFields().values()) {
            System.out.println("Field: " + field.getName() + ", Type: " + field.getType().getName());
        }
    }

    @Test
    public void shouldInsertOneAllReturns() {
        Table<Row> tableAllReturns = getDatabase().getTable(TABLE_ALL_RETURNS);
        Row row = new Row()
                .addText("p_ascii", "abc")
                .addBigInt("p_bigint", 10002L)
                .addInt("p_int", 987)
                .addBoolean("p_boolean", false)
                .addText("p_text", "Ålesund")
                .addDouble("p_double_pinf", Double.MAX_VALUE);
                //.addBlob("p_blob", "blob".getBytes());
                //.addDate("p_date", LocalDate.now())
                //.addDecimal("p_decimal", new BigDecimal("42.42"))
                //.addByte("p_tinyint", (byte) 42)
                //.addDouble("p_double", 987.6543)
                //.addDuration("p_duration", Duration.ofSeconds(42))
                //.addFloat("p_float", 42.42f)
                //.addInt("p_int", 42)
                //.addInet("p_inet", InetAddress.getByName("
        getDatabase().getTable(TABLE_ALL_RETURNS).insertOne(row);
    }

}
