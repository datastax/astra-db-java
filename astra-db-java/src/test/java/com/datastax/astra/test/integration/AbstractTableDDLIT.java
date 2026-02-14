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

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.exceptions.DataAPIException;
import com.datastax.astra.client.tables.Table;
import com.datastax.astra.client.tables.commands.options.CreateTableOptions;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinitionVector;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.test.integration.model.TableEntityGameWithAnnotation;
import com.datastax.astra.test.integration.model.TableEntityGameWithAnnotationAllHints;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;
import static com.datastax.astra.test.integration.utils.TestDataset.*;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Abstract base class for Database integration tests.
 * Extend this class and add environment-specific annotations.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractTableDDLIT extends AbstractDataAPITest {

    @BeforeAll
    void setupCleanKeyspace() {
        dropAllCollections();
        dropAllTables();
    }

    @Test
    @Order(1)
    void should_create_table() {
        Database db = getDatabase();
        if (db.tableExists("game1")) {
            db.dropTable("game1");
        }

        TableDefinition tableDefinition = new TableDefinition()
                .addColumnText("match_id")
                .addColumnInt("round")
                .addColumnVector("m_vector", new TableColumnDefinitionVector()
                        .dimension(3)
                        .metric(SimilarityMetric.COSINE))
                .addColumn("score", TableColumnTypes.INT)
                .addColumn("when", TableColumnTypes.TIMESTAMP)
                .addColumn("winner", TableColumnTypes.TEXT)
                .addColumnSet("fighters", TableColumnTypes.UUID)
                .addPartitionBy("match_id")
                .addPartitionSort(Sort.ascending("round"));

        CreateTableOptions createTableOptions = new CreateTableOptions()
                .ifNotExists(true)
                .timeout(ofSeconds(5));

        Table<Row> table1 = db.createTable("game1", tableDefinition, createTableOptions);
        assertThat(table1).isNotNull();
        assertThat(db.tableExists("game1")).isTrue();
    }

    @Test
    @Order(2)
    void should_list_tables() {
        List<String> tableNames = getDatabase().listTableNames();
        assertThat(tableNames).isNotEmpty();
        assertThat(tableNames).contains("game1");
    }

    @Test
    @Order(3)
    void should_create_table_from_bean() {
        // game_ann2
        Database db = getDatabase();
        String tableName = db.getTableName(TableEntityGameWithAnnotation.class);
        if (db.tableExists(tableName)) {
            db.dropTable(tableName);
        }

        Table<TableEntityGameWithAnnotation> table = db.createTable(
                TableEntityGameWithAnnotation.class,
                CreateTableOptions.IF_NOT_EXISTS);
        assertThat(getDatabase().tableExists(tableName)).isTrue();
        assertThat(table).isNotNull();
    }

    @Test
    @Order(4)
    void should_create_table_from_fully_annotated_bean() {
        String tableName = getDatabase().getTableName(TableEntityGameWithAnnotationAllHints.class);
        if (getDatabase().tableExists(tableName)) {
            getDatabase().dropTable(tableName);
        }

        Table<TableEntityGameWithAnnotationAllHints> table = getDatabase().createTable(
                TableEntityGameWithAnnotationAllHints.class,
                CreateTableOptions.IF_NOT_EXISTS);
        assertThat(getDatabase().tableExists(tableName)).isTrue();
        assertThat(table).isNotNull();
    }
}
