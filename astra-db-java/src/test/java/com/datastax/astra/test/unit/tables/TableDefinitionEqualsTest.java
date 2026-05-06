package com.datastax.astra.test.unit.tables;

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

import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TableDefinition equals and hashCode methods.
 */
class TableDefinitionEqualsTest {

    @Test
    void should_be_equal_when_same_structure() {
        // Given: Two identical table definitions
        TableDefinition def1 = new TableDefinition()
                .addColumnText("id")
                .addColumnText("name")
                .addColumnInt("age")
                .partitionKey("id");

        TableDefinition def2 = new TableDefinition()
                .addColumnText("id")
                .addColumnText("name")
                .addColumnInt("age")
                .partitionKey("id");

        // Then: They should be equal
        assertThat(def1).isEqualTo(def2);
        assertThat(def1.hashCode()).isEqualTo(def2.hashCode());
    }

    @Test
    void should_not_be_equal_when_different_columns() {
        // Given: Two table definitions with different columns
        TableDefinition def1 = new TableDefinition()
                .addColumnText("id")
                .addColumnText("name")
                .partitionKey("id");

        TableDefinition def2 = new TableDefinition()
                .addColumnText("id")
                .addColumnInt("age")
                .partitionKey("id");

        // Then: They should not be equal
        assertThat(def1).isNotEqualTo(def2);
    }

    @Test
    void should_not_be_equal_when_different_column_types() {
        // Given: Two table definitions with same column names but different types
        TableDefinition def1 = new TableDefinition()
                .addColumnText("id")
                .addColumnText("value")
                .partitionKey("id");

        TableDefinition def2 = new TableDefinition()
                .addColumnText("id")
                .addColumnInt("value")
                .partitionKey("id");

        // Then: They should not be equal
        assertThat(def1).isNotEqualTo(def2);
    }

    @Test
    void should_not_be_equal_when_different_primary_key() {
        // Given: Two table definitions with different primary keys
        TableDefinition def1 = new TableDefinition()
                .addColumnText("id")
                .addColumnText("name")
                .partitionKey("id");

        TableDefinition def2 = new TableDefinition()
                .addColumnText("id")
                .addColumnText("name")
                .partitionKey("name");

        // Then: They should not be equal
        assertThat(def1).isNotEqualTo(def2);
    }

    @Test
    void should_be_equal_with_clustering_columns() {
        // Given: Two table definitions with clustering columns
        TableDefinition def1 = new TableDefinition()
                .addColumnText("customer_id")
                .addColumnTimestamp("order_date")
                .addColumnInt("amount")
                .partitionKey("customer_id")
                .clusteringColumns(Sort.ascending("order_date"));

        TableDefinition def2 = new TableDefinition()
                .addColumnText("customer_id")
                .addColumnTimestamp("order_date")
                .addColumnInt("amount")
                .partitionKey("customer_id")
                .clusteringColumns(Sort.ascending("order_date"));

        // Then: They should be equal
        assertThat(def1).isEqualTo(def2);
        assertThat(def1.hashCode()).isEqualTo(def2.hashCode());
    }

    @Test
    void should_not_be_equal_when_different_clustering_order() {
        // Given: Two table definitions with different clustering order
        TableDefinition def1 = new TableDefinition()
                .addColumnText("customer_id")
                .addColumnTimestamp("order_date")
                .partitionKey("customer_id")
                .clusteringColumns(Sort.ascending("order_date"));

        TableDefinition def2 = new TableDefinition()
                .addColumnText("customer_id")
                .addColumnTimestamp("order_date")
                .partitionKey("customer_id")
                .clusteringColumns(Sort.descending("order_date"));

        // Then: They should not be equal
        assertThat(def1).isNotEqualTo(def2);
    }

    @Test
    void should_handle_null_comparison() {
        // Given: A table definition
        TableDefinition def = new TableDefinition()
                .addColumnText("id")
                .partitionKey("id");

        // Then: Should not equal null
        assertThat(def).isNotEqualTo(null);
    }

    @Test
    void should_be_equal_to_itself() {
        // Given: A table definition
        TableDefinition def = new TableDefinition()
                .addColumnText("id")
                .partitionKey("id");

        // Then: Should equal itself
        assertThat(def).isEqualTo(def);
    }
}
