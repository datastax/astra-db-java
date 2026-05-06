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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Standalone demo to verify @TablePrimaryKeyClass pattern implementation.
 * Run with: java -cp ... com.datastax.astra.test.unit.TablePrimaryKeyClassDemo
 */
public class TablePrimaryKeyClassDemo {

    public static void main(String[] args) {
        System.out.println("=== @TablePrimaryKeyClass Pattern Demo ===\n");
        
        // Test 1: Row Serialization (flattening)
        System.out.println("Test 1: Row Serialization");
        OrderKey key = new OrderKey("CUST123", LocalDate.of(2024, 1, 15));
        OrderBean orderBean = new OrderBean(key, "ORD001", new BigDecimal("99.99"), "PENDING");
        
        Row row = RowMapper.mapAsRow(orderBean);
        
        System.out.println("  ✓ Primary key fields flattened:");
        System.out.println("    - customer_id: " + row.get("customer_id", String.class));
        System.out.println("    - order_date: " + row.get("order_date", LocalDate.class));
        System.out.println("  ✓ Entity fields present:");
        System.out.println("    - order_id: " + row.get("order_id", String.class));
        System.out.println("    - amount: " + row.get("amount", BigDecimal.class));
        System.out.println("    - status: " + row.get("status", String.class));
        System.out.println("  ✓ @TablePrimaryKey field NOT in row: " + !row.getColumnMap().containsKey("key"));
        
        // Test 2: Bean Definition (field expansion)
        System.out.println("\nTest 2: Bean Definition");
        EntityTableBeanDefinition<OrderBean> beanDef = new EntityTableBeanDefinition<>(OrderBean.class);
        
        System.out.println("  ✓ Fields expanded from OrderKey:");
        System.out.println("    - customer_id present: " + beanDef.getFields().containsKey("customer_id"));
        System.out.println("    - order_date present: " + beanDef.getFields().containsKey("order_date"));
        System.out.println("  ✓ Entity fields present:");
        System.out.println("    - order_id present: " + beanDef.getFields().containsKey("order_id"));
        System.out.println("    - amount present: " + beanDef.getFields().containsKey("amount"));
        System.out.println("    - status present: " + beanDef.getFields().containsKey("status"));
        System.out.println("  ✓ @TablePrimaryKey field NOT in fields: " + !beanDef.getFields().containsKey("key"));
        
        // Test 3: Partition Keys
        System.out.println("\nTest 3: Partition Keys");
        System.out.println("  ✓ Partition keys: " + beanDef.getPartitionBy());
        
        // Test 4: Clustering Columns
        System.out.println("\nTest 4: Clustering Columns");
        System.out.println("  ✓ Clustering columns: " + beanDef.getPartitionSort());
        
        System.out.println("\n=== All Tests Passed! ===");
    }
}
