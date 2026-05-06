# Agent Guide: AstraDB Java SDK

> Comprehensive guide for AI agents working with the AstraDB Java SDK codebase

## 📋 Project Overview

### Identity

| Property | Value |
|----------|-------|
| **Project** | AstraDB Java SDK |
| **Group ID** | `com.datastax.astra` |
| **Artifact** | `astra-db-java-parent` |
| **Version** | `2.1.5-SNAPSHOT` |
| **Java Version** | 17+ (source and target) |
| **Build Tool** | Maven 3.6.3+ |
| **License** | Apache-2.0 |

### Purpose

The AstraDB Java SDK provides a unified client for interacting with:
- **Astra DB Serverless** (cloud-native database)
- **HCD** (Hyper-Converged Database)
- **DSE** (DataStax Enterprise)
- **Apache Cassandra** with Data API

Supports both document-oriented (MongoDB-style) and relational (table-based) data models with vector search capabilities.

---

## 🏗️ Architecture

### Module Structure

```
astra-db-java/              # ⭐ Core SDK (primary development module)
├── client/                 # Public API surface
├── internal/               # Implementation details (not public)
└── test/                   # Unit and integration tests

astra-sdk-devops/           # Astra DevOps API client
integrations/
├── langchain4j-astradb/    # LangChain4j vector store integration
├── data-api-spring-boot-3x-*/  # Spring Boot starters
└── skills/                 # Reusable skill templates

tools/
├── data-api-tools/         # CLI utilities
└── data-api-tools-csv/     # CSV import/export

samples/                    # Example applications
```

### Client Hierarchy

```
DataAPIClient                    # Entry point (token + options)
  └── Database                   # Database-level operations
        ├── Collection<T>        # Document collections (schemaless)
        └── Table<T>             # Relational tables (schema-defined)

Admin Hierarchy:
AstraDBAdmin                     # Organization-level operations
  └── AstraDBDatabaseAdmin       # Astra database admin
DataAPIDatabaseAdmin             # Local/HCD database admin
```

### Package Organization

**Public API** (`com.datastax.astra.client.*`):
```
client/
├── DataAPIClient.java           # Main entry point
├── DataAPIClients.java          # Factory methods
├── DataAPIDestination.java      # Enum: ASTRA | HCD | DSE | CASSANDRA
├── databases/Database.java      # Database operations
├── collections/Collection.java  # Document operations
├── tables/Table.java            # Table operations
├── admin/                       # Admin interfaces
├── core/                        # Query builders, options, auth
├── exceptions/                  # 14 exception types
└── model/                       # Data models (Document, Row)
```

**Internal Implementation** (`com.datastax.astra.internal.*`):
```
internal/
├── http/RetryHttpClient.java    # HTTP client with retry logic
├── serdes/                      # Jackson serializers/deserializers
├── command/                     # Command execution engine
├── api/                         # API endpoint routing
└── utils/                       # Helper utilities
```

---

## 🚀 Development Setup

### Quick Start

```bash
# Clone repository
git clone https://github.com/datastax/astra-db-java.git
cd astra-db-java

# Build without tests
mvn clean install -DskipTests

# Build core module only
mvn clean install -pl astra-db-java -DskipTests
```

### Local Development Environment

#### 1. Start DSE/Cassandra Backend

```bash
# Using Docker Compose
docker-compose up -d
```

#### 2. Start Data API Server

```bash
cd $DATA_API_FOLDER

# Required environment variables
export STARGATE_DATA_STORE_SAI_ENABLED=true
export STARGATE_DATA_STORE_VECTOR_SEARCH_ENABLED=true
export STARGATE_JSONAPI_OPERATIONS_VECTORIZE_ENABLED=true
export STARGATE_DATA_STORE_IGNORE_BRIDGE=true
export STARGATE_JSONAPI_OPERATIONS_DATABASE_CONFIG_LOCAL_DATACENTER=dc1
export STARGATE_JSONAPI_OPERATIONS_DATABASE_CONFIG_CASSANDRA_END_POINTS=localhost

# Start in dev mode
mvn quarkus:dev
```

#### 3. Configure Test Credentials

```bash
# Copy templates
cp src/test/resources/test-config-astra.properties.template \
   src/test/resources/test-config-astra.properties

cp src/test/resources/test-config-embedding-providers.properties.template \
   src/test/resources/test-config-embedding-providers.properties

# Edit and add your tokens/API keys
# ⚠️ These files are gitignored - never commit them!
```

### Connection Examples

**Astra DB Serverless:**
```java
DataAPIClient client = new DataAPIClient("AstraCS:...");
Database db = client.getDatabase("https://01234567-....apps.astra.datastax.com");
```

**Local HCD/DSE:**
```java
TokenProvider tp = new UsernamePasswordTokenProvider("cassandra", "cassandra");
DataAPIClient client = new DataAPIClient(tp);
Database db = client.getDatabase("http://localhost:8181");
```

**Default local token:** `Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh` (cassandra/cassandra)

---

## 🧪 Testing Strategy

### Test Organization

```
src/test/java/com/datastax/astra/test/
├── unit/                        # Unit tests (no external dependencies)
│   ├── core/                    # Filters, sorts, projections
│   ├── collections/             # Document serialization
│   ├── tables/                  # Row builders, table definitions
│   └── admin/                   # API endpoint parsing
│
└── integration/                 # Integration tests
    ├── AbstractDataAPITest.java # Base class for all ITs
    ├── Abstract*IT.java         # Abstract test suites
    ├── local/                   # Local*IT (HCD/DSE tests)
    └── astra/                   # Astra*IT (cloud tests)
```

### Test Inheritance Pattern

Every integration test class:
1. Extends `AbstractDataAPITest` (provides `getDatabase()`, config access)
2. Uses `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`
3. Cleans up in `@BeforeAll` with `dropAllCollections()` + `dropAllTables()`
4. Uses `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` with `@Order`

**Exception:** `AbstractDatabaseAdminIT` skips cleanup to avoid premature database creation.

### Running Tests

```bash
# All tests against local HCD/DSE
mvn clean verify -pl astra-db-java -Plocal

# All tests against Astra DEV
mvn clean verify -pl astra-db-java -Pastra-dev

# All tests against Astra PROD
mvn clean verify -pl astra-db-java -Pastra-prod

# Single test class
mvn test -pl astra-db-java -Dtest="LocalCollectionIT"

# With coverage report
mvn clean test jacoco:report -pl astra-db-java -Pastra-prod
```

**Coverage report:** `astra-db-java/target/site/jacoco/index.html`

### Maven Profiles

| Profile | Environment | Cloud | Region | Description |
|---------|-------------|-------|--------|-------------|
| `local` | `local` | GCP | us-east1 | Local HCD/DSE |
| `astra-dev` | `astra_dev` | GCP | us-central1 | Astra development |
| `astra-prod` | `astra_prod` | AWS | us-east-2 | Astra production |
| `skip-tests` | - | - | - | Skip all tests |

### Test Configuration

**Files in `src/test/resources/`:**

| File | Purpose | Committed |
|------|---------|-----------|
| `test-config.properties` | Default settings | ✅ Yes |
| `test-config-local.properties` | Local overrides | ✅ Yes |
| `test-config-astra.properties` | Astra credentials | ❌ No (gitignored) |
| `test-config-embedding-providers.properties` | API keys | ❌ No (gitignored) |
| `junit-platform.properties` | JUnit 5 config | ✅ Yes |
| `logback-test.xml` | Test logging | ✅ Yes |

**Configuration priority:** Environment variables > System properties > Config files

### Custom Test Annotations

```java
@EnabledIfLocalAvailable   // Skip if local HCD unreachable
@EnabledIfAstra            // Skip if no Astra token or wrong environment
```

Tests for unavailable environments are **skipped** (not failed).

### Environment Variables (Optional)

| Variable | Description |
|----------|-------------|
| `ASTRA_DB_APPLICATION_TOKEN` | Astra PROD token |
| `ASTRA_DB_APPLICATION_TOKEN_DEV` | Astra DEV token |
| `ASTRA_DB_JAVA_TEST_ENV` | `local` \| `astra_dev` \| `astra_prod` |
| `ASTRA_CLOUD_PROVIDER` | `AWS` \| `GCP` \| `AZURE` |
| `ASTRA_CLOUD_REGION` | e.g. `us-east1`, `eu-west-1` |

---

## 🔧 Common Operations

### Adding a New Collection/Table Operation

**Step-by-step:**

1. **Define options class**
   - Location: `client/collections/commands/` or `client/tables/commands/`
   - Use `@Getter`, `@Setter`, `@Builder` (Lombok)
   - Follow fluent builder pattern

2. **Add method to Collection/Table**
   - Location: `Collection.java` or `Table.java`
   - Use options pattern for parameters
   - Return appropriate result type

3. **Add serialization (if needed)**
   - Location: `internal/serdes/collections/` or `internal/serdes/tables/`
   - Register in `DataAPISerializer.java` or `DatabaseSerializer.java`

4. **Write unit test**
   - Location: `test/unit/collections/` or `test/unit/tables/`
   - Test serialization, validation, edge cases

5. **Write integration tests**
   - Create abstract test class extending appropriate base
   - Add concrete implementations in `local/` and `astra/`
   - Use `@Order` annotations for test sequence

6. **Verify**
   ```bash
   mvn clean install -DskipTests
   mvn test -pl astra-db-java -Dtest="YourTestClass"
   ```

### Adding a New Integration Test Class

**Template:**

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractYourFeatureIT extends AbstractDataAPITest {
    
    @BeforeAll
    public void setup() {
        dropAllCollections();
        dropAllTables();
        // Your setup code
    }
    
    @Test
    @Order(1)
    public void testFeature() {
        // Test implementation
    }
}
```

**Concrete implementations:**

```java
// Local version
@EnabledIfLocalAvailable
public class LocalYourFeatureIT extends AbstractYourFeatureIT {
}

// Astra version
@EnabledIfAstra
public class AstraYourFeatureIT extends AbstractYourFeatureIT {
}
```

### Adding a Vectorize Provider Test

**Extend the appropriate base:**

```java
public class YourProviderVectorizeIT extends AbstractVectorizeApiHeaderIT {
    
    @Override
    protected String getEmbeddingProviderId() {
        return "yourProvider";
    }
    
    @Override
    protected String getApiKey() {
        return testConfig.getEmbeddingApiKey("YOUR_PROVIDER_KEY");
    }
    
    @Override
    protected Map<String, Object> getAuthenticationParameters() {
        return Map.of("apiKey", getApiKey());
    }
}
```

For non-standard auth (e.g., AWS Bedrock), override `getEmbeddingAuthProvider()`.

### Modifying HTTP Layer

**⚠️ Critical:** All HTTP logic is in `internal/http/RetryHttpClient.java`

Changes here affect **every API call**. Test thoroughly:
- Retry behavior
- Timeout handling
- Error responses
- Connection pooling
- Proxy support

---

## 📚 Best Practices

### Code Conventions

- **Lombok everywhere:** `@Getter`, `@Setter`, `@Slf4j`, `@Builder`
- **Fluent builders** for all configuration/options classes
- **Options pattern:** Each operation has dedicated options class
- **Public vs Internal:** Public API in `client/`, implementation in `internal/`
- **No style enforcement:** Convention-based (no checkstyle/spotless)
- **License headers:** Enforced via `license-maven-plugin`

### Data Models

**Collections (Document-oriented):**
- `Collection<T>` stores flexible JSON documents
- `Document` is `Map<String, Object>` with two access modes:
  - **Plain:** `put`/`get` (literal keys)
  - **Escaping-aware:** `append`/`read` (dot-notation for nested maps)
- Supports vector search, vectorize, hybrid search

**Tables (Relational):**
- `Table<T>` stores rows with defined schema
- `Row` provides typed column access
- Requires `TableDefinition` with column types and partition key
- POJO mapping via Jackson annotations

### Security

**⚠️ Never commit:**
- `test-config-astra.properties` (contains tokens)
- `test-config-embedding-providers.properties` (contains API keys)
- `.env` files
- Any file with tokens, passwords, or API keys

**Gitignored patterns:**
```
.env
.astrarc
sec/
*.log
test-config-astra.properties
test-config-embedding-providers.properties
```

---

## 📦 Dependencies

### Core Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Jackson | 2.20.1 | JSON serialization |
| Apache HttpClient 5 | 5.5.1 | HTTP transport |
| SLF4J | 2.0.17 | Logging API |
| Logback | 1.5.21 | Logging implementation |
| Lombok | 1.18.42 | Boilerplate reduction |
| Retry4j | 0.15.0 | Retry logic |

### Integration Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| LangChain4j | 1.10.0 | AI framework integration |
| Spring Boot | 3.x | Spring integration |

### Testing Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| JUnit Jupiter | 5.14.1 | Testing framework |
| AssertJ | 3.27.6 | Fluent assertions |
| JaCoCo | - | Code coverage |

---

## 🚢 Release Process

### Maven Release

```bash
# Prepare release (updates versions, creates tag)
mvn -pl astra-db-java,langchain4j-astradb -am release:prepare -DskipTests=true

# Perform release (builds and deploys)
mvn -pl astra-db-java,langchain4j-astradb -am release:perform -DskipTests=true
```

**Publishing:**
- Artifacts published to Maven Central
- Via Sonatype Central Publishing
- GPG signing required

### CI/CD Workflows

**GitHub Actions** (`.github/workflows/`):

| Workflow | Target | Purpose |
|----------|--------|---------|
| `ci-astra-dev.yml` | Astra DEV | Full test suite |
| `ci-astra-dev-collection.yml` | Astra DEV | Collection operations |
| `ci-astra-dev-database.yml` | Astra DEV | Database operations |
| `ci-astra-dev-databaseadmin.yml` | Astra DEV | Database admin |
| `ci-astra-dev-vectorize-*.yml` | Astra DEV | Provider-specific vectorize |
| `ci-astra-prod-devops.yml` | Astra PROD | DevOps API |

---

## 🔍 Reference

### Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Root Maven configuration |
| `astra-db-java/pom.xml` | Core module configuration |
| `DOCUMENT-API.md` | API documentation |
| `CONTRIBUTING.md` | Contribution guidelines |
| `RELEASE.MD` | Release procedures |

### Important Classes

**Entry Points:**
- `DataAPIClient` - Main client
- `DataAPIClients` - Factory methods
- `Database` - Database operations
- `Collection<T>` - Document operations
- `Table<T>` - Table operations

**Core Types:**
- `Document` - Document container
- `Row` - Table row container
- `Filter` - Query filter builder
- `Projection` - Field projection builder
- `Sort` - Sort specification builder

**Admin:**
- `AstraDBAdmin` - Org-level admin
- `AstraDBDatabaseAdmin` - Astra database admin
- `DataAPIDatabaseAdmin` - Local database admin

**Exceptions:**
- `DataAPIException` - Base exception
- 14 specific exception types for different error scenarios

### Useful Commands

```bash
# Build everything
mvn clean install -DskipTests

# Build core only
mvn clean install -pl astra-db-java -DskipTests

# Run specific test
mvn test -pl astra-db-java -Dtest="TestClassName"

# Generate coverage report
mvn clean test jacoco:report -pl astra-db-java

# Check for dependency updates
mvn versions:display-dependency-updates

# Format license headers
mvn license:format
```

---

## 📞 Support

- **Documentation:** [docs.datastax.com](https://docs.datastax.com)
- **Issues:** [GitHub Issues](https://github.com/datastax/astra-db-java/issues)
- **Community:** [DataStax Community](https://community.datastax.com)

---

*Last updated: 2026-05-06*