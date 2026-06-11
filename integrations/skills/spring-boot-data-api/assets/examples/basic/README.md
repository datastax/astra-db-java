# Basic Spring Boot with Astra DB Example

This is a minimal working example demonstrating Spring Boot integration with Astra DB.

## What's Included

- Simple Product entity with basic CRUD operations
- Spring Data repository
- REST API endpoints
- Configuration examples

## Quick Start

1. **Set environment variables:**
   ```bash
   export ASTRA_DB_TOKEN="AstraCS:..."
   export ASTRA_DB_ENDPOINT="https://your-db-id-region.apps.astra.datastax.com"
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Test the API:**
   ```bash
   # Create a product
   curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{"name":"Laptop","category":"Electronics","price":999.99,"stock":10}'
   
   # List all products
   curl http://localhost:8080/api/products
   
   # Get by ID
   curl http://localhost:8080/api/products/{id}
   ```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── DemoApplication.java
│   │       ├── model/
│   │       │   └── Product.java
│   │       ├── repository/
│   │       │   └── ProductRepository.java
│   │       ├── service/
│   │       │   └── ProductService.java
│   │       └── controller/
│   │           └── ProductController.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/example/demo/
            └── ProductServiceTest.java
```

## Next Steps

- Add vector search capabilities
- Implement pagination
- Add validation
- Create integration tests
- Deploy to production

See the main [SKILL.md](../../SKILL.md) for detailed guidance.
