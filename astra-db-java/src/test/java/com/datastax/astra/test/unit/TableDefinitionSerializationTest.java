package com.datastax.astra.test.unit;

import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import org.junit.jupiter.api.Test;

public class TableDefinitionSerializationTest {

    RowSerializer ROW_SERIALIZER = new RowSerializer();

    private void log(TableIndexDefinition idx) {
        System.out.println(ROW_SERIALIZER.marshall(idx));
    }

    @Test
    public void should_serialize_table_index_column_definition() {
        log(new TableIndexDefinition().column("scalar_col"));
        log(new TableIndexDefinition().column("map_col1", TableIndexMapTypes.KEYS));
        log(new TableIndexDefinition().column("map_col2", TableIndexMapTypes.VALUES));
        log(new TableIndexDefinition().column("map_col2", TableIndexMapTypes.ENTRIES));
    }
}
