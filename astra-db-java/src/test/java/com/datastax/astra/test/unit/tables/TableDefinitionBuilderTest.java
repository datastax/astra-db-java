package com.datastax.astra.test.unit.tables;

import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TableDefinition fluent builder.
 */
class TableDefinitionBuilderTest {

    // --------------------------------------------------
    // Column type helpers
    // --------------------------------------------------

    @Test
    void shouldAddTextColumn() {
        TableDefinition def = new TableDefinition().addColumnText("name");
        assertThat(def.getColumns()).containsKey("name");
        assertThat(def.getColumns().get("name").getType()).isEqualTo(TableColumnTypes.TEXT);
    }

    @Test
    void shouldAddIntColumn() {
        TableDefinition def = new TableDefinition().addColumnInt("age");
        assertThat(def.getColumns().get("age").getType()).isEqualTo(TableColumnTypes.INT);
    }

    @Test
    void shouldAddBooleanColumn() {
        TableDefinition def = new TableDefinition().addColumnBoolean("active");
        assertThat(def.getColumns().get("active").getType()).isEqualTo(TableColumnTypes.BOOLEAN);
    }

    @Test
    void shouldAddTimestampColumn() {
        TableDefinition def = new TableDefinition().addColumnTimestamp("created_at");
        assertThat(def.getColumns().get("created_at").getType()).isEqualTo(TableColumnTypes.TIMESTAMP);
    }

    @Test
    void shouldAddBigIntColumn() {
        TableDefinition def = new TableDefinition().addColumnBigInt("counter");
        assertThat(def.getColumns().get("counter").getType()).isEqualTo(TableColumnTypes.BIGINT);
    }

    @Test
    void shouldAddBlobColumn() {
        TableDefinition def = new TableDefinition().addColumnBlob("data");
        assertThat(def.getColumns().get("data").getType()).isEqualTo(TableColumnTypes.BLOB);
    }

    @Test
    void shouldAddUuidColumn() {
        TableDefinition def = new TableDefinition().addColumnUuid("id");
        assertThat(def.getColumns().get("id").getType()).isEqualTo(TableColumnTypes.UUID);
    }

    @Test
    void shouldAddAsciiColumn() {
        TableDefinition def = new TableDefinition().addColumnAscii("code");
        assertThat(def.getColumns().get("code").getType()).isEqualTo(TableColumnTypes.ASCII);
    }

    @Test
    void shouldAddColumnWithExplicitType() {
        TableDefinition def = new TableDefinition().addColumn("score", TableColumnTypes.DOUBLE);
        assertThat(def.getColumns().get("score").getType()).isEqualTo(TableColumnTypes.DOUBLE);
    }

    // --------------------------------------------------
    // Collection columns (list, set, map)
    // --------------------------------------------------

    @Test
    void shouldAddListColumn() {
        TableDefinition def = new TableDefinition().addColumnList("tags", TableColumnTypes.TEXT);
        assertThat(def.getColumns()).containsKey("tags");
        assertThat(def.getColumns().get("tags").getType()).isEqualTo(TableColumnTypes.LIST);
    }

    @Test
    void shouldAddSetColumn() {
        TableDefinition def = new TableDefinition().addColumnSet("ids", TableColumnTypes.UUID);
        assertThat(def.getColumns()).containsKey("ids");
        assertThat(def.getColumns().get("ids").getType()).isEqualTo(TableColumnTypes.SET);
    }

    @Test
    void shouldAddMapColumn() {
        TableDefinition def = new TableDefinition().addColumnMap("meta", TableColumnTypes.TEXT, TableColumnTypes.INT);
        assertThat(def.getColumns()).containsKey("meta");
        assertThat(def.getColumns().get("meta").getType()).isEqualTo(TableColumnTypes.MAP);
    }

    // --------------------------------------------------
    // Vector column
    // --------------------------------------------------

    @Test
    void shouldAddVectorColumn() {
        TableDefinition def = new TableDefinition()
                .addColumnVector("vec", new TableColumnDefinitionVector().dimension(3).metric(COSINE));
        assertThat(def.getColumns()).containsKey("vec");
        assertThat(def.getColumns().get("vec").getType()).isEqualTo(TableColumnTypes.VECTOR);
    }

    // --------------------------------------------------
    // UDT columns
    // --------------------------------------------------

    @Test
    void shouldAddUserDefinedTypeColumn() {
        TableDefinition def = new TableDefinition()
                .addColumnUserDefinedType("address", "address_udt");
        assertThat(def.getColumns()).containsKey("address");
        assertThat(def.getColumns().get("address").getType()).isEqualTo(TableColumnTypes.USERDEFINED);
    }

    @Test
    void shouldAddListUserDefinedTypeColumn() {
        TableDefinition def = new TableDefinition()
                .addColumnListUserDefinedType("addresses", "address_udt");
        assertThat(def.getColumns()).containsKey("addresses");
    }

    @Test
    void shouldAddSetUserDefinedTypeColumn() {
        TableDefinition def = new TableDefinition()
                .addColumnSetUserDefinedType("address_set", "address_udt");
        assertThat(def.getColumns()).containsKey("address_set");
    }

    @Test
    void shouldAddMapUserDefinedTypeColumn() {
        TableDefinition def = new TableDefinition()
                .addColumnMapUserDefinedType("address_map", "address_udt", TableColumnTypes.TEXT);
        assertThat(def.getColumns()).containsKey("address_map");
    }

    // --------------------------------------------------
    // Primary key
    // --------------------------------------------------

    @Test
    void shouldSetPartitionKey() {
        TableDefinition def = new TableDefinition()
                .addColumnText("id")
                .partitionKey("id");
        assertThat(def.getPrimaryKey().getPartitionBy()).containsExactly("id");
    }

    @Test
    void shouldSetCompositePartitionKey() {
        TableDefinition def = new TableDefinition()
                .addColumnText("tenant")
                .addColumnText("id")
                .partitionKey("tenant", "id");
        assertThat(def.getPrimaryKey().getPartitionBy()).containsExactly("tenant", "id");
    }

    @Test
    void shouldRejectPartitionKeyForMissingColumn() {
        TableDefinition def = new TableDefinition().addColumnText("id");
        assertThatThrownBy(() -> def.partitionKey("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void shouldSetClusteringColumns() {
        TableDefinition def = new TableDefinition()
                .addColumnText("id")
                .addColumnInt("round")
                .partitionKey("id")
                .clusteringColumns(Sort.ascending("round"));
        assertThat(def.getPrimaryKey().getPartitionSort()).containsKey("round");
        assertThat(def.getPrimaryKey().getPartitionSort().get("round")).isEqualTo(1);
    }

    @Test
    void shouldSetDescendingClusteringColumn() {
        TableDefinition def = new TableDefinition()
                .addColumnText("id")
                .addColumnTimestamp("ts")
                .partitionKey("id")
                .clusteringColumns(Sort.descending("ts"));
        assertThat(def.getPrimaryKey().getPartitionSort().get("ts")).isEqualTo(-1);
    }

    @Test
    void shouldRejectClusteringColumnForMissingColumn() {
        TableDefinition def = new TableDefinition().addColumnText("id").partitionKey("id");
        assertThatThrownBy(() -> def.clusteringColumns(Sort.ascending("missing")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    // --------------------------------------------------
    // Fluent chaining
    // --------------------------------------------------

    @Test
    void shouldChainMultipleColumns() {
        TableDefinition def = new TableDefinition()
                .addColumnText("match_id")
                .addColumnInt("round")
                .addColumnVector("m_vector", new TableColumnDefinitionVector().dimension(3).metric(COSINE))
                .addColumn("score", TableColumnTypes.INT)
                .addColumn("when", TableColumnTypes.TIMESTAMP)
                .addColumnSet("fighters", TableColumnTypes.UUID)
                .addPartitionBy("match_id")
                .addPartitionSort(Sort.ascending("round"));

        assertThat(def.getColumns()).hasSize(6);
        assertThat(def.getPrimaryKey().getPartitionBy()).containsExactly("match_id");
        assertThat(def.getPrimaryKey().getPartitionSort()).containsKey("round");
    }

    // --------------------------------------------------
    // toString / serialization
    // --------------------------------------------------

    @Test
    void shouldSerializeToString() {
        TableDefinition def = new TableDefinition()
                .addColumnText("id")
                .addColumnInt("value")
                .partitionKey("id");
        String json = def.toString();
        assertThat(json).contains("id");
        assertThat(json).contains("value");
    }

    // --------------------------------------------------
    // TableColumnTypes enum
    // --------------------------------------------------

    @Test
    void shouldHaveExpectedColumnTypeValues() {
        assertThat(TableColumnTypes.TEXT.getValue()).isEqualTo("text");
        assertThat(TableColumnTypes.INT.getValue()).isEqualTo("int");
        assertThat(TableColumnTypes.BOOLEAN.getValue()).isEqualTo("boolean");
        assertThat(TableColumnTypes.UUID.getValue()).isEqualTo("uuid");
        assertThat(TableColumnTypes.VECTOR.getValue()).isEqualTo("vector");
        assertThat(TableColumnTypes.BIGINT.getValue()).isEqualTo("bigint");
        assertThat(TableColumnTypes.DOUBLE.getValue()).isEqualTo("double");
        assertThat(TableColumnTypes.FLOAT.getValue()).isEqualTo("float");
        assertThat(TableColumnTypes.TIMESTAMP.getValue()).isEqualTo("timestamp");
        assertThat(TableColumnTypes.BLOB.getValue()).isEqualTo("blob");
        assertThat(TableColumnTypes.ASCII.getValue()).isEqualTo("ascii");
        assertThat(TableColumnTypes.DECIMAL.getValue()).isEqualTo("decimal");
        assertThat(TableColumnTypes.SMALLINT.getValue()).isEqualTo("smallint");
        assertThat(TableColumnTypes.TINYINT.getValue()).isEqualTo("tinyint");
        assertThat(TableColumnTypes.VARINT.getValue()).isEqualTo("varint");
        assertThat(TableColumnTypes.LIST.getValue()).isEqualTo("list");
        assertThat(TableColumnTypes.SET.getValue()).isEqualTo("set");
        assertThat(TableColumnTypes.MAP.getValue()).isEqualTo("map");
    }
}
