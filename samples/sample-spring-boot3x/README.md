# Sample Spring Boot 3.x Application with DataStax Astra DB

This sample demonstrates how to use the DataStax Astra DB Java SDK with Spring Boot 3.x, including the new Spring Data CRUD repositories for both Collections and Tables.

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- DataStax Astra DB account with a database created
- Application token with appropriate permissions

## Configuration

### 1. Set Environment Variable

Set your Astra DB application token as an environment variable:

```bash
export ASTRA_DB_APPLICATION_TOKEN="your-token-here"
```

### 2. Update application.yaml

Edit `src/main/resources/application.yaml` and update the following:

```yaml
astra:
  data-api:
    token: ${ASTRA_DB_APPLICATION_TOKEN}
    endpoint-url: https://your-database-id-region.apps.astra.datastax.com
    keyspace: default_keyspace
    schema-action: CREATE_IF_NOT_EXISTS
```

**Configuration Options:**

- `endpoint-url`: Your Astra DB API endpoint (found in Astra DB console)
- `keyspace`: The keyspace to use (default: `default_keyspace`)
- `schema-action`: 
  - `CREATE_IF_NOT_EXISTS`: Auto-create collections/tables if they don't exist
  - `VALIDATE`: Validate that collections/tables match entity definitions
  - `NONE`: Assume collections/tables already exist

## Running the Application

### Using Maven

```bash
# From the project root
cd samples/sample-spring-boot3x

# Run the application
mvn spring-boot:run
```

### Using Java

```bash
# Build the project
mvn clean package

# Run the JAR
java -jar target/sample-spring-boot3x-2.2.1-SNAPSHOT.jar
```

The application will start on port **8081** (configured in `application.yaml`).

## Available REST API Endpoints

### Health Check

```bash
# Simple hello endpoint
curl http://localhost:8081/api/hello
```

**Response:**
```json
{
  "message": "Hello from DataAPI Spring Boot!",
  "status": "running"
}
```

### Database Information Endpoints

#### Get Complete Database Info

```bash
curl http://localhost:8081/api/database/info
```

**Response:**
```json
{
  "keyspace": "default_keyspace",
  "collections": ["c_book_auto"],
  "collectionsCount": 1,
  "tables": [],
  "tablesCount": 0,
  "types": [],
  "typesCount": 0,
  "status": "success"
}
```

#### Get Current Keyspace

```bash
curl http://localhost:8081/api/database/keyspace
```

**Response:**
```json
{
  "keyspace": "default_keyspace",
  "status": "success"
}
```

#### List Collections

```bash
curl http://localhost:8081/api/database/collections
```

**Response:**
```json
{
  "collections": ["c_book_auto"],
  "count": 1,
  "status": "success"
}
```

#### List Tables

```bash
curl http://localhost:8081/api/database/tables
```

**Response:**
```json
{
  "tables": [],
  "count": 0,
  "status": "success"
}
```

#### List User-Defined Types

```bash
curl http://localhost:8081/api/database/types
```

**Response:**
```json
{
  "types": [],
  "count": 0,
  "status": "success"
}
```

#### Get Collection Details with Schema

```bash
curl http://localhost:8081/api/database/collections/details
```

**Response:** Returns detailed schema information for each collection including vector configuration, indexing options, etc.

#### Get Table Details with Schema

```bash
curl http://localhost:8081/api/database/tables/details
```

**Response:** Returns detailed schema information for each table including columns, primary keys, etc.

## Project Structure

```
src/main/java/com/ibm/astra/demo/
├── DataApiStarterSpringBootApplication.java  # Main Spring Boot application
├── books/
│   ├── Book.java                             # Collection entity with @DataApiCollection
│   ├── BookRepository.java                   # Spring Data repository for Book collection
│   └── DataSet.java                          # Sample data loader
├── config/                                   # Configuration classes
└── controller/
    ├── HelloController.java                  # Simple health check endpoint
    └── DatabaseInfoController.java           # Database metadata REST API
```

## Key Features Demonstrated

### 1. Collection Repository Pattern

The `BookRepository` extends `DataApiCollectionCrudRepository` to provide automatic CRUD operations:

```java
@Repository
public class BookRepository extends DataApiCollectionCrudRepository<Book, String> {
    // No implementation needed - all CRUD methods inherited
}
```

### 2. Auto-Schema Creation

The `Book` entity is annotated with `@DataApiCollection` which automatically creates the collection with:
- Vector search (1024 dimensions, COSINE similarity)
- Vectorization service (NVIDIA NV-Embed-QA)
- Lexical search (STANDARD analyzer)
- Reranking service (NVIDIA llama-3.2-nv-rerankqa-1b-v2)

### 3. Database Metadata API

The `DatabaseInfoController` provides REST endpoints to inspect:
- Current keyspace
- Available collections and their schemas
- Available tables and their schemas
- User-defined types

## Troubleshooting

### Connection Issues

If you see connection errors:
1. Verify your `ASTRA_DB_APPLICATION_TOKEN` is set correctly
2. Check that the `endpoint-url` in `application.yaml` matches your database
3. Ensure your token has appropriate permissions

### Schema Validation Errors

If you see schema mismatch errors with `schema-action: VALIDATE`:
1. Switch to `CREATE_IF_NOT_EXISTS` to auto-create collections/tables
2. Or manually update your database schema to match entity definitions

### Port Already in Use

If port 8081 is already in use, change it in `application.yaml`:

```yaml
server:
  port: 8082  # or any available port
```

## Next Steps

- Explore the Book collection CRUD operations
- Add your own entities and repositories
- Implement custom query methods
- Add table-based entities using `@EntityTable`

## Documentation

- [Astra DB Java SDK Documentation](https://docs.datastax.com/en/astra-db-serverless/api-reference/client-sdks.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data Documentation](https://spring.io/projects/spring-data)
