package com.datastax.astra.test.integration.hcd;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.commands.options.DropTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.datastax.astra.client.tables.definition.columns.TableColumnTypes.TEXT;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "ASTRA_DB_JAVA_TEST_ENV", matches = "xxx")
public class Local_21_TableCollectionIndexTest {

    static Database database;

    protected static Database getDatabase() {
        if (database == null) {
            database = DataAPIClients.localDbWithDefaultKeyspace();
        }
        return database;
    }

    @Test
    @Order(1)
    public void should_create_yuki_table() {
        Database db = getDatabase();
        db.dropTable("mapsetlist", DropTableOptions.IF_EXISTS);
        db.createTable("mapsetlist", new TableDefinition()
                .addColumnText("name")
                .addColumnInt("age")
                .addColumnText("textcolumn")
                .addColumnMap("mapcolumn", TEXT, TEXT)
                .addColumnSet("setcolumn", TEXT)
                .addColumnList("listcolumn", TEXT)
                        .addColumnVector("vectorcolumn", new TableColumnDefinitionVector()
                                .dimension(1536).metric(SimilarityMetric.DOT_PRODUCT))
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
                .createIndex("idx_set_column", new TableRegularIndexDefinition()
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
                .createIndex("idx_list_column", new TableRegularIndexDefinition()
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
                .createIndex("idx_mapColumn_entries", new TableRegularIndexDefinition()
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
                .createIndex("idx_mapColumn_keys", new TableRegularIndexDefinition()
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
                .createIndex("idx_mapColumn_values", new TableRegularIndexDefinition()
                                .column("mapcolumn", TableIndexMapTypes.VALUES)
                                .ascii(true).normalize(true).caseSensitive(true),
                        CreateIndexOptions.IF_NOT_EXISTS);

        db.getTable("mapsetlist")
                .createVectorIndex("idx_vector_column",
                        new TableVectorIndexDefinition().column("vectorcolumn")
                                .metric(SimilarityMetric.DOT_PRODUCT), CreateVectorIndexOptions.IF_NOT_EXISTS);
    }

    @Test
    @Order(2)
    public void should_list_indexes() {
        Database db = getDatabase();
        db.getTable("mapsetlist").listIndexes().forEach(System.out::println);
    }


}
