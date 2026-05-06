package com.datastax.astra.client.tables.mapping;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as containing the primary key for a table entity.
 * <p>
 * This annotation is used on a field whose type is annotated with {@link TablePrimaryKeyClass}.
 * The primary key class encapsulates all partition key and clustering column components.
 * </p>
 *
 * <p>This pattern is similar to Spring Data Cassandra's {@code @PrimaryKey} annotation and allows
 * for cleaner entity modeling when dealing with composite keys.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * {@code
 * @TablePrimaryKeyClass
 * public class OrderKey {
 *     @PartitionBy(1)
 *     @Column("customer_id")
 *     private String customerId;
 *     
 *     @PartitionSort(position = 1, order = PartitionSortOrder.ASC)
 *     @Column("order_date")
 *     private LocalDate orderDate;
 *     
 *     // constructors, getters, setters, equals, hashCode
 * }
 *
 * @EntityTable("orders")
 * public class Order {
 *     @TablePrimaryKey
 *     private OrderKey key;
 *     
 *     @Column("amount")
 *     private BigDecimal amount;
 *     
 *     // getters and setters
 * }
 * }
 * </pre>
 *
 * <p><b>Retention:</b> {@code RUNTIME}</p>
 * This annotation is retained at runtime to allow runtime reflection.
 *
 * <p><b>Target:</b> {@code FIELD}</p>
 * This annotation can only be applied to fields.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TablePrimaryKey {
}
