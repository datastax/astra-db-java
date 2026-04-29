# Spring Boot with Astra DB Data API - AI Skill

A comprehensive AI-powered skill for building production-ready Spring Boot applications with DataStax Astra DB.

## Quick Links

- 📚 **[Main Skill Document](SKILL.md)** - Complete step-by-step guide
- 📝 **[Code Templates](templates/)** - Reusable code templates
- 💡 **[Basic Example](examples/basic/)** - Minimal working example

## What You'll Learn

- Set up Spring Boot with Astra DB Data API
- Use Spring Data repositories for CRUD operations
- Implement object mapping with annotations
- Build REST APIs with Spring MVC
- Enable vector search for semantic queries
- Configure and optimize database connections
- Test and deploy to production

## Prerequisites

- Java 17 or higher
- Maven or Gradle
- Basic Spring Boot knowledge
- Astra DB account ([sign up free](https://astra.datastax.com))

## Estimated Time

30-45 minutes

## How to Use This Skill

### With Claude

```
I want to build a Spring Boot application with Astra DB.
Use the skill at: @integrations/skills/spring-boot-data-api/SKILL.md
```

### With Bob Shell

```bash
bob
# Then in Bob:
Use the Spring Boot Data API skill to create a REST API
@integrations/skills/spring-boot-data-api/SKILL.md
```

### With GitHub Copilot

Open the SKILL.md file in VS Code and use Copilot Chat:
```
@workspace /explain How do I implement the repository pattern from this skill?
```

### With Cursor

Use Composer (Cmd/Ctrl + K):
```
Create a Spring Boot application following the pattern in 
integrations/skills/spring-boot-data-api/SKILL.md
```

## Quick Start

1. **Read the skill document:**
   ```bash
   cat SKILL.md
   ```

2. **Use templates to generate code:**
   - Copy templates from `templates/` directory
   - Replace placeholders ({{CLASS_NAME}}, {{COLLECTION_NAME}}, etc.)
   - Customize for your use case

3. **Follow the skill step-by-step:**
   - Part 1: Project Setup
   - Part 2: Domain Model & Object Mapping
   - Part 3: Data Access Layer
   - Part 4: REST API Layer
   - Part 5: Advanced Features
   - Part 6: Testing
   - Part 7: Production Considerations

## Available Templates

| Template | Description |
|----------|-------------|
| `Document.java.template` | Entity class with annotations |
| `Repository.java.template` | Spring Data repository interface |
| `Service.java.template` | Service layer with business logic |
| `Controller.java.template` | REST controller with endpoints |
| `application.yml.template` | Application configuration |

## Key Features Covered

### Object Mapping
- `@DataApiCollection` - Collection configuration
- `@DocumentId` - Document identifier
- `@Vectorize` - Automatic vectorization
- `@Lexical` - Full-text search
- `@Vector` - Pre-computed embeddings

### Spring Data Integration
- CRUD operations
- Query by Example
- Data API filters
- Pagination and sorting

### Advanced Features
- Vector search for semantic queries
- Full-text search with lexical indexing
- Custom ID types
- Indexing optimization

### Production Ready
- Error handling
- Validation
- Testing strategies
- Configuration profiles
- Monitoring with Actuator

## Example Use Cases

This skill helps you build:

1. **Product Catalog API** - E-commerce product management
2. **Document Search** - Full-text and semantic search
3. **Content Management** - Blog posts, articles with vector search
4. **User Management** - Authentication and profile management
5. **IoT Data Storage** - Time-series data with flexible schema

## Related Skills

- **Vector Search & RAG Applications** (Coming Soon)
- **Microservices with Astra DB** (Coming Soon)
- **GraphQL API with Astra DB** (Coming Soon)

## Support

- 📖 [Astra DB Documentation](https://docs.datastax.com/en/astra-db-serverless/)
- 💬 [Community Forum](https://community.datastax.com/)
- 🐛 [GitHub Issues](https://github.com/datastax/astra-db-java/issues)

## Contributing

Found an issue or have a suggestion? Please open an issue or pull request!

## License

Apache License 2.0
