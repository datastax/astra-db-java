package com.datastax.astra.test.unit.tables;

import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TableRegularIndexDefinition serialization.
 */
class TableDefinitionSerializationTest {

    private final RowSerializer serializer = new RowSerializer();

    @Test
    void shouldSerializeScalarColumnIndex() {
        TableRegularIndexDefinition idx = new TableRegularIndexDefinition().column("scalar_col");
        String json = serializer.marshall(idx);
        assertThat(json).contains("scalar_col");
    }

    @Test
    void shouldSerializeMapKeysIndex() {
        TableRegularIndexDefinition idx = new TableRegularIndexDefinition()
                .column("map_col", TableIndexMapTypes.KEYS);
        String json = serializer.marshall(idx);
        assertThat(json).contains("map_col");
        assertThat(json).contains("keys");
    }

    @Test
    void shouldSerializeMapValuesIndex() {
        TableRegularIndexDefinition idx = new TableRegularIndexDefinition()
                .column("map_col", TableIndexMapTypes.VALUES);
        String json = serializer.marshall(idx);
        assertThat(json).contains("map_col");
        assertThat(json).contains("values");
    }

    @Test
    void shouldSerializeMapEntriesIndex() {
        TableRegularIndexDefinition idx = new TableRegularIndexDefinition()
                .column("map_col", TableIndexMapTypes.ENTRIES);
        String json = serializer.marshall(idx);
        assertThat(json).contains("map_col");
    }
}
