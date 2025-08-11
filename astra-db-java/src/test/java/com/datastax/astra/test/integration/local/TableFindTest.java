package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableIndexOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinitionOptions;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.test.integration.AbstractTableITTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static com.datastax.astra.client.tables.commands.options.DropTableOptions.IF_EXISTS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TableFindTest extends AbstractTableITTest {

    public static final String TABLE_SIMPLE = "table_simple";
    // Those environment variables are need
    // TOKEN
    // DB URL
    // ENV

    @Test
    @Order(1)
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

    }
}
