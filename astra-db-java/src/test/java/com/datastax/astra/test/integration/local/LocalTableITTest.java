package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.query.Sorts;
import com.datastax.astra.client.tables.TableDefinition;
import com.datastax.astra.client.tables.TableDescriptor;
import com.datastax.astra.client.tables.columns.ColumnTypes;
import com.datastax.astra.client.tables.commands.TableInsertOneOptions;
import com.datastax.astra.client.tables.commands.TableInsertOneResult;
import com.datastax.astra.client.tables.index.IndexDefinition;
import com.datastax.astra.client.tables.index.IndexDefinitionOptions;
import com.datastax.astra.client.tables.index.IndexOptions;
import com.datastax.astra.client.tables.index.VectorIndexDefinition;
import com.datastax.astra.client.tables.index.VectorIndexDefinitionOptions;
import com.datastax.astra.client.tables.row.Row;
import com.datastax.astra.test.integration.AbstractTableITTest;
import com.datastax.astra.test.model.TableSimpleRow;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test Operation Locally with Docker and DSE/HCD
 */
public class LocalTableITTest extends AbstractTableITTest {

    public static final String TABLE_SIMPLE       = "table_simple";
    public static final String TABLE_COMPOSITE    = "table_composite_pk";
    public static final String TABLE_TYPES        = "table_types";
    public static final String TABLE_CASSIO       = "table_cassio";

    public static final String INDEX_COUNTRY      = "country_index";
    public static final String INDEX_VECTOR_TYPES = "idx_vector_types";

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
        getDatabase().dropTable(TABLE_TYPES);
        getDatabase().dropTable(TABLE_CASSIO);
        assertThat(getDatabase().tableExists(TABLE_SIMPLE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_COMPOSITE)).isFalse();
        assertThat(getDatabase().tableExists(TABLE_TYPES)).isFalse();
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
        getDatabase().createTable(TABLE_TYPES, new TableDefinition()
                        .addColumn("p_ascii", ColumnTypes.ASCII)
                        .addColumn("p_boolean", ColumnTypes.BOOLEAN)
                        .addColumn("p_tinyint", ColumnTypes.TINYINT)
                        .addColumn("p_smallint", ColumnTypes.SMALLINT)
                        .addColumn("p_duration", ColumnTypes.DURATION)
                        .addColumn("p_inet", ColumnTypes.INET)
                        .addColumn("p_blob", ColumnTypes.BLOB)
                        .addColumn("p_double", ColumnTypes.DOUBLE)
                        .addColumn("p_float", ColumnTypes.FLOAT)
                        .addColumn("p_varint", ColumnTypes.VARINT)
                        .addColumn("p_decimal", ColumnTypes.DECIMAL)
                        .addColumn("p_text", ColumnTypes.TEXT)
                        .addColumn("p_time", ColumnTypes.TIME)
                        .addColumn("p_date", ColumnTypes.DATE)
                        .addColumn("p_int", ColumnTypes.INT)
                        .addColumn("p_bigint", ColumnTypes.BIGINT)
                        .addColumn("p_uuid", ColumnTypes.UUID)
                        .addColumnList("p_list", ColumnTypes.TEXT)
                        .addColumnSet("p_set", ColumnTypes.INT)
                        .addColumnMap("p_map", ColumnTypes.TEXT, ColumnTypes.TEXT)
                        .addColumnVector("vector", 1536, SimilarityMetric.COSINE)
                        .withPartitionKey("p_uuid")
                        .withClusteringColumns(Sorts.ascending("p_text"), Sorts.descending("p_int")));
        assertThat(getDatabase().tableExists(TABLE_TYPES)).isTrue();

        getDatabase().getTable(TABLE_TYPES)
                     .createVectorIndex(INDEX_VECTOR_TYPES, new VectorIndexDefinition()
                        .column("vector")
                        .options(new VectorIndexDefinitionOptions().metric(SimilarityMetric.COSINE)));
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
                        .addColumnVector("vector", 1536, SimilarityMetric.COSINE)
                        .withPartitionKey("partition_id")
                        .withClusteringColumns(Sorts.descending("row_id")));
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

        Table<TableSimpleRow> table2 = getDatabase().getTable(TableSimpleRow.class);
        table2.insertOne(new TableSimpleRow());

    }

}
