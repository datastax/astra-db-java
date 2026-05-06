# Integrations

This module contains integration libraries for various frameworks and platforms.

## Modules

### 1. langchain4j-astradb

Integration with LangChain4j for vector store and embedding capabilities using AstraDB.

**Artifact:**
```xml
<dependency>
    <groupId>com.datastax.astra</groupId>
    <artifactId>langchain4j-astradb</artifactId>
    <version>2.2.1-SNAPSHOT</version>
</dependency>
```

### 2. data-api-spring-boot-3x-autoconfigure

Spring Boot 3.x auto-configuration module for DataAPI Client. This module provides:
- `DataAPIClientProperties` - Configuration properties class with prefix `astra.data-api`
- `DataAPIAutoConfiguration` - Auto-configuration for `DataAPIClient` and `Database` beans
- Automatic registration via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Package:** `com.datastax.astra.boot.autoconfigure`

**Configuration Properties:**
```yaml
astra:
  data-api:
    token: ${ASTRA_DB_APPLICATION_TOKEN}
    endpoint-url: ${ASTRA_DB_API_ENDPOINT}
    keyspace: ${ASTRA_DB_KEYSPACE:default_keyspace}
    options:
      http:
        connect-timeout: 10000
        request-timeout: 10000
      timeout:
        general-method-timeout: 30000
        collection-admin-timeout: 60000
        table-admin-timeout: 60000
        database-admin-timeout: 60000
        keyspace-admin-timeout: 60000
```

### 3. astra-spring-boot-3x-starter
Spring Boot 3.x starter module that provides a convenient way to include DataAPI Client in Spring Boot applications.

**Usage:**
```xml
<dependency>
    <groupId>com.datastax.astra</groupId>
    <artifactId>astra-spring-boot-3x-starter</artifactId>
    <version>2.2.1-SNAPSHOT</version>
</dependency>
```

This starter automatically includes:
- `data-api-spring-boot-3x-autoconfigure` module
- `spring-boot-starter` dependencies
- All necessary DataAPI Client dependencies

**Auto-configured Beans:**
- `DataAPIClient` - Configured from `astra.data-api` properties
- `Database` - Configured with endpoint-url and optional keyspace

## Building

```bash
cd integrations
mvn clean install
```

## Spring Boot Integration Example

1. Add the starter dependency to your `pom.xml`
2. Configure properties in `application.yaml`:
```yaml
astra:
  data-api:
    token: ${ASTRA_DB_APPLICATION_TOKEN}
    endpoint-url: ${ASTRA_DB_API_ENDPOINT}
    keyspace: default_keyspace
```

3. Inject beans in your application:
```java
@Service
public class MyService {
    
    @Autowired
    private DataAPIClient client;
    
    @Autowired
    private Database database;
    
    public void doSomething() {
        // Use client or database
    }
}
```
