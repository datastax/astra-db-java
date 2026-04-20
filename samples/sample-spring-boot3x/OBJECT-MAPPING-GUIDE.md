# Object Mapping and Spring Data Integration Guide

This guide explains how to use the DataStax Astra DB Java SDK's object mapping features and Spring Data integration in your Spring Boot applications.

## Table of Contents

- [Overview](#overview)
- [Core Annotations](#core-annotations)
  - [@DataApiCollection](#dataapicollection)
  - [@DocumentId](#documentid)
  - [@Vectorize](#vectorize)
  - [@Lexical](#lexical)
  - [@Vector](#vector)
- [Spring Data Integration](#spring-data-integration)
- [Configuration](#configuration)
- [Complete Example](#complete-example)
- [Best Practices](#best-practices)

## Overview

The Astra DB Java SDK provides a powerful object mapping framework that allows you to work with Java objects instead of raw documents. Combined with Spring Data integration, you get a familiar repository pattern for database operations.

## Core Annotations

### @DataApiCollection

The `@DataApiCollection` annotation marks a class as a collection document and configures collection-level settings.

**Location:** `com.datastax.astra.client.collections.mapping.DataApiCollection`

**Properties:**

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `name` | String | Class name (lowercase) | Collection name in the database |
| `defaultIdType` | String | "" | Default ID type (e.g., "uuid", "objectId", "uuidv6", "uuidv7") |
| `indexingDeny` | String[] | {} | Fields to exclude from indexing |
| `indexingAllow` | String[] | {} | Fields to include in indexing (mutually exclusive with `indexingDeny`) |
| `vectorDimension` | int | -1 | Vector dimension size for vector search |
| `vectorSimilarity` | SimilarityMetric | COSINE | Similarity metric (COSINE, DOT_PRODUCT, EUCLIDEAN) |
| `vectorizeProvider` | String | "" | Vectorization service provider (e.g., "openai", "nvidia", "huggingface") |
| `vectorizeModel` | String | "" | Vectorization model name |
| `vectorizeSharedSecret` | String | "" | Shared secret key name for authentication |
| `lexicalEnabled` | boolean | false | Enable lexical (full-text) search |
| `lexicalAnalyzer` | AnalyzerTypes | STANDARD | Analyzer type for lexical search |
| `rerankEnabled` | boolean | false | Enable reranking |
| `rerankProvider` | String | "" | Reranking service provider |
| `rerankModel` | String | "" | Reranking model name |

**Example:**

```java
@DataApiCollection(
    name = "books",
    vectorDimension = 1536,
    vectorSimilarity = SimilarityMetric.COSINE,
    vectorizeProvider = "openai",
    vectorizeModel = "text-embedding-ada-002",
    vectorizeSharedSecret = "OPENAI_API_KEY",
    lexicalEnabled = true,
    indexingDeny = {"internal_field", "temp_data"}
)
public class Book {
    // fields...
}
```

### @DocumentId

The `@DocumentId` annotation marks a field as the document's unique identifier (`_id` in the database).

**Location:** `com.datastax.astra.client.collections.mapping.DocumentId`

**Rules:**
- Only one field per class can be annotated with `@DocumentId`
- The field is serialized as `_id` in the database
- If not provided during insertion, the server generates a unique ID

**Example:**

```java
@DataApiCollection(name = "users")
public class User {
    @DocumentId
    private String id;
    
    private String username;
    private String email;
    
    // getters and setters
}
```

**Supported ID Types:**
- `String`
- `UUID`
- `ObjectId`
- `UUIDv6`
- `UUIDv7`
- Custom types

### @Vectorize

The `@Vectorize` annotation marks a field for automatic vectorization. The field content will be sent to the configured vectorization service to generate embeddings.

**Location:** `com.datastax.astra.client.collections.mapping.Vectorize`

**Rules:**
- Only one field per class can be annotated with `@Vectorize`
- The field must be of type `String`
- The field is serialized as `$vectorize` in the database
- Requires `vectorizeProvider` and `vectorizeModel` in `@DataApiCollection`

**Example:**

```java
@DataApiCollection(
    name = "articles",
    vectorDimension = 1536,
    vectorizeProvider = "openai",
    vectorizeModel = "text-embedding-ada-002",
    vectorizeSharedSecret = "OPENAI_API_KEY"
)
public class Article {
    @DocumentId
    private String id;
    
    private String title;
    
    @Vectorize
    private String content;  // This will be automatically vectorized
    
    // getters and setters
}
```

### @Lexical

The `@Lexical` annotation marks a field for lexical (full-text) search indexing.

**Location:** `com.datastax.astra.client.collections.mapping.Lexical`

**Rules:**
- Only one field per class can be annotated with `@Lexical`
- The field must be of type `String`
- The field is serialized as `$lexical` in the database
- Requires `lexicalEnabled = true` in `@DataApiCollection`

**Example:**

```java
@DataApiCollection(
    name = "documents",
    lexicalEnabled = true,
    lexicalAnalyzer = AnalyzerTypes.ENGLISH
)
public class Document {
    @DocumentId
    private String id;
    
    @Lexical
    private String searchableText;  // Indexed for full-text search
    
    private String metadata;
    
    // getters and setters
}
```

### @Vector

The `@Vector` annotation marks a field that contains pre-computed vector embeddings.

**Location:** `com.datastax.astra.client.collections.mapping.Vector`

**Rules:**
- Only one field per class can be annotated with `@Vector`
- The field must be of type `float[]` or `DataAPIVector`
- The field is serialized as `$vector` in the database
- Use when you compute embeddings yourself (vs. `@Vectorize` for automatic vectorization)

**Example:**

```java
@DataApiCollection(
    name = "embeddings",
    vectorDimension = 768,
    vectorSimilarity = SimilarityMetric.COSINE
)
public class Embedding {
    @DocumentId
    private String id;
    
    private String text;
    
    @Vector
    private float[] embedding;  // Pre-computed vector
    
    // getters and setters
}
```

**Using DataAPIVector:**

```java
@Vector
private DataAPIVector embedding;

// Usage
embedding = new DataAPIVector(new float[]{0.1f, 0.2f, 0.3f});
```

## Spring Data Integration

The SDK provides Spring Data integration through the `DataApiCollectionCrudRepository` interface, which implements Spring's `CrudRepository` and `QueryByExampleExecutor`.

### Setting Up Dependencies

Add the Spring Boot starter to your `pom.xml`:

```xml
<dependency>
    <groupId>com.datastax.astra</groupId>
    <artifactId>data-api-spring-boot-3x-starter</artifactId>
    <version>${astra-sdk.version}</version>
</dependency>
```

### Creating a Repository

```java
import com.datastax.astra.spring.DataApiCollectionCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends DataApiCollectionCrudRepository<Book, String> {
    // Spring Data methods are automatically available
    // You can also add custom query methods
}
```

### Available Methods

The repository provides all standard Spring Data CRUD operations:

**Basic CRUD:**
```java
// Create
Book book = new Book();
book.setTitle("1984");
book.setAuthor("George Orwell");
bookRepository.save(book);

// Read
Optional<Book> found = bookRepository.findById("book-1");
List<Book> allBooks = (List<Book>) bookRepository.findAll();

// Update
book.setTitle("Nineteen Eighty-Four");
bookRepository.save(book);

// Delete
bookRepository.deleteById("book-1");
bookRepository.delete(book);
```

**Batch Operations:**
```java
List<Book> books = Arrays.asList(book1, book2, book3);
bookRepository.saveAll(books);

List<String> ids = Arrays.asList("book-1", "book-2");
List<Book> found = (List<Book>) bookRepository.findAllById(ids);

bookRepository.deleteAll(books);
```

**Query by Example:**
```java
// Find books by author
Book probe = new Book();
probe.setAuthor("George Orwell");
List<Book> orwellBooks = (List<Book>) bookRepository.findAll(Example.of(probe));

// With sorting
Sort sort = Sort.by(Sort.Direction.DESC, "title");
List<Book> sorted = (List<Book>) bookRepository.findAll(Example.of(probe), sort);

// With pagination
Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
Page<Book> page = bookRepository.findAll(Example.of(probe), pageable);
```

**Data API Filters:**
```java
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;

// Using Data API filters directly
Filter filter = Filters.eq("author", "George Orwell");
List<Book> books = (List<Book>) bookRepository.findAll(filter);

// With sorting
Sort sort = Sort.by("title");
List<Book> sorted = (List<Book>) bookRepository.findAll(filter, sort);

// With pagination
Pageable pageable = PageRequest.of(0, 10);
List<Book> paged = (List<Book>) bookRepository.findAll(filter, pageable);
```

## Configuration

### Application Properties

Configure the Astra DB connection in `application.yml`:

```yaml
astra:
  data-api:
    # Required: Your Astra DB token
    token: ${ASTRA_DB_TOKEN}
    
    # Required: Your database endpoint URL
    endpoint-url: ${ASTRA_DB_ENDPOINT}
    
    # Optional: Keyspace name (default: "default_keyspace")
    keyspace: my_keyspace
    
    # Optional: Schema action (CREATE_IF_NOT_EXISTS, VALIDATE, NONE)
    schema-action: CREATE_IF_NOT_EXISTS
    
    # Optional: Enable request logging
    log-request: true
    
    # Optional: Destination (ASTRA, ASTRA_DEV, ASTRA_TEST, DSE, HCD, CASSANDRA)
    destination: ASTRA
    
    # Optional: Advanced options
    options:
      # HTTP configuration
      http:
        retry-count: 3
        retry-delay: 100
        version: HTTP_2
        redirect: NORMAL
      
      # Timeout configuration (in milliseconds)
      timeout:
        connect: 5000
        request: 10000
        general: 30000
        collection-admin: 60000
      
      # Embedding API key for vectorization
      embedding-api-key: ${OPENAI_API_KEY}
```

### Logging Configuration

To see Data API request/response logs, configure logging in `application.yml`:

```yaml
logging:
  level:
    com.datastax.astra.client.DataAPIClient: DEBUG
```

### Schema Actions

The `schema-action` property controls how collections are managed:

- **`CREATE_IF_NOT_EXISTS`** (default): Creates collections if they don't exist
- **`VALIDATE`**: Validates that collections exist and match the schema
- **`NONE`**: No automatic schema management

## Complete Example

### 1. Document Class

```java
package com.example.demo.model;

import com.datastax.astra.client.collections.mapping.*;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import lombok.Data;

import java.util.Set;

@Data
@DataApiCollection(
    name = "books",
    vectorDimension = 1536,
    vectorSimilarity = SimilarityMetric.COSINE,
    vectorizeProvider = "openai",
    vectorizeModel = "text-embedding-ada-002",
    vectorizeSharedSecret = "OPENAI_API_KEY",
    lexicalEnabled = true,
    lexicalAnalyzer = AnalyzerTypes.ENGLISH
)
public class Book {
    
    @DocumentId
    private String id;
    
    private String title;
    private String author;
    private Integer year;
    private Set<String> genres;
    
    @Vectorize
    private String description;  // Automatically vectorized
    
    @Lexical
    private String fullText;  // Indexed for full-text search
}
```

### 2. Repository Interface

```java
package com.example.demo.repository;

import com.datastax.astra.spring.DataApiCollectionCrudRepository;
import com.example.demo.model.Book;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends DataApiCollectionCrudRepository<Book, String> {
}
```

### 3. Service Layer

```java
package com.example.demo.service;

import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    
    private final BookRepository bookRepository;
    
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }
    
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }
    
    public List<Book> findByAuthor(String author) {
        Book probe = new Book();
        probe.setAuthor(author);
        return (List<Book>) bookRepository.findAll(Example.of(probe));
    }
    
    public List<Book> findByGenre(String genre) {
        Filter filter = Filters.eq("genres", genre);
        return (List<Book>) bookRepository.findAll(filter);
    }
    
    public List<Book> findAll() {
        return (List<Book>) bookRepository.findAll();
    }
    
    public void deleteBook(String id) {
        bookRepository.deleteById(id);
    }
}
```

### 4. REST Controller

```java
package com.example.demo.controller;

import com.example.demo.model.Book;
import com.example.demo.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }
    
    @GetMapping("/{id}")
    public Book getBook(@PathVariable String id) {
        return bookService.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found"));
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
    }
}
```

### 5. Application Configuration

```yaml
# application.yml
astra:
  data-api:
    token: ${ASTRA_DB_TOKEN}
    endpoint-url: ${ASTRA_DB_ENDPOINT}
    keyspace: bookstore
    schema-action: CREATE_IF_NOT_EXISTS
    log-request: true
    options:
      embedding-api-key: ${OPENAI_API_KEY}

logging:
  level:
    com.datastax.astra.client.DataAPIClient: DEBUG
```

## Best Practices

### 1. Annotation Usage

- **Use only one special annotation per field**: A field can have only one of `@DocumentId`, `@Vectorize`, `@Lexical`, or `@Vector`
- **Choose between `@Vectorize` and `@Vector`**: Use `@Vectorize` for automatic vectorization, `@Vector` for pre-computed embeddings
- **Configure collection settings**: Always specify `vectorDimension` when using vector features

### 2. ID Management

```java
// Let the server generate IDs
Book book = new Book();
book.setTitle("1984");
bookRepository.save(book);  // ID will be auto-generated

// Or provide your own ID
book.setId("book-1984");
bookRepository.save(book);
```

### 3. Indexing Strategy

```java
// Index only necessary fields
@DataApiCollection(
    name = "products",
    indexingAllow = {"name", "category", "price"}  // Only index these fields
)

// Or exclude internal fields
@DataApiCollection(
    name = "users",
    indexingDeny = {"password_hash", "internal_notes"}  // Don't index these
)
```

### 4. Vector Search Configuration

```java
// For OpenAI embeddings
@DataApiCollection(
    name = "documents",
    vectorDimension = 1536,  // text-embedding-ada-002
    vectorizeProvider = "openai",
    vectorizeModel = "text-embedding-ada-002",
    vectorizeSharedSecret = "OPENAI_API_KEY"
)

// For NVIDIA embeddings
@DataApiCollection(
    name = "documents",
    vectorDimension = 1024,  // NV-Embed-QA
    vectorizeProvider = "nvidia",
    vectorizeModel = "NV-Embed-QA",
    vectorizeSharedSecret = "NVIDIA_API_KEY"
)
```

### 5. Error Handling

```java
@Service
public class BookService {
    
    public Book createBook(Book book) {
        try {
            return bookRepository.save(book);
        } catch (DataAPIException e) {
            // Handle Data API specific errors
            log.error("Failed to create book: {}", e.getMessage());
            throw new ServiceException("Could not create book", e);
        }
    }
}
```

### 6. Testing

```java
@SpringBootTest
class BookRepositoryTest {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Test
    void testCreateAndFind() {
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        
        Book saved = bookRepository.save(book);
        assertNotNull(saved.getId());
        
        Optional<Book> found = bookRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Book", found.get().getTitle());
    }
    
    @AfterEach
    void cleanup() {
        bookRepository.deleteAll();
    }
}
```

## Additional Resources

- [Astra DB Java SDK Documentation](https://docs.datastax.com/en/astra-db-serverless/api-reference/client-sdks.html)
- [Spring Data Documentation](https://spring.io/projects/spring-data)
- [Vector Search Guide](https://docs.datastax.com/en/astra-db-serverless/databases/vector-search.html)
- [Sample Application](https://github.com/datastax/astra-db-java/tree/main/samples/sample-spring-boot3x)

## Support

For issues and questions:
- GitHub Issues: https://github.com/datastax/astra-db-java/issues
- Community Forum: https://community.datastax.com/
- Documentation: https://docs.datastax.com/
