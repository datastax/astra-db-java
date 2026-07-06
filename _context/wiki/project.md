# Project Overview — astra-db-java

## What this project is

`astra-db-java` is the **Java client library** for the DataStax **Data API** — an HTTP abstraction layer on top of Apache Cassandra. The Data API exposes two storage paradigms:

- **Collections** — document-oriented access backed by a dedicated shredding structure, making Cassandra behave like a document database.
- **Tables** — structured row-based access (classic CQL-style semantics via HTTP).

Both paradigms support **vector search**, including server-side embedding generation (Vectorize).

The Data API is available for **Astra DB** (DataStax cloud) and **HCD** (Hyper-Converged Database, on-prem). Java is one of five official client implementations (alongside Python, TypeScript, Go, and others).

This repo also contains:
- **`astra-sdk-devops`** — a companion client for the Astra DevOps/Management API (provisioning, org management, streaming).
- **`integrations/`** — ready-made connectors for Spring Boot 3.x and LangChain4j.
- **`tools/`** — CLI utilities (CSV import, data migration helpers).
- **`samples/`** — example applications.

## Main goals and objectives

1. **Keep pace with the Data API** — implement new commands and capabilities as they are released by the server team.
2. **Keep pace with the DevOps API** — track changes in `astra-sdk-devops`.
3. **Planned features in flight:**
   - Cloning databases (DevOps API)
   - New commands for advanced vector search
4. **Maintain feature parity** with the other four client implementations.
5. **Deliver a great Java developer experience** — fluent builders, POJO mapping, full sync + async support, zero-config setup.

## Key stakeholders / users

- **Java developers** building applications on Astra DB or HCD.
- **DataStax internal teams** — the client is the reference Java implementation.
- **LangChain4j / Spring Boot ecosystem users** via the integration modules.

## Module map

| Module | Artifact | Description |
|--------|----------|-------------|
| `astra-db-java` | `com.datastax.astra:astra-db-java` | Core Data API client — collections, tables, vector search |
| `astra-sdk-devops` | `com.datastax.astra:astra-sdk-devops` | DevOps / Management API client |
| `integrations/langchain4j-astradb` | — | LangChain4j integration |
| `integrations/data-api-spring-boot-3x-*` | — | Spring Boot 3.x auto-configure + starter |
| `tools/data-api-tools` | — | General CLI utilities |
| `tools/data-api-tools-csv` | — | CSV import tooling |

## Key entry points

- [`DataAPIClient`](../../astra-db-java/src/main/java/com/datastax/astra/client/DataAPIClient.java) — top-level client, start here.
- [`Database`](../../astra-db-java/src/main/java/com/datastax/astra/client/databases/Database.java) — database-level operations.
- [`Collection`](../../astra-db-java/src/main/java/com/datastax/astra/client/collections/Collection.java) — document collection access.
- [`AstraOpsClient`](../../astra-sdk-devops/src/main/java/com/dtsx/astra/sdk/AstraOpsClient.java) — DevOps API entry point.

## Tech stack

- Java 17+, Maven multi-module
- Jackson (JSON), Lombok, Retry4j
- JUnit 5, AssertJ, MockWebServer
- Apache 2.0 license
- Version: `2.3.1-SNAPSHOT`
