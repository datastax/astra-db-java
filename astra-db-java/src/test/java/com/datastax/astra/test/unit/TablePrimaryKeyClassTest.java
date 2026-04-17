package com.datastax.astra.test.unit;

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

import com.datastax.astra.client.tables.definition.rows.Row;
import com.datastax.astra.internal.reflection.EntityTableBeanDefinition;
import com.datastax.astra.internal.serdes.tables.RowMapper;
import com.datastax.astra.test.integration.model.OrderBean;
import com.datastax.astra.test.integration.model.OrderKey;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for @TablePrimaryKeyClass pattern support.
 * Tests serialization and TableDefinition creation without requiring database connection.
 */
public class TablePrimaryKeyClassTest {

    @Test
    public void should_flatten_primaryKey_fields_in_row_serialization() {
        // Given: An Order entity with @TablePrimaryKey
        OrderKey key = new OrderKey("CUST123", LocalDate.of(2024, 1, 15));
        OrderBean orderBean = new OrderBean(key, "ORD001", new BigDecimal("99.99"), "PENDING");
        
        // When: Serialize to Row
        Row row = RowMapper.mapAsRow(orderBean);
        
        // Then: Primary key fields should be flattened at the same level as other columns
        assertThat(row.getColumnMap()).containsKeys(
            "customer_id",  // from OrderKey (flattened)
            "order_date",   // from OrderKey (flattened)
            "order_id",     // from Order
            "amount",       // from Order
            "status"        // from Order
        );
        
        // Verify values
        assertThat(row.get("customer_id", String.class)).isEqualTo("CUST123");
        assertThat(row.get("order_date", LocalDate.class)).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(row.get("order_id", String.class)).isEqualTo("ORD001");
        assertThat(row.get("amount", BigDecimal.class)).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(row.get("status", String.class)).isEqualTo("PENDING");
        
        // The @TablePrimaryKey field itself should NOT be in the row
        assertThat(row.getColumnMap()).doesNotContainKey("key");
    }

    @Test
    public void should_expand_primaryKey_class_fields_in_bean_definition() {
        // Given: Order class with @TablePrimaryKeyClass pattern
        EntityTableBeanDefinition<OrderBean> beanDef = new EntityTableBeanDefinition<>(OrderBean.class);
        
        // Then: Fields from OrderKey should be expanded into the entity's field map
        assertThat(beanDef.getFields()).containsKeys(
            "customerId",   // from OrderKey (expanded)
            "orderDate",    // from OrderKey (expanded)
            "orderId",      // from Order
            "amount",       // from Order
            "status"        // from Order
        );
        
        // The @TablePrimaryKey field itself should NOT be in the fields map
        assertThat(beanDef.getFields()).doesNotContainKey("key");
    }

    @Test
    public void should_extract_partition_keys_from_primaryKey_class() {
        // Given: Order class with @TablePrimaryKeyClass pattern
        EntityTableBeanDefinition<OrderBean> beanDef = new EntityTableBeanDefinition<>(OrderBean.class);
        
        // When: Get partition keys
        var partitionKeys = beanDef.getPartitionBy();
        
        // Then: Should contain the partition key from OrderKey
        assertThat(partitionKeys).containsExactly("customer_id");
    }

    @Test
    public void should_extract_clustering_columns_from_primaryKey_class() {
        // Given: Order class with @TablePrimaryKeyClass pattern
        EntityTableBeanDefinition<OrderBean> beanDef = new EntityTableBeanDefinition<>(OrderBean.class);
        
        // When: Get clustering columns
        var clusteringColumns = beanDef.getPartitionSort();
        
        // Then: Should contain the clustering column from OrderKey
        assertThat(clusteringColumns).containsKey("order_date");
        assertThat(clusteringColumns.get("order_date")).isEqualTo(1); // ASC order
    }
}
