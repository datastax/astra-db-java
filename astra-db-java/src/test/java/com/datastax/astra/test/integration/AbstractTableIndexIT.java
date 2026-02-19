package com.datastax.astra.test.integration;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateIndexOptions;
import com.datastax.astra.client.tables.commands.options.CreateVectorIndexOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.indexes.TableIndexDescriptor;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.datastax.astra.client.tables.definition.indexes.TableRegularIndexDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableVectorIndexDefinition;
import com.datastax.astra.client.tables.definition.rows.Row;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static com.datastax.astra.client.tables.commands.options.CreateTableOptions.IF_NOT_EXISTS;
import static com.datastax.astra.client.tables.commands.options.DropTableOptions.IF_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for Table Index integration tests.
 * Tests creation of various index types: regular, map (entries/keys/values),
 * set, list, text, and vector indexes.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableIndexIT extends AbstractDataAPITest {

    static final String TABLE_COLLECTION_INDEX = "table_collection_index";

    @BeforeAll
    void setupCleanKeyspace() {
        dropAllCollections();
        dropAllTables();
    }
    static final String TABLE_TEXT_INDEX = "table_text_index";

    // ------------------------------------------
    // Setup: create table with collection columns
    // ------------------------------------------

    @Test
    @Order(1)
    @DisplayName("01. Should create table with map, set, list and vector columns")
    public void shouldCreateTableWithCollectionColumns() {
        getDatabase().dropTable(TABLE_COLLECTION_INDEX, IF_EXISTS);

        getDatabase().createTable(TABLE_COLLECTION_INDEX, new TableDefinition()
                .addColumnText("name")
                .addColumnInt("age")
                .addColumnText("textcolumn")
                .addColumnMap("mapcolumn", TableColumnTypes.TEXT, TableColumnTypes.TEXT)
                .addColumnSet("setcolumn", TableColumnTypes.TEXT)
                .addColumnList("listcolumn", TableColumnTypes.TEXT)
                .addColumnVector("vectorcolumn", new TableColumnDefinitionVector().dimension(1536))
                .partitionKey("name"), IF_NOT_EXISTS);

        assertThat(getDatabase().tableExists(TABLE_COLLECTION_INDEX)).isTrue();
    }

    // ------------------------------------------
    // Set Index
    // ------------------------------------------

    @Test
    @Order(2)
    @DisplayName("02. Should create index on set column")
    public void shouldCreateSetIndex() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        table.createIndex("idx_set_column", new TableRegularIndexDefinition()
                        .column("setcolumn")
                        .ascii(true).normalize(true).caseSensitive(true),
                CreateIndexOptions.IF_NOT_EXISTS);
        log.info("Created index on set column 'setcolumn'");
    }

    // ------------------------------------------
    // List Index
    // ------------------------------------------

    @Test
    @Order(3)
    @DisplayName("03. Should create index on list column")
    public void shouldCreateListIndex() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        table.createIndex("idx_list_column", new TableRegularIndexDefinition()
                        .column("listcolumn")
                        .ascii(true).normalize(true).caseSensitive(true),
                CreateIndexOptions.IF_NOT_EXISTS);
        log.info("Created index on list column 'listcolumn'");
    }

    // ------------------------------------------
    // Map Index (entries - default)
    // ------------------------------------------

    @Test
    @Order(4)
    @DisplayName("04. Should create index on map column (entries, default)")
    public void shouldCreateMapEntriesIndex() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        // Map entries index does not accept options
        table.createIndex("idx_mapcolumn_entries", new TableRegularIndexDefinition()
                        .column("mapcolumn"),
                CreateIndexOptions.IF_NOT_EXISTS);
        log.info("Created map entries index on 'mapcolumn'");
    }

    // ------------------------------------------
    // Map Index (keys)
    // ------------------------------------------

    @Test
    @Order(5)
    @DisplayName("05. Should create index on map column (keys)")
    public void shouldCreateMapKeysIndex() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        table.createIndex("idx_mapcolumn_keys", new TableRegularIndexDefinition()
                        .column("mapcolumn", TableIndexMapTypes.KEYS)
                        .ascii(true).normalize(true).caseSensitive(true),
                CreateIndexOptions.IF_NOT_EXISTS);
        log.info("Created map keys index on 'mapcolumn'");
    }

    // ------------------------------------------
    // Map Index (values)
    // ------------------------------------------

    @Test
    @Order(6)
    @DisplayName("06. Should create index on map column (values)")
    public void shouldCreateMapValuesIndex() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        table.createIndex("idx_mapcolumn_values", new TableRegularIndexDefinition()
                        .column("mapcolumn", TableIndexMapTypes.VALUES)
                        .ascii(true).normalize(true).caseSensitive(true),
                CreateIndexOptions.IF_NOT_EXISTS);
        log.info("Created map values index on 'mapcolumn'");
    }

    // ------------------------------------------
    // Vector Index
    // ------------------------------------------

    @Test
    @Order(7)
    @DisplayName("07. Should create vector index")
    public void shouldCreateVectorIndex() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        table.createVectorIndex("idx_vector_column",
                new TableVectorIndexDefinition()
                        .column("vectorcolumn")
                        .metric(SimilarityMetric.DOT_PRODUCT),
                CreateVectorIndexOptions.IF_NOT_EXISTS);
        log.info("Created vector index on 'vectorcolumn'");
    }

    // ------------------------------------------
    // Text Index
    // ------------------------------------------

    @Test
    @Order(8)
    @DisplayName("08. Should create text index on table")
    public void shouldCreateTextIndex() {
        getDatabase().dropTable(TABLE_TEXT_INDEX, IF_EXISTS);

        Table<Row> table = getDatabase().createTable(TABLE_TEXT_INDEX, new TableDefinition()
                .addColumnText("email")
                .addColumnText("description")
                .partitionKey("email"), IF_NOT_EXISTS);

        table.createTextIndex("idx_description", "description");
        assertThat(table.listIndexes()).isNotEmpty();
        log.info("Created text index on 'description' column");
    }

    // ------------------------------------------
    // List Indexes
    // ------------------------------------------

    @Test
    @Order(9)
    @DisplayName("09. Should list all indexes on collection table")
    public void shouldListIndexes() {
        Table<Row> table = getDatabase().getTable(TABLE_COLLECTION_INDEX);
        int count = 0;
        for (TableIndexDescriptor<?> idx : table.listIndexes()) {
            log.info("Index: {}", idx.getName());
            count++;
        }
        // We created: set, list, map entries, map keys, map values, vector = 6 indexes
        assertThat(count).isGreaterThanOrEqualTo(6);
    }
}
