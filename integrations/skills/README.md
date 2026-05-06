# Astra DB Java SDK - AI Skills

This directory contains AI-powered skills for building applications with DataStax Astra DB. Each skill is a comprehensive, step-by-step guide designed to be used with AI coding assistants like Claude, GitHub Copilot, or Bob Shell.

## What are AI Skills?

AI Skills are structured learning modules that combine:
- **Detailed instructions** - Step-by-step guidance with code examples
- **Best practices** - Production-ready patterns and recommendations
- **Complete examples** - Working code you can copy and adapt
- **Troubleshooting** - Common issues and solutions

Skills are designed to be consumed by AI assistants to help developers build applications faster and with fewer errors.

## Available Skills

### 1. Spring Boot with Data API
**Path:** `spring-boot-data-api/SKILL.md`  
**Level:** Intermediate  
**Time:** 30-45 minutes

Learn to build production-ready Spring Boot applications with Astra DB using Spring Data repositories, object mapping, and vector search.

**Topics Covered:**
- Spring Boot project setup
- Object mapping with annotations
- Spring Data repositories
- REST API development
- Vector search integration
- Testing and production deployment

**Prerequisites:**
- Java 17+
- Basic Spring Boot knowledge
- Astra DB account

---

## Planned Skills (Coming Soon)

### 2. Vector Search & RAG Applications
**Status:** Planned  
**Level:** Advanced

Build Retrieval-Augmented Generation (RAG) applications using Astra DB's vector search capabilities with LangChain4j integration.

**Topics:**
- Vector embeddings and similarity search
- RAG architecture patterns
- LangChain4j integration
- Prompt engineering
- Production RAG deployment

---
-- list of document ingested
CREATE TABLE workbench_documents_by_vector_store (
    workspace           UUID,
    vector_store        UUID,
    source_doc_id      text,
    source_filename    text,
    file_type          text,
    file_size          bigint,
    -- ingestion
    md5_hash           text,
    chunk_total        int,
    ingested_at        timestamp,
    status             text,
    metadata           map<text, text>,
    PRIMARY KEY ((workspace, vector_store), source_doc_id)
);

## How to Use Skills with AI Assistants

### Using with Claude (Anthropic)

1. **Open Claude** in your preferred interface (Claude.ai, API, or IDE extension)

2. **Reference the skill** in your conversation:
   ```
   I want to build a Spring Boot application with Astra DB.
   Please use the skill at: @/path/to/integrations/skills/spring-boot-data-api/SKILL.md
   ```

3. **Follow along** as Claude guides you through the skill step-by-step

4. **Ask questions** at any point:
   ```
   Can you explain the @Vectorize annotation in more detail?
   How do I implement pagination for my specific use case?
   ```

### Using with Bob Shell

Bob Shell is an AI-powered terminal assistant that can execute commands and write code.

1. **Start Bob Shell** in your project directory:
   ```bash
   bob
   ```

2. **Load the skill**:
   ```
   Use the Spring Boot Data API skill to create a new REST API for managing products
   @/path/to/integrations/skills/spring-boot-data-api/SKILL.md
   ```

3. **Bob will**:
   - Read the skill document
   - Ask clarifying questions
   - Generate code files
   - Execute setup commands
   - Run tests

4. **Iterate and refine**:
   ```
   Add vector search to the product service
   Implement pagination with 20 items per page
   Add validation for the price field
   ```

### Using with GitHub Copilot

1. **Open the skill file** in VS Code:
   ```bash
   code integrations/skills/spring-boot-data-api/SKILL.md
   ```

2. **Use Copilot Chat** with context:
   ```
   @workspace /explain How do I implement the repository pattern shown in this skill?
   ```

3. **Generate code** based on skill examples:
   - Open a new file
   - Start typing based on skill patterns
   - Copilot will suggest completions based on the skill context

### Using with Cursor

1. **Open your project** in Cursor

2. **Use Composer** (Cmd/Ctrl + K):
   ```
   Create a Spring Boot application following the pattern in 
   integrations/skills/spring-boot-data-api/SKILL.md
   ```

3. **Cursor will**:
   - Analyze the skill
   - Generate multiple files
   - Apply changes across your project

### Using with Cline (formerly Claude Dev)

1. **Open VS Code** with Cline extension

2. **Start a task**:
   ```
   Build a product management API using the Spring Boot Data API skill
   Skill location: integrations/skills/spring-boot-data-api/SKILL.md
   ```

3. **Cline will**:
   - Read the skill
   - Create files
   - Run commands
   - Ask for approval at each step

---

## Skill Structure

Each skill follows this structure:

```
skill-name/
├── SKILL.md              # Main skill document with YAML front matter
├── examples/             # Complete working examples (optional)
│   ├── basic/
│   └── advanced/
├── templates/            # Code templates (optional)
│   ├── controller.java
│   ├── service.java
│   └── repository.java
└── assets/               # Diagrams, images (optional)
    └── architecture.png
```

### YAML Front Matter

Each skill includes metadata:

```yaml
---
name: "Skill Name"
description: "Brief description"
author: "DataStax"
version: "1.0.0"
tags: [tag1, tag2, tag3]
difficulty: beginner|intermediate|advanced
prerequisites:
  - "Prerequisite 1"
  - "Prerequisite 2"
estimated_time: "30-45 minutes"
learning_objectives:
  - "Objective 1"
  - "Objective 2"
---
```

---

## Best Practices for Using Skills

### 1. Start with Prerequisites
Ensure you have all prerequisites installed and configured before starting a skill.

### 2. Follow Step-by-Step
Skills are designed to be followed sequentially. Don't skip steps unless you're experienced.

### 3. Adapt to Your Needs
Skills provide patterns and examples. Adapt them to your specific requirements.

### 4. Test as You Go
Run tests after each major section to ensure everything works.

### 5. Ask Questions
Don't hesitate to ask your AI assistant for clarification or alternative approaches.

### 6. Combine Skills
Advanced users can combine multiple skills for complex applications.

---

## Contributing New Skills

Want to contribute a new skill? Follow these guidelines:

### 1. Skill Proposal
Create an issue describing:
- Skill topic and scope
- Target audience and difficulty level
- Prerequisites
- Learning objectives

### 2. Skill Structure
Follow the standard structure:
- YAML front matter with metadata
- Clear sections with step-by-step instructions
- Complete, working code examples
- Best practices and troubleshooting

### 3. Code Quality
- All code must be tested
- Follow Java and Spring Boot conventions
- Include error handling
- Add comments for complex logic

### 4. Documentation
- Clear, concise writing
- Explain "why" not just "how"
- Include diagrams where helpful
- Provide troubleshooting tips

### 5. Review Process
Submit a pull request with:
- The skill document
- Example code (if applicable)
- Tests demonstrating the skill works

---

## Skill Development Roadmap

### Q2 2024
- ✅ Spring Boot with Data API
- 🔄 Vector Search & RAG Applications
- 📋 Microservices with Astra DB

### Q3 2024
- 📋 Real-time Data Streaming
- 📋 GraphQL API with Astra DB
- 📋 Multi-Model Data Access

### Q4 2024
- 📋 Serverless Functions with Astra DB
- 📋 Testing Strategies for Astra DB

Legend:
- ✅ Complete
- 🔄 In Progress
- 📋 Planned

---

## Support and Feedback

### Questions?
- 💬 [DataStax Community Forum](https://community.datastax.com/)
- 📧 [GitHub Issues](https://github.com/datastax/astra-db-java/issues)
- 📖 [Documentation](https://docs.datastax.com/)

### Feedback
We'd love to hear how you're using these skills! Share your experience:
- What worked well?
- What could be improved?
- What skills would you like to see next?

Create an issue or discussion on GitHub with the `skill-feedback` label.

---

## License

All skills are provided under the Apache License 2.0, same as the Astra DB Java SDK.

---

## Additional Resources

- [Astra DB Documentation](https://docs.datastax.com/en/astra-db-serverless/)
- [Java SDK Reference](https://docs.datastax.com/en/astra-db-serverless/api-reference/client-sdks.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Sample Applications](https://github.com/datastax/astra-db-java/tree/main/samples)
- [Video Tutorials](https://www.youtube.com/@DataStaxDevs)

---

**Happy Learning!** 🚀

Build amazing applications with DataStax Astra DB and AI-powered skills.
