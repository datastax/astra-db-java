package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.Table;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.client.model.query.Sorts;
import com.datastax.astra.client.model.tables.columns.ColumnTypes;
import com.datastax.astra.client.model.tables.index.IndexDefinition;
import com.datastax.astra.client.model.tables.index.IndexDefinitionOptions;
import com.datastax.astra.client.model.tables.index.IndexDescriptor;
import com.datastax.astra.client.model.tables.index.IndexOptions;
import com.datastax.astra.client.model.tables.index.VectorIndexDefinition;
import com.datastax.astra.client.model.tables.index.VectorIndexDefinitionOptions;
import com.datastax.astra.client.model.tables.index.VectorIndexDescriptor;
import com.datastax.astra.client.model.tables.row.Row;
import com.datastax.astra.client.model.tables.TableDefinition;
import com.datastax.astra.client.model.tables.TableDescriptor;
import com.datastax.astra.test.integration.AbstractTableITTest;
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

    protected static String TABLE_SIMPLE       = "table_simple";
    protected static String TABLE_COMPOSITE    = "table_composite_pk";
    protected static String TABLE_TYPES        = "table_types";
    protected static String TABLE_CASSIO       = "table_cassio";

    protected static String INDEX_COUNTRY      = "country_index";
    protected static String INDEX_VECTOR_TYPES = "idx_vector_types";

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

}
