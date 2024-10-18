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
        if (getDatabase().tableExists("table_simple")) {
            getDatabase()
                    .getTable("table_simple")
                    .dropIndex("country_index");
            getDatabase().dropTable("table_simple");
        }
        getDatabase().dropTable("table_composite_pk");
        getDatabase().dropTable("table_types");
        getDatabase().dropTable("table_cassio");
        assertThat(getDatabase().tableExists("table_simple")).isFalse();
        assertThat(getDatabase().tableExists("table_composite_pk")).isFalse();
        assertThat(getDatabase().tableExists("table_types")).isFalse();
        assertThat(getDatabase().tableExists("table_cassio")).isFalse();
    }

    @Test
    @Order(2)
    public void shouldCreateTableSimple() {
        // Simple
        Table<Row> tableSimple = getDatabase().createTable(new TableDescriptor().name("table_simple")
                .definition(new TableDefinition()
                        .addColumn("email", ColumnTypes.TEXT)
                        .addColumn("age", ColumnTypes.INT)
                        .addColumn("name", ColumnTypes.TEXT)
                        .addColumn("country", ColumnTypes.TEXT)
                        .addColumn("human", ColumnTypes.BOOLEAN)
                        .withPartitionKey("email")));
        assertThat(getDatabase().tableExists("table_simple")).isTrue();

        // Create Index Simple
        tableSimple.createIndex(new IndexDescriptor()
                .name("country_index")
                .definition(new IndexDefinition()
                    .column("country")
                    .options(new IndexDefinitionOptions()
                            .ascii(true)
                            .caseSensitive(true)
                            .normalize(true))), new IndexOptions().ifNotExists());

    }

    @Test
    @Order(2)
    public void shouldDeleteIndexSimple() {
    }

    @Test
    @Order(3)
    public void shouldCreateTableComposite() {
        getDatabase().createTable(new TableDescriptor().name("table_composite_pk")
                .definition(new TableDefinition()
                        .addColumn("id", ColumnTypes.TEXT)
                        .addColumn("age", ColumnTypes.INT)
                        .addColumn("name", ColumnTypes.TEXT)
                        .withPartitionKey("id", "name")));
        assertThat(getDatabase().tableExists("table_composite_pk")).isTrue();
    }

    @Test
    @Order(4)
    public void shouldCreateTableAllTypes() {
        getDatabase().createTable(new TableDescriptor().name("table_types")
                .definition(new TableDefinition()
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
                        .withClusteringColumns(Sorts.ascending("p_text"), Sorts.descending("p_int"))));
        assertThat(getDatabase().tableExists("table_types")).isTrue();
    }

    @Test
    @Order(5)
    public void shouldCreateTableAllCassio() {
        getDatabase().dropTable("table_cassio");
        assertThat(getDatabase().tableExists("table_cassio")).isFalse();
        getDatabase().createTable(new TableDescriptor().name("table_cassio")
                .definition(new TableDefinition()
                        .addColumn("partition_id", ColumnTypes.TEXT)
                        .addColumn("attributes_blob", ColumnTypes.TEXT)
                        .addColumn("body_blob", ColumnTypes.TEXT)
                        .addColumn("row_id", ColumnTypes.UUID)
                        .addColumnMap("metadata_s", ColumnTypes.TEXT, ColumnTypes.TEXT)
                        .addColumnVector("vector", 1536, SimilarityMetric.COSINE)
                        .withPartitionKey("partition_id")
                        .withClusteringColumns(Sorts.descending("row_id"))));
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
