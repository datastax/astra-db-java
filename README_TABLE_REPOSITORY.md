# DataApiTableCrudRepository Usage Guide

This guide demonstrates the three patterns for using `DataApiTableCrudRepository` with Astra DB Tables in Spring Boot applications.

## Pattern 1: Single Partition Key

Use when your table has a single partition key column.

### Entity Definition
```java
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import java.util.UUID;

@EntityTable("users")
public class User {
    @PartitionBy(1)
    @Column("user_id")
    private UUID userId;
    
    @Column("name")
    private String name;
    
    @Column("email")
    private String email;
    
    // Constructors, getters, setters
}
```

### Repository Definition
```java
import com.datastax.astra.spring.DataApiTableCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends DataApiTableCrudRepository<User, UUID> {
}
```

### Usage
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public void example() {
        // Create
        User user = new User(UUID.randomUUID(), "John Doe", "john@example.com");
        userRepository.save(user);
        
        // Read
        UUID userId = user.getUserId();
        Optional<User> found = userRepository.findById(userId);
        
        // Delete
        userRepository.deleteById(userId);
    }
}
```

## Pattern 2: Composite Key with Map

Use when your table has multiple partition keys or clustering columns, and you prefer a flexible Map-based approach.

### Entity Definition
```java
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import com.datastax.astra.client.tables.mapping.PartitionSortOrder;
import java.math.BigDecimal;
import java.time.LocalDate;

@EntityTable("orders")
public class Order {
    @PartitionBy(1)
    @Column("customer_id")
    private String customerId;
    
    @PartitionSort(position = 1, orderBean = PartitionSortOrder.ASC)
    @Column("order_date")
    private LocalDate orderDate;
    
    @Column("order_id")
    private String orderId;
    
    @Column("amount")
    private BigDecimal amount;
    
    // Constructors, getters, setters
}
```

### Repository Definition
```java
import com.datastax.astra.spring.DataApiTableCrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public interface OrderRepository extends DataApiTableCrudRepository<Order, Map<String, Object>> {
}
```

### Usage
```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    public void example() {
        // Create
        Order orderBean = new Order("CUST123", LocalDate.now(), "ORD001", new BigDecimal("99.99"));
        orderRepository.save(orderBean);
        
        // Read - construct primary key as Map
        Map<String, Object> primaryKey = Map.of(
            "customer_id", "CUST123",
            "order_date", LocalDate.now()
        );
        Optional<Order> found = orderRepository.findById(primaryKey);
        
        // Delete
        orderRepository.deleteById(primaryKey);
    }
}
```

## Pattern 3: Composite Key with @TablePrimaryKeyClass (Recommended)

Use when your table has multiple partition keys or clustering columns, and you want type-safe, object-oriented primary key handling. This pattern is similar to Spring Data Cassandra's `@PrimaryKeyClass`.

### Primary Key Class Definition
```java
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.PartitionBy;
import com.datastax.astra.client.tables.mapping.PartitionSort;
import com.datastax.astra.client.tables.mapping.PartitionSortOrder;
import com.datastax.astra.client.tables.mapping.TablePrimaryKeyClass;
import java.time.LocalDate;
import java.util.Objects;

@TablePrimaryKeyClass
public class OrderKey {
    @PartitionBy(1)
    @Column("customer_id")
    private String customerId;
    
    @PartitionSort(position = 1, orderBean = PartitionSortOrder.ASC)
    @Column("order_date")
    private LocalDate orderDate;
    
    // Default constructor
    public OrderKey() {}
    
    // Constructor
    public OrderKey(String customerId, LocalDate orderDate) {
        this.customerId = customerId;
        this.orderDate = orderDate;
    }
    
    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    
    // equals and hashCode are REQUIRED for proper key comparison
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
```

### Entity Definition
```java
import com.datastax.astra.client.tables.mapping.Column;
import com.datastax.astra.client.tables.mapping.EntityTable;
import com.datastax.astra.client.tables.mapping.TablePrimaryKey;
import java.math.BigDecimal;

@EntityTable("orders")
public class Order {
    @TablePrimaryKey
    private OrderKey key;
    
    @Column("order_id")
    private String orderId;
    
    @Column("amount")
    private BigDecimal amount;
    
    @Column("status")
    private String status;
    
    // Constructors
    public Order() {}
    
    public Order(OrderKey key, String orderId, BigDecimal amount, String status) {
        this.key = key;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
    
    // Getters and setters
    public OrderKey getKey() { return key; }
    public void setKey(OrderKey key) { this.key = key; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

### Repository Definition
```java
import com.datastax.astra.spring.DataApiTableCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends DataApiTableCrudRepository<Order, OrderKey> {
}
```

### Usage
```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    public void example() {
        // Create - type-safe primary key
        OrderKey key = new OrderKey("CUST123", LocalDate.now());
        Order orderBean = new Order(key, "ORD001", new BigDecimal("99.99"), "PENDING");
        orderRepository.save(orderBean);
        
        // Read - type-safe lookup
        Optional<Order> found = orderRepository.findById(key);
        
        // Update
        found.ifPresent(o -> {
            o.setStatus("COMPLETED");
            orderRepository.save(o);
        });
        
        // Delete - type-safe deletion
        orderRepository.deleteById(key);
        
        // Delete by entity (extracts key automatically)
        orderRepository.delete(orderBean);
    }
}
```

## Comparison of Patterns

| Feature | Single Key | Map Key | @TablePrimaryKeyClass |
|---------|-----------|---------|----------------------|
| Type Safety | ✅ High | ❌ Low | ✅ High |
| Readability | ✅ Excellent | ⚠️ Fair | ✅ Excellent |
| Refactoring | ✅ Easy | ❌ Difficult | ✅ Easy |
| IDE Support | ✅ Full | ⚠️ Limited | ✅ Full |
| Boilerplate | ✅ Minimal | ✅ Minimal | ⚠️ Moderate |
| Best For | Single partition key | Quick prototypes | Production code with composite keys |

## Recommendations

1. **Use Pattern 1** for tables with a single partition key
2. **Use Pattern 3** (@TablePrimaryKeyClass) for tables with composite keys in production code
3. **Use Pattern 2** (Map) only for quick prototypes or when key structure is highly dynamic

## Spring Boot Configuration

Add to your `application.yml`:

```yaml
astra:
  data-api:
    token: ${ASTRA_DB_TOKEN}
    endpoint-url: ${ASTRA_DB_ENDPOINT}
    keyspace: ${ASTRA_DB_KEYSPACE:default_keyspace}
```

## Additional Features

All patterns support the full `CrudRepository` interface:

```java
// Batch operations
List<Order> orders = Arrays.asList(order1, order2, order3);
orderRepository.saveAll(orders);

// Existence check
boolean exists = orderRepository.existsById(key);

// Count
long count = orderRepository.count();

// Find all
Iterable<Order> allOrders = orderRepository.findAll();

// Delete all
orderRepository.deleteAll();
```
