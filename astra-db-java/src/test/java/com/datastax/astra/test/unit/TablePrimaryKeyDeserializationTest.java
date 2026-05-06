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
import com.datastax.astra.internal.serdes.tables.RowMapper;
import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.datastax.astra.test.integration.model.OrderBean;
import com.datastax.astra.test.integration.model.OrderKey;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for deserializing flattened rows back to beans with @TablePrimaryKey.
 */
public class TablePrimaryKeyDeserializationTest {

    @Test
    public void should_deserialize_flattened_row_to_bean_with_primaryKey() {
        // Given: A flattened row (as returned by the server)
        Row row = new Row();
        row.put("customer_id", "CUST123");
        row.put("order_date", LocalDate.of(2024, 1, 15));
        row.put("order_id", "ORD001");
        row.put("amount", new BigDecimal("99.99"));
        row.put("status", "PENDING");
        
        // When: Deserialize to OrderBean
        RowSerializer serializer = new RowSerializer();
        OrderBean bean = RowMapper.mapFromRow(row, serializer, OrderBean.class);
        
        // Then: The bean should be properly reconstructed with the primary key
        assertThat(bean).isNotNull();
        assertThat(bean.getKey()).isNotNull();
        assertThat(bean.getKey().getCustomerId()).isEqualTo("CUST123");
        assertThat(bean.getKey().getOrderDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(bean.getOrderId()).isEqualTo("ORD001");
        assertThat(bean.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(bean.getStatus()).isEqualTo("PENDING");
    }

    @Test
    public void should_roundtrip_serialize_and_deserialize() {
        // Given: An OrderBean with composite primary key
        OrderKey key = new OrderKey("CUST456", LocalDate.of(2024, 2, 20));
        OrderBean original = new OrderBean(key, "ORD002", new BigDecimal("150.50"), "SHIPPED");
        
        // When: Serialize to Row
        Row row = RowMapper.mapAsRow(original);
        
        // And: Deserialize back to OrderBean
        RowSerializer serializer = new RowSerializer();
        OrderBean deserialized = RowMapper.mapFromRow(row, serializer, OrderBean.class);
        
        // Then: The deserialized bean should match the original
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getKey()).isNotNull();
        assertThat(deserialized.getKey().getCustomerId()).isEqualTo(original.getKey().getCustomerId());
        assertThat(deserialized.getKey().getOrderDate()).isEqualTo(original.getKey().getOrderDate());
        assertThat(deserialized.getOrderId()).isEqualTo(original.getOrderId());
        assertThat(deserialized.getAmount()).isEqualByComparingTo(original.getAmount());
        assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
    }
}
