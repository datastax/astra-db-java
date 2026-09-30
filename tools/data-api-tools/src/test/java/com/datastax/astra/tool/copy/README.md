# CollectionCloner Integration Tests

This directory contains integration tests for the `CollectionCloner` utility.

## Test Coverage

The `CollectionClonerIT` test suite covers:

1. **Basic Cloning** - Small collection (50 documents) with default settings
2. **Large Collection Cloning** - 2500+ documents with parallel reading (tests estimatedDocumentCount)
3. **Document Transformation** - Using DocumentMapper to transform documents during cloning
4. **Custom Thread Pools** - Testing different read/insert thread pool configurations
5. **Empty Collection** - Handling edge case of empty source collection
6. **Duplicate Prevention** - Verifying no duplicates are created on repeated cloning

## Running the Tests

### Prerequisites

- Local HCD/DSE instance running on `http://localhost:8181` (default)
- OR Astra database with proper credentials configured

### Run Tests Locally (HCD/DSE)

```bash
# From project root
mvn test -pl tools/data-api-tools

# Or from tools/data-api-tools directory
mvn test
```

### Run Tests Against Astra

```bash
# Set environment variables
export ASTRA_DB_APPLICATION_TOKEN=<your-token>
export ASTRA_DB_API_ENDPOINT=<your-endpoint>

# Run tests
mvn test -pl tools/data-api-tools -Dtest.environment=astra_prod
```

## Test Configuration

Tests use configuration from:
- `src/test/resources/test-config.properties` - Default local settings
- Environment variables can override config file settings
- Inherits from `AbstractDataAPITest` in astra-db-java module

## Performance Expectations

With default settings (5 read threads, 10 insert threads):
- Small collections (< 100 docs): < 5 seconds
- Medium collections (500 docs): < 15 seconds  
- Large collections (2500+ docs): < 60 seconds

Actual performance depends on:
- Network latency
- Database load
- Document size and complexity
- Available system resources

## Troubleshooting

### Test Failures

1. **Connection refused**: Ensure HCD/DSE is running on localhost:8181
2. **Timeout errors**: Increase timeout in test configuration
3. **Duplicate key errors**: Expected behavior when cloning to non-empty target

### Logging

Adjust log levels in `src/test/resources/logback-test.xml`:
```xml
<logger name="com.datastax.astra.tool.copy" level="DEBUG"/>
```

## Adding New Tests

When adding new test cases:
1. Extend `CollectionClonerIT` class
2. Use `@Order` annotation to control execution sequence
3. Clean up collections in `@BeforeAll` and `@AfterAll`
4. Use descriptive test method names: `should_<action>_<condition>()`
5. Add assertions to verify expected behavior
