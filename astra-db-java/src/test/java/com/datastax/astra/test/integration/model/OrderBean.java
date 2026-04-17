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

import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.TablePrimaryKey;

import java.math.BigDecimal;

/**
 * Test model for @TablePrimaryKeyClass pattern.
 * Entity with a composite primary key using @TablePrimaryKey annotation.
 */
@EntityTable("orders_pk_class")
public class OrderBean {
    
    @TablePrimaryKey
    private OrderKey key;
    
    @Column(name = "order_id")
    private String orderId;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Column(name = "status")
    private String status;
    
    public OrderBean() {}
    
    public OrderBean(OrderKey key, String orderId, BigDecimal amount, String status) {
        this.key = key;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
    
    public OrderKey getKey() {
        return key;
    }
    
    public void setKey(OrderKey key) {
        this.key = key;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "Order{key=" + key + ", orderId='" + orderId + "', amount=" + amount + ", status='" + status + "'}";
    }
}
