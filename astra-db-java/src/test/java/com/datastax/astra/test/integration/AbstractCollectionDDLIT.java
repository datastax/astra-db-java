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
public abstract class AbstractCollectionDDLIT extends AbstractDataAPITest {

    @BeforeAll
    void setupCleanKeyspace() {
        dropAllCollections();
        dropAllTables();
    }

    // ==========================
    //   DDL FOR COLLECTIONS
    // ==========================

    @Test
    @Order(1)
    void should_create_collection_simple() {
        // When accessing and initializing the DB
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        if (db.collectionExists(COLLECTION_SIMPLE)) {
            db.dropCollection(COLLECTION_SIMPLE);
        }

        db.createCollection(COLLECTION_SIMPLE);
        assertThat(db.collectionExists(COLLECTION_SIMPLE)).isTrue();

        Collection<Document> collection = db.getCollection(COLLECTION_SIMPLE);
        assertThat(collection).isNotNull();
        assertThat(collection.getCollectionName()).isEqualTo(COLLECTION_SIMPLE);
    }

    @Test
    @Order(2)
    void should_create_collection_vector() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        if (db.collectionExists(COLLECTION_VECTOR)) {
            db.dropCollection(COLLECTION_VECTOR);
        }

        Collection<Document> collectionVector = db.createCollection(COLLECTION_VECTOR, COLLECTION_VECTOR_DEF);

        assertThat(collectionVector).isNotNull();
        assertThat(collectionVector.getCollectionName()).isEqualTo(COLLECTION_VECTOR);

        CollectionDefinition colDefinition = collectionVector.getDefinition();
        assertThat(colDefinition.getVector()).isNotNull();
        assertThat(colDefinition.getVector().getDimension()).isEqualTo(14);
    }

    @Test
    @Order(3)
    void should_create_collection_index_allow() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);

        if (db.collectionExists(COLLECTION_ALLOW)) {
            db.dropCollection(COLLECTION_ALLOW);
        }

        Collection<Document> collectionAllow = db.createCollection(COLLECTION_ALLOW,
                new CollectionDefinition().indexingAllow("a", "b", "c"));
        assertThat(collectionAllow).isNotNull();
        CollectionDefinition options = collectionAllow.getDefinition();
        assertThat(options.getIndexing()).isNotNull();
        assertThat(options.getIndexing().getAllow()).isNotNull();
    }

    @Test
    @Order(4)
    void should_list_collections() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);
        if (!db.collectionExists(COLLECTION_SIMPLE)) {
            db.createCollection(COLLECTION_SIMPLE);
        }
        List<String> collectionNames = db.listCollectionNames();
        assertThat(collectionNames).isNotNull();
        assertThat(collectionNames).isNotEmpty();
    }

    @Test
    @Order(5)
    void should_drop_collection() throws InterruptedException {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);
        String tempCollection = "temp_collection_" + System.currentTimeMillis();
        db.createCollection(tempCollection);
        assertThat(db.collectionExists(tempCollection)).isTrue();
        db.dropCollection(tempCollection);
        Thread.sleep(1000);
        assertThat(db.collectionExists(tempCollection)).isFalse();
    }

    @Test
    @Order(6)
    void should_create_collection_command() {
        Database db = getDatabase().useKeyspace(DEFAULT_KEYSPACE);
        Command cmd = Command.create("createCollection").append("name", COLLECTION_SIMPLE);
        DataAPIResponse res = db.runCommand(cmd);
        assertThat(res).isNotNull();
        assertThat(res.getStatus().getInteger("ok")).isEqualTo(1);
    }

    @Test
    @Order(7)
    void should_throw_error_on_collection_not_found() {
        Database db = getDatabase();
        Collection<Document> collection = db.getCollection("nonexistent_collection");
        assertThat(collection).isNotNull();
        assertThat(db.collectionExists("nonexistent_collection")).isFalse();
        assertThatThrownBy(collection::getDefinition)
                .isInstanceOf(DataAPIException.class)
                .hasMessageContaining("COLLECTION_NOT_EXIST");
    }

}
