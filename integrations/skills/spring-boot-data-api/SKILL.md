---
name: "Building Spring Boot Applications with DataStax Astra DB"
description: "Learn how to integrate DataStax Astra DB with Spring Boot using Spring Data repositories, object mapping, and the Data API for building modern, scalable applications with vector search capabilities."
author: "DataStax"
version: "1.0.0"
tags:
  - spring-boot
  - astra-db
  - spring-data
  - nosql
  - vector-search
  - java
  - rest-api
difficulty: intermediate
prerequisites:
  - "Java 17 or higher"
  - "Maven or Gradle"
  - "Basic Spring Boot knowledge"
  - "Astra DB account (free tier available)"
estimated_time: "30-45 minutes"
learning_objectives:
  - "Set up Spring Boot with Astra DB Data API"
  - "Use Spring Data repositories for CRUD operations"
  - "Implement object mapping with annotations"
  - "Build REST APIs with Spring MVC"
  - "Enable vector search for semantic queries"
  - "Configure and optimize database connections"
---

# Building Spring Boot Applications with DataStax Astra DB

## Overview

This skill teaches you how to build production-ready Spring Boot applications using DataStax Astra DB, a cloud-native NoSQL database built on Apache Cassandra. You'll learn to leverage the Spring Boot ecosystem with Astra DB's Data API, enabling rapid development of scalable applications with advanced features like vector search for AI/ML workloads.

**What You'll Build:**
- RESTful APIs with Spring MVC
- Data access layer with Spring Data repositories
- Object mapping with custom annotations
- Vector search for semantic queries
- Production-ready configuration

**Why This Matters:**
- **Developer Productivity**: Spring Data abstracts database complexity
- **Scalability**: Astra DB provides automatic scaling
- **Modern Features**: Built-in vector search for AI applications
- **Cloud-Native**: Serverless architecture with pay-per-use pricing

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                  │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ REST         │  │ Service      │  │ Repository   │     │
│  │ Controllers  │─▶│ Layer        │─▶│ (Spring Data)│     │
│  └──────────────┘  └──────────────┘  └──────┬───────┘     │
│                                              │              │
│  ┌──────────────────────────────────────────▼───────────┐  │
│  │         Astra DB Spring Boot Starter                 │  │
│  │  • Auto-configuration                                │  │
│  │  • Connection management                             │  │
│  │  • Object mapping                                    │  │
│  └──────────────────────────────────────────┬───────────┘  │
└─────────────────────────────────────────────┼──────────────┘
                                              │
                                              │ Data API
                                              ▼
                                    ┌──────────────────┐
                                    │   Astra DB       │
                                    │   (Cloud)        │
                                    └──────────────────┘
```

## Part 1: Project Setup

### Step 1: Create Spring Boot Project

Use Spring Initializr or create manually:

```xml
<!-- pom.xml -->
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Astra DB Spring Boot Starter -->
        <dependency>
            <groupId>com.datastax.astra</groupId>
            <artifactId>data-api-spring-boot-3x-starter</artifactId>
            <version>1.5.1</version>
        </dependency>
        
        <!-- Lombok (optional, for cleaner code) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Step 2: Configure Astra DB Connection

Create `application.yml`:

```yaml
spring:
  application:
    name: astra-spring-demo

astra:
  data-api:
    # Database connection
    token: ${ASTRA_DB_TOKEN}
    endpoint-url: ${ASTRA_DB_ENDPOINT}
    keyspace: ${ASTRA_DB_KEYSPACE:default_keyspace}
    
    # Schema management
    schema-action: CREATE_IF_NOT_EXISTS
    
    # Logging (development)
    log-request: ${ASTRA_LOG_REQUESTS:false}
    
    # Advanced options
    options:
      # Timeouts (milliseconds)
      timeout:
        connect: 5000
        request: 10000
        general: 30000
        collection-admin: 60000
      
      # HTTP configuration
      http:
        retry-count: 3
        retry-delay: 100
        version: HTTP_2
      
      # Embedding API key for vector search
      embedding-api-key: ${OPENAI_API_KEY:}

# Logging configuration
logging:
  level:
    com.datastax.astra: ${ASTRA_LOG_LEVEL:INFO}
    com.datastax.astra.client.DataAPIClient: ${ASTRA_LOG_LEVEL:INFO}
```

**Environment Variables:**
```bash
export ASTRA_DB_TOKEN="AstraCS:..."
export ASTRA_DB_ENDPOINT="https://your-db-id-region.apps.astra.datastax.com"
export ASTRA_DB_KEYSPACE="my_keyspace"
export OPENAI_API_KEY="sk-..."  # For vector search
```

### Step 3: Main Application Class

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AstraSpringDemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AstraSpringDemoApplication.class, args);
    }
}
```

## Part 2: Domain Model & Object Mapping

### Understanding Object Mapping Annotations

The Astra DB SDK provides annotations for mapping Java objects to database collections:

| Annotation | Purpose | Serialized As |
|------------|---------|---------------|
| `@DataApiCollection` | Defines collection and configuration | N/A |
| `@DocumentId` | Marks the document identifier | `_id` |
| `@Vectorize` | Auto-vectorization for semantic search | `$vectorize` |
| `@Lexical` | Full-text search indexing | `$lexical` |
| `@Vector` | Pre-computed vector embeddings | `$vector` |

### Step 4: Create Domain Model

```java
package com.example.demo.model;

import com.datastax.astra.client.collections.mapping.*;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Set;

@Data
@Accessors(chain = true)
@DataApiCollection(
    name = "products",
    // Vector search configuration
    vectorDimension = 1536,
    vectorSimilarity = SimilarityMetric.COSINE,
    vectorizeProvider = "openai",
    vectorizeModel = "text-embedding-ada-002",
    vectorizeSharedSecret = "OPENAI_API_KEY",
    // Full-text search
    lexicalEnabled = true,
    lexicalAnalyzer = AnalyzerTypes.ENGLISH,
    // Indexing optimization
    indexingDeny = {"internal_notes", "audit_log"}
)
public class Product {
    
    @DocumentId
    private String id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100)
    private String name;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    
    @Min(0)
    private Integer stock;
    
    @Vectorize
    private String description;  // Automatically vectorized
    
    @Lexical
    private String searchableText;  // Full-text search
    
    private Set<String> tags;
    
    @JsonProperty("created_at")
    private Instant createdAt;
    
    @JsonProperty("updated_at")
    private Instant updatedAt;
    
    // Not indexed
    private String internalNotes;
}
```

**Key Points:**
- `@DataApiCollection` configures the collection
- `@DocumentId` maps to `_id` in the database
- `@Vectorize` enables semantic search
- `@Lexical` enables full-text search
- `indexingDeny` excludes fields from indexing

## Part 3: Data Access Layer

### Step 5: Create Repository Interface

```java
package com.example.demo.repository;

import com.datastax.astra.spring.DataApiCollectionCrudRepository;
import com.example.demo.model.Product;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository 
    extends DataApiCollectionCrudRepository<Product, String> {
    
    // Spring Data methods are automatically available:
    // - save(entity)
    // - findById(id)
    // - findAll()
    // - deleteById(id)
    // - count()
    // - existsById(id)
    // 
    // Plus Query by Example and Data API filters
}
```

### Step 6: Create Service Layer

```java
package com.example.demo.service;

import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository repository;
    
    // Create
    public Product create(Product product) {
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        log.info("Creating product: {}", product.getName());
        return repository.save(product);
    }
    
    // Read
    public Optional<Product> findById(String id) {
        return repository.findById(id);
    }
    
    public List<Product> findAll() {
        return (List<Product>) repository.findAll();
    }
    
    // Update
    public Product update(String id, Product updates) {
        Product product = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        
        product.setName(updates.getName());
        product.setCategory(updates.getCategory());
        product.setPrice(updates.getPrice());
        product.setStock(updates.getStock());
        product.setDescription(updates.getDescription());
        product.setUpdatedAt(Instant.now());
        
        return repository.save(product);
    }
    
    // Delete
    public void delete(String id) {
        repository.deleteById(id);
    }
    
    // Query by Example
    public List<Product> findByCategory(String category) {
        Product probe = new Product().setCategory(category);
        return (List<Product>) repository.findAll(Example.of(probe));
    }
    
    // Data API Filters
    public List<Product> findInPriceRange(Double minPrice, Double maxPrice) {
        Filter filter = Filters.and(
            Filters.gte("price", minPrice),
            Filters.lte("price", maxPrice)
        );
        return (List<Product>) repository.findAll(filter);
    }
    
    // Pagination
    public Page<Product> findAllPaginated(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return repository.findAll(Example.of(new Product()), pageable);
    }
    
    // Complex query
    public List<Product> searchProducts(String category, Double minPrice, Integer minStock) {
        List<Filter> filters = new java.util.ArrayList<>();
        
        if (category != null) {
            filters.add(Filters.eq("category", category));
        }
        if (minPrice != null) {
            filters.add(Filters.gte("price", minPrice));
        }
        if (minStock != null) {
            filters.add(Filters.gte("stock", minStock));
        }
        
        if (filters.isEmpty()) {
            return findAll();
        }
        
        Filter filter = filters.size() == 1 
            ? filters.get(0) 
            : Filters.and(filters.toArray(new Filter[0]));
            
        return (List<Product>) repository.findAll(filter);
    }
}
```

## Part 4: REST API Layer

### Step 7: Create REST Controller

```java
package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService service;
    
    @GetMapping
    public List<Product> list() {
        return service.findAll();
    }
    
    @GetMapping("/paginated")
    public Page<Product> listPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        return service.findAllPaginated(page, size, sortBy);
    }
    
    @GetMapping("/{id}")
    public Product get(@PathVariable String id) {
        return service.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product product) {
        return service.create(product);
    }
    
    @PutMapping("/{id}")
    public Product update(@PathVariable String id, @Valid @RequestBody Product product) {
        return service.update(id, product);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
    
    @GetMapping("/category/{category}")
    public List<Product> byCategory(@PathVariable String category) {
        return service.findByCategory(category);
    }
    
    @GetMapping("/search")
    public List<Product> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Integer minStock) {
        return service.searchProducts(category, minPrice, minStock);
    }
    
    @GetMapping("/price-range")
    public List<Product> priceRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return service.findInPriceRange(min, max);
    }
}
```

### Step 8: Error Handling

```java
package com.example.demo.exception;

import com.datastax.astra.client.exceptions.DataAPIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        return new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            Instant.now()
        );
    }
    
    @ExceptionHandler(DataAPIException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleDataAPIException(DataAPIException ex) {
        return new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Database error: " + ex.getMessage(),
            Instant.now()
        );
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
    
    record ErrorResponse(int status, String message, Instant timestamp) {}
}

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

## Part 5: Advanced Features

### Vector Search for Semantic Queries

```java
package com.example.demo.service;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;
import com.example.demo.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VectorSearchService {
    
    private final Database database;
    
    public List<Product> semanticSearch(String query, int limit) {
        Collection<Product> collection = database.getCollection("products", Product.class);
        
        return collection.find(
            Filters.vectorize(query),
            new CollectionFindOptions()
                .sort(Sort.vectorize(query))
                .limit(limit)
        ).toList();
    }
    
    public List<Product> hybridSearch(String query, String category, int limit) {
        Collection<Product> collection = database.getCollection("products", Product.class);
        
        return collection.find(
            Filters.and(
                Filters.vectorize(query),
                Filters.eq("category", category)
            ),
            new CollectionFindOptions()
                .sort(Sort.vectorize(query))
                .limit(limit)
        ).toList();
    }
}
```

### Health Check Endpoint

```java
package com.example.demo.controller;

import com.datastax.astra.client.databases.Database;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {
    
    private final Database database;
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "database", database.getRootEndpoint(),
            "keyspace", database.getKeyspace(),
            "collections", database.listCollectionNames().size()
        );
    }
}
```

## Part 6: Testing

### Unit Tests

```java
package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository repository;
    
    @InjectMocks
    private ProductService service;
    
    @Test
    void testCreate() {
        Product product = new Product()
            .setName("Test Product")
            .setPrice(99.99);
        
        when(repository.save(any(Product.class))).thenReturn(product);
        
        Product result = service.create(product);
        
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(repository, times(1)).save(any(Product.class));
    }
    
    @Test
    void testFindById() {
        Product product = new Product().setId("123").setName("Test");
        when(repository.findById("123")).thenReturn(Optional.of(product));
        
        Optional<Product> result = service.findById("123");
        
        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }
}
```

### Integration Tests

```java
package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductRepository repository;
    
    @Test
    void testCreateProduct() {
        Product product = new Product()
            .setName("Integration Test Product")
            .setCategory("Test")
            .setPrice(49.99)
            .setStock(10);
        
        ResponseEntity<Product> response = restTemplate.postForEntity(
            "/api/products",
            product,
            Product.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }
    
    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }
}
```

## Part 7: Production Considerations

### Configuration Profiles

```yaml
# application-dev.yml
astra:
  data-api:
    schema-action: CREATE_IF_NOT_EXISTS
    log-request: true

logging:
  level:
    com.datastax.astra: DEBUG

---
# application-prod.yml
astra:
  data-api:
    schema-action: VALIDATE
    log-request: false
    options:
      timeout:
        connect: 10000
        request: 20000

logging:
  level:
    com.datastax.astra: WARN
```

### Monitoring with Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  metrics:
    tags:
      application: ${spring.application.name}
```

### Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/actuator/**").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

## Spring Boot Ecosystem Integration

### Spring Boot DevTools

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Spring Boot Actuator Metrics

```java
@Component
public class DatabaseMetrics {
    
    private final MeterRegistry registry;
    private final ProductRepository repository;
    
    public DatabaseMetrics(MeterRegistry registry, ProductRepository repository) {
        this.registry = registry;
        this.repository = repository;
        
        Gauge.builder("products.count", repository, repo -> repo.count())
            .description("Total number of products")
            .register(registry);
    }
}
```

### Spring Cache

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("products");
    }
}

@Service
public class ProductService {
    
    @Cacheable("products")
    public Optional<Product> findById(String id) {
        return repository.findById(id);
    }
    
    @CacheEvict(value = "products", key = "#id")
    public void delete(String id) {
        repository.deleteById(id);
    }
}
```

### Spring Validation

```java
@Data
public class Product {
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private Double price;
    
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
}
```

### Spring Events

```java
@Component
public class ProductEventListener {
    
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Product created: {}", event.getProduct().getName());
        // Send notification, update cache, etc.
    }
}

@Service
public class ProductService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public Product create(Product product) {
        Product saved = repository.save(product);
        eventPublisher.publishEvent(new ProductCreatedEvent(this, saved));
        return saved;
    }
}
```

## Best Practices Summary

1. **Use DTOs for API boundaries** - Separate domain models from API contracts
2. **Implement proper error handling** - Use `@RestControllerAdvice` for global exception handling
3. **Validate input** - Use Bean Validation annotations
4. **Paginate large results** - Always use pagination for list endpoints
5. **Index strategically** - Only index fields you query on
6. **Use environment-specific configs** - Different settings for dev/prod
7. **Monitor your application** - Use Spring Boot Actuator
8. **Test thoroughly** - Unit tests for logic, integration tests for APIs
9. **Secure your endpoints** - Use Spring Security
10. **Document your API** - Use SpringDoc OpenAPI

## Troubleshooting

### Common Issues

**Connection Timeout:**
```yaml
astra:
  data-api:
    options:
      timeout:
        connect: 10000  # Increase timeout
```

**Schema Validation Errors:**
```yaml
astra:
  data-api:
    schema-action: CREATE_IF_NOT_EXISTS  # Auto-create collections
```

**Performance Issues:**
- Use pagination for large datasets
- Index only necessary fields
- Enable caching for frequently accessed data

## Next Steps

- 📖 Read the [Object Mapping Guide](OBJECT-MAPPING-GUIDE.md)
- 🔍 Explore [Vector Search Documentation](https://docs.datastax.com/en/astra-db-serverless/databases/vector-search.html)
- 🚀 Deploy to production with [Spring Boot best practices](https://spring.io/guides/gs/spring-boot/)
- 💬 Join the [DataStax Community](https://community.datastax.com/)

## Resources

- [Astra DB Documentation](https://docs.datastax.com/en/astra-db-serverless/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data Documentation](https://spring.io/projects/spring-data)
- [Sample Code Repository](https://github.com/datastax/astra-db-java/tree/main/samples/sample-spring-boot3x)

---

**Congratulations!** You've learned how to build production-ready Spring Boot applications with DataStax Astra DB. You can now create scalable, cloud-native applications with advanced features like vector search.