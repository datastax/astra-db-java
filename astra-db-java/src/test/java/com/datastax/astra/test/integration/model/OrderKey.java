package com.datastax.astra.test.integration.model;

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

import com.datastax.astra.client.core.query.SortOrder;
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import com.datastax.astra.client.tables.mapping.TablePrimaryKeyClass;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Test model for @TablePrimaryKeyClass pattern.
 * Represents a composite primary key with partition key and clustering column.
 */
@TablePrimaryKeyClass
public class OrderKey {
    
    @PartitionBy(1)
    @Column(name = "customer_id")
    private String customerId;
    
    @PartitionSort(position = 1, order = SortOrder.ASCENDING)
    @Column(name = "order_date")
    private LocalDate orderDate;
    
    public OrderKey() {}
    
    public OrderKey(String customerId, LocalDate orderDate) {
        this.customerId = customerId;
        this.orderDate = orderDate;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderKey orderKey = (OrderKey) o;
        return Objects.equals(customerId, orderKey.customerId) &&
               Objects.equals(orderDate, orderKey.orderDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(customerId, orderDate);
    }
    
    @Override
    public String toString() {
        return "OrderKey{customerId='" + customerId + "', orderDate=" + orderDate + "}";
    }
}
