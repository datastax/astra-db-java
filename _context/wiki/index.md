# Wiki — astra-db-java

Concise orientation for any agent or contributor picking up this repository.
Read this first; follow links only when relevant to your task.

## Files in this wiki

| File | Purpose |
|------|---------|
| [project.md](project.md) | What the project is, its goals, modules, and planned roadmap |
| [preferences.md](preferences.md) | Coding standards, style rules, and how to work with the owner |

## One-line summary

`astra-db-java` is the **official Java client** for the DataStax Data API — an abstraction layer over Apache Cassandra that exposes both document-style collections and structured table access, including vector search. The repo is a multi-module Maven project (Java 17+, Apache 2.0).

## Quick orientation

- Main entry point: [`DataAPIClient`](../astra-db-java/src/main/java/com/datastax/astra/client/DataAPIClient.java)
- DevOps client: [`AstraOpsClient`](../astra-sdk-devops/src/main/java/com/dtsx/astra/sdk/AstraOpsClient.java)
- Framework integrations live under `integrations/` (Spring Boot 3.x, LangChain4j)
- CLI tools live under `tools/`
