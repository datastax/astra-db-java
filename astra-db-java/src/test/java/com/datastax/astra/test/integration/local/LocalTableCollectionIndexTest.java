package com.datastax.astra.test.integration.local;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.client.tables.definition.columns.ColumnTypes.TEXT;

public class LocalTableCollectionIndexTest {

    static Database database;

    protected static Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.localDbWithDefaultKeyspace();
        }
        return database;
    }

    @Test
    public void should_create_yuki_table() {
        Database db = getDatabase();

        db.dropTable("mapsetlist", DropTableOptions.IF_EXISTS);
        db.createTable("mapsetlist", new TableDefinition()
                .addColumnText("name")
                .addColumnInt("age")
                .addColumnMap("mapcolumn", TEXT, TEXT)
                .addColumnSet("setcolumn", TEXT)
                .addColumnList("listcolumn", TEXT)
                .partitionKey("name"));

        /**
         * Index on Set.
         *
         * {
         *   "createIndex": {
         *     "name": "idx_set_column",
         *     "definition": {
         *       "column": "setcolumn",
         *       "options": {
         *         "caseSensitive": true,
         *         "normalize": true,
         *         "ascii": true
         *       }
         *     }
         *   }
         * }
         */
        db.getTable("mapsetlist")
                .createIndex("idx_set_column", new TableIndexDefinition()
                                .column("setcolumn")
                                .ascii(true).normalize(true).caseSensitive(true),
                        CreateIndexOptions.IF_NOT_EXISTS);

        /*
         * Index on List.
         *
         * {
         *   "createIndex": {
         *     "name": "idx_list_column",
         *     "definition": {
         *       "column": "listcolumn",
         *       "options": {
         *         "caseSensitive": true,
         *         "normalize": true,
         *         "ascii": true
         *       }
         *     }
         *   }
         * }
         */
        db.getTable("mapsetlist")
                .createIndex("idx_list_column", new TableIndexDefinition()
                                .column("listcolumn")
                                .ascii(true).normalize(true).caseSensitive(true),
                        CreateIndexOptions.IF_NOT_EXISTS);

        /* Index on Map (Entries, default)
         * {
         *   "createIndex": {
         *     "name": "idx_mapColumn_entries",
         *     "definition": {
         *       "column": "mapColumn"
         *     }
         *   }
         * }
         */
        db.getTable("mapsetlist")
                .createIndex("idx_mapColumn_entries", new TableIndexDefinition()
                                .column("mapcolumn")
                                // options are here not accepted
                                /*.ascii(true).normalize(true).caseSensitive(true)*/,
                        CreateIndexOptions.IF_NOT_EXISTS);

        /*
         * Index on Map (keys).
         * <code>
         * {
         *   "createIndex": {
         *     "name": "idx_mapColumn_keys",
         *     "definition": {
         *       "column": { "mapColumn": "$keys" },
         *       "options": {
         *         "caseSensitive": true,
         *         "normalize": true,
         *         "ascii": true
         *       }
         *     }
         *   }
         * }
         * </code>
         */
        db.getTable("mapsetlist")
                .createIndex("idx_mapColumn_keys", new TableIndexDefinition()
                                .column("mapcolumn", TableIndexMapTypes.KEYS)
                                .ascii(true).normalize(true).caseSensitive(true),
                        CreateIndexOptions.IF_NOT_EXISTS);

        /*
         * Index on Map (values).
         *
         * {
         *   "createIndex": {
         *     "name": "idx_mapColumn_values",
         *     "definition": {
         *       "column": { "mapColumn": "$values" },
         *       "options": {
         *         "caseSensitive": true,
         *         "normalize": true,
         *         "ascii": true
         *       }
         *     }
         *   }
         * }
         */
        db.getTable("mapsetlist")
                .createIndex("idx_mapColumn_values", new TableIndexDefinition()
                                .column("mapcolumn", TableIndexMapTypes.VALUES)
                                .ascii(true).normalize(true).caseSensitive(true),
                        CreateIndexOptions.IF_NOT_EXISTS);

    }

    @Test
    public void should_list_indexes() {
        Database db = getDatabase();
        db.getTable("mapsetlist").listIndexes().forEach(System.out::println);
    }


}
