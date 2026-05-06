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
 * Marks a class as representing a composite primary key for a table entity.
 * <p>
 * This annotation is used to define a separate class that encapsulates the primary key
 * components (partition key + clustering columns) of a table. Fields within this class
 * should be annotated with {@link PartitionBy} and {@link PartitionSort} to define
 * the key structure.
 * </p>
 *
 * <p>This pattern is similar to Spring Data Cassandra's {@code @PrimaryKeyClass} and allows
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
 * <p><b>Target:</b> {@code TYPE}</p>
 * This annotation can only be applied to classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TablePrimaryKeyClass {
}
