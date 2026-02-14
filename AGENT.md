# AGENT.md

Codex / AI agent guide for working with the **AstraDB Java SDK** (`astra-db-java`).

## Identity

| Field | Value |
|-------|-------|
| **Group ID** | `com.datastax.astra` |
| **Artifact** | `astra-db-java-parent` |
| **Version** | `2.1.5-SNAPSHOT` |
| **Java** | 17+ (source and target) |
| **Maven** | 3.6.3+ |
| **License** | Apache-2.0 |

## Repository Layout

```
astra-db-java/           # Core SDK (main module — most work happens here)
astra-sdk-devops/         # Astra DevOps API client
langchain4j-astradb/      # LangChain4j vector-store integration
astra-db-java-tools/      # CSV utilities & extras
astra-db-java-tests/      # (Deprecated) legacy test modules — do NOT use
```

## Build Commands

```bash
# Full build, no tests
mvn clean install -DskipTests

# Build core module only, no tests
mvn clean install -pl astra-db-java -DskipTests

# Run all tests against local HCD/DSE (with coverage report)
mvn clean verify -pl astra-db-java -Plocal

# Run all tests against Astra DEV (with coverage report)
mvn clean verify -pl astra-db-java -Pastra-dev

# Run all tests against Astra PROD (with coverage report)
mvn clean verify -pl astra-db-java -Pastra-prod

# Run tests and generate JaCoCo report explicitly
mvn clean test jacoco:report -pl astra-db-java -Pastra-prod

# Run a single test class
mvn test -pl astra-db-java -Dtest="LocalCollectionIT"

# Skip all tests (explicit profile)
mvn clean install -pl astra-db-java -Pskip-tests
```

The JaCoCo coverage report is generated at `astra-db-java/target/site/jacoco/index.html`.

### Maven Profiles

| Profile | `test.environment` | Cloud Provider | Region | Description |
|---------|--------------------|----------------|--------|-------------|
| `local` | `local` | GCP | us-east1 | Local HCD/DSE instance |
| `astra-dev` | `astra_dev` | GCP | us-central1 | Astra development environment |
| `astra-prod` | `astra_prod` | AWS | us-east-2 | Astra production environment |
| `skip-tests` | - | - | - | Skip all tests |

Profile values are passed as system properties, which take priority over config file values but not environment variables.

## Source Code Map

Root package: `com.datastax.astra`

### Public API (`client/`)

```
client/
  DataAPIClient.java              # Entry point — token, options, database access
  DataAPIClients.java             # Factory helpers
  DataAPIDestination.java         # Enum: DSE | ASTRA | HCD | CASSANDRA
  databases/
    Database.java                 # Database operations, collection/table management
  collections/
    Collection.java               # Document-oriented ops (MongoDB-style)
  tables/
    Table.java                    # Row-oriented ops (relational-style)
    mapping/                      # @EntityTable, @Column, @PartitionBy annotations
  admin/
    AstraDBAdmin.java             # Org-level Astra operations
    AstraDBDatabaseAdmin.java     # Astra-specific database admin
    DataAPIDatabaseAdmin.java     # Generic Data API admin (local/HCD)
    DatabaseAdmin.java            # Base database admin
  core/
    query/                        # Filter, Projection, Sort builders
    options/                      # Shared option classes
    auth/                         # Token/auth providers
    vector/                       # Vector search options
    vectorize/                    # Vectorization provider config
    rerank/                       # Re-ranking options
    hybrid/                       # Hybrid search options
  exceptions/                     # 14 exception types (DataAPIException hierarchy)
```

### Internal (`internal/`) — NOT public API

```
internal/
  http/RetryHttpClient.java       # HTTP with retries, timeouts, proxy
  serdes/                         # Jackson serializers/deserializers
    collections/                  # Document serializers (ObjectId, UUID, Date)
    tables/                       # Row serializers with type mapping
  command/                        # Command execution engine
  api/                            # API endpoint routing
  reflection/                     # Reflection utilities
  utils/                          # General helpers
```

## Client Hierarchy

```
DataAPIClient                       # holds token + client options
  └── Database                      # database-level operations
        ├── Collection<T>           # document collection (MongoDB-style)
        └── Table<T>                # relational table
```

Admin hierarchy:

```
AstraDBAdmin                        # org-level (create/delete databases)
  └── AstraDBDatabaseAdmin          # Astra database admin (keyspaces)
DataAPIDatabaseAdmin                # local/HCD database admin
```

## Connecting to a Database

### Astra DB Serverless

```java
DataAPIClient client = new DataAPIClient("AstraCS:...");
Database database = client.getDatabase("https://01234567-....apps.astra.datastax.com");
```

### HCD / Local Instance

```java
TokenProvider tp = new UsernamePasswordTokenProvider("cassandra", "cassandra");
DataAPIClient client = new DataAPIClient(tp);
Database database = client.getDatabase("http://localhost:8181");
```

## Data Models

### Collections and Documents

- `Collection<T>` stores flexible JSON documents (schemaless, MongoDB-style)
- `Document` is the primary data container: `Map<String, Object>` with two access modes:
  - **Plain** (`put`/`get`): literal keys, no parsing
  - **Escaping-aware** (`append`/`read`): dot-notation navigates nested maps, `&.` for literal dots
- Supports vector search, server-side embeddings (vectorize), and hybrid search

### Tables and Rows

- `Table<T>` stores rows with defined column types (relational-style, schema-defined)
- `Row` is the data container with typed column access
- Requires `TableDefinition` with column types and partition key
- POJO mapping via Jackson annotations (`@JsonProperty`) for both `Collection<T>` and `Table<T>`

## Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jackson | 2.20.1 | JSON serialization |
| Apache HttpClient 5 | 5.5.1 | HTTP transport |
| SLF4J | 2.0.17 | Logging API |
| Logback | 1.5.21 | Logging implementation |
| Lombok | 1.18.42 | Boilerplate reduction |
| Retry4j | 0.15.0 | Retry logic |
| LangChain4j | 1.10.0 | AI framework integration |
| JUnit Jupiter | 5.14.1 | Testing |
| AssertJ | 3.27.6 | Fluent assertions |

## Testing

### Test structure

All tests live under `astra-db-java/src/test/java/com/datastax/astra/test/`.

```
test/
  unit/                              # Unit tests (no external dependencies)
    core/                            # Filters, sorts, projections, options, exceptions
    collections/                     # Document serialization, collection definitions
    tables/                          # Row builders, table definitions, update operations
    admin/                           # API endpoint parsing
  integration/
    AbstractDataAPITest.java         # Root: config, client, database lifecycle
    ├── AbstractDatabaseAdminIT      # Keyspace management tests
    ├── AbstractCollectionDDLIT      # Collection DDL (create/drop/list)
    ├── AbstractCollectionIT         # Collection CRUD operations
    ├── AbstractCollectionDefaultIdIT # Default ID generation strategies
    ├── AbstractCollectionVectorSearchIT # Vector search on collections
    ├── AbstractCollectionFindAndRerankIT # Find + rerank operations
    ├── AbstractTableDDLIT           # Table DDL (create/drop/list)
    ├── AbstractTableIT              # Table CRUD operations
    ├── AbstractTableIndexIT         # Table index management
    ├── AbstractTableUdtIT           # User-defined types
    ├── AbstractTableVectorSearchIT  # Vector search on tables
    ├── AbstractVectorizeIT          # Base vectorize tests (shared key)
    │   └── AbstractVectorizeApiHeaderIT # API key header-based vectorize
    local/                           # Local*IT — requires local HCD/DSE + Data API
      HCD_02_DatabaseAdminIT
      HCD_Collections_00_SchemaIT
      HCD_Collections_01_CrudIT
      Local05_CollectionDefaultIdIT
      Local06_CollectionVectorSearchIT
      Local08_TableIT
      Local09_TableVectorSearchIT
      LocalVectorize*IT              # Provider-specific vectorize tests
    astra/                           # Astra*IT — requires Astra token
      Astra_01_AstraDBAdminIT
      Astra_02_DatabaseAdminIT
      Astra_Collections_00_SchemaIT
      Astra_Collections_01_CrudIT
      Astra_Collections_02_DefaultIdIT
      Astra_Collections_03_VectorSearchIT
      Astra_Collections_04_FindAndRerankIT
      Astra_Collections_05+_Vectorize_*IT  # Provider-specific vectorize tests
      Astra_Tables_00_SchemaIT
      Astra_Tables_01_CrudIT
      Astra_Tables_02_VectorSearchIT
      Astra_Tables_03_IndexIT
      Astra_Tables_04_UdtIT
    utils/                           # Test infrastructure
      TestConfig.java                # Layered config loading
      TestDataset.java               # Test data fixtures
      EnabledIfAstra.java            # @EnabledIfAstra annotation
      EnabledIfLocalAvailable.java   # @EnabledIfLocalAvailable annotation
      AstraOnlyCondition.java        # Condition: checks config.isAstra() + token
      LocalAvailableCondition.java   # Condition: checks local HCD reachability
```

### Test inheritance pattern

Every abstract IT class:
- Extends `AbstractDataAPITest` (provides `getDatabase()`, `getDatabaseAdmin()`, config access)
- Uses `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` for non-static `@BeforeAll`
- Cleans up with `dropAllCollections()` + `dropAllTables()` in `@BeforeAll` (resets to default keyspace)
- Uses `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` with `@Order` annotations

Exception: `AbstractDatabaseAdminIT` does NOT call `dropAllCollections()`/`dropAllTables()` in `@BeforeAll` because it would trigger premature database creation via the DevOps API.

The `database` field in `AbstractDataAPITest` is **static** (shared across all test classes). The cleanup methods call `getDatabase().useKeyspace(DEFAULT_KEYSPACE)` to prevent cross-class keyspace contamination from admin tests.

### Test configuration

Files in `astra-db-java/src/test/resources/`:

| File | Purpose |
|------|---------|
| `test-config.properties` | Default settings (committed) |
| `test-config-local.properties` | Local HCD/DSE overrides (committed) |
| `test-config-astra.properties` | Astra credentials (**gitignored**) |
| `test-config-astra.properties.template` | Template — copy and fill in token |
| `test-config-embedding-providers.properties` | Embedding API keys (**gitignored**) |
| `test-config-embedding-providers.properties.template` | Template — copy and fill in API keys |
| `junit-platform.properties` | JUnit 5 config (ordered execution, sequential) |
| `logback-test.xml` | Test logging |

**Priority order:** Environment variables > System properties > Config files.

### Custom test annotations

```java
@EnabledIfLocalAvailable   // skips if local HCD unreachable
@EnabledIfAstra            // skips if no valid Astra token or not Astra environment
```

Tests for unavailable environments are **skipped** (not failed), so the full suite can always be run safely.

### Surefire configuration

The Maven Surefire plugin is configured to include both `*Test.java` (unit tests) and `*IT.java` (integration tests):

```xml
<includes>
    <include>**/*Test.java</include>
    <include>**/*IT.java</include>
</includes>
```

### Environment variables (optional, for CI or overrides)

| Variable | Description |
|----------|-------------|
| `ASTRA_DB_APPLICATION_TOKEN` | Astra PROD token |
| `ASTRA_DB_APPLICATION_TOKEN_DEV` | Astra DEV token |
| `ASTRA_DB_JAVA_TEST_ENV` | `local`, `astra_dev`, or `astra_prod` |
| `ASTRA_CLOUD_PROVIDER` | `AWS`, `GCP`, `AZURE` |
| `ASTRA_CLOUD_REGION` | e.g. `us-east1`, `eu-west-1` |

## Local Development

```bash
# 1. Start DSE via Docker (port 9042)
docker-compose up -d

# 2. Start Data API (from separate data-api repo clone)
#    See TEST.MD for full env-var list
cd $DATA_API_FOLDER
STARGATE_DATA_STORE_SAI_ENABLED=true \
STARGATE_DATA_STORE_VECTOR_SEARCH_ENABLED=true \
STARGATE_JSONAPI_OPERATIONS_VECTORIZE_ENABLED=true \
STARGATE_DATA_STORE_IGNORE_BRIDGE=true \
STARGATE_JSONAPI_OPERATIONS_DATABASE_CONFIG_LOCAL_DATACENTER=dc1 \
STARGATE_JSONAPI_OPERATIONS_DATABASE_CONFIG_CASSANDRA_END_POINTS=localhost \
mvn quarkus:dev

# 3. Local token format: Cassandra:Base64(user):Base64(pass)
#    Default: Cassandra:Y2Fzc2FuZHJh:Y2Fzc2FuZHJh  (cassandra/cassandra)
#    Endpoint: http://localhost:8181
```

## Code Conventions

- **Lombok** everywhere: `@Getter`, `@Setter`, `@Slf4j`, `@Builder`, etc.
- **Fluent builders** for all configuration/options classes.
- **Options pattern**: each operation has a dedicated options class (e.g. `CollectionFindOptions`, `TableInsertManyOptions`).
- Public API lives in `client/`; implementation details in `internal/` — do not expose internal types in the public API.
- Jackson custom serializers/deserializers in `internal/serdes/` — collections and tables each have their own set.
- No checkstyle or spotless — style is enforced through convention and IDE settings.
- Apache 2.0 license headers are enforced via `license-maven-plugin`.

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

| Workflow | Target |
|----------|--------|
| `ci-astra-dev.yml` | Astra DEV (full suite) |
| `ci-astra-dev-new.yml` | Astra DEV (new tests) |
| `ci-astra-dev-collection.yml` | Collection operations on DEV |
| `ci-astra-dev-database.yml` | Database operations on DEV |
| `ci-astra-dev-databaseadmin.yml` | Database admin on DEV |
| `ci-astra-dev-devops.yml` | DevOps API on DEV |
| `ci-astra-dev-vectorize-aws-bedrock.yml` | AWS Bedrock vectorization |
| `ci-astra-dev-vectorize-hf-dedicated.yml` | HuggingFace Dedicated vectorization |
| `ci-astra-prod-devops.yml` | DevOps API on PROD |
| `ci-astra-col-db-dbadmin.yaml` | Combined collection/db/admin |
| `ci-astra-devops.yaml` | Comprehensive DevOps |
| `ci-astra-vectorize-bedrock-hf.yaml` | Combined Bedrock + HF vectorize |

## Release

```bash
# Prepare + perform release (core + langchain4j modules)
mvn -pl astra-db-java,langchain4j-astradb -am release:prepare -DskipTests=true
mvn -pl astra-db-java,langchain4j-astradb -am release:perform -DskipTests=true
```

Artifacts are published to Maven Central via Sonatype Central Publishing with GPG signing.

## Common Tasks for Agents

### Adding a new operation to Collection or Table

1. Define the options class in `client/collections/commands/` or `client/tables/commands/`.
2. Add the method to `Collection.java` or `Table.java`.
3. If serialization is needed, add serializer/deserializer in `internal/serdes/collections/` or `internal/serdes/tables/`.
4. Add unit test in `astra-db-java/src/test/java/com/datastax/astra/test/unit/`.
5. Add integration test in the appropriate `integration/local/` or `integration/astra/` package.
6. Build and verify: `mvn clean install -DskipTests && mvn test -pl astra-db-java -Dtest="YourTestClass"`.

### Adding a new integration test class

1. Create the abstract class extending `AbstractDataAPITest` (or a more specific abstract like `AbstractCollectionIT`).
2. Add `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` and `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`.
3. Add `@BeforeAll` method calling `dropAllCollections()` and `dropAllTables()` for clean state.
4. Create concrete subclasses in `local/` (with `@EnabledIfLocalAvailable`) and `astra/` (with `@EnabledIfAstra`).
5. Use `@Order` annotations on test methods to control execution sequence.

### Adding a new vectorize provider test

1. Create class extending `AbstractVectorizeApiHeaderIT` (for API-key providers) or `AbstractVectorizeIT` (for shared-key providers).
2. Implement: `getEmbeddingProviderId()`, `getApiKey()`, `getAuthenticationParameters()`.
3. If the provider uses non-standard auth (e.g. AWS Bedrock with access+secret keys), override `getEmbeddingAuthProvider()` to return the appropriate `EmbeddingHeadersProvider`.
4. Add config key support in `TestConfig.java` if needed.

### Adding a new core type

1. Add the type in `client/core/` (under the appropriate sub-package).
2. If it needs custom JSON handling, add serializer/deserializer in `internal/serdes/`.
3. Register the serializer in `DataAPISerializer.java` or `DatabaseSerializer.java`.

### Modifying the HTTP layer

All HTTP logic is in `internal/http/RetryHttpClient.java`. Changes here affect every API call — test thoroughly.

## Files to Never Commit

- `test-config-astra.properties` (contains tokens)
- `test-config-embedding-providers.properties` (contains API keys)
- `.env` files
- Any file containing tokens, passwords, or API keys
