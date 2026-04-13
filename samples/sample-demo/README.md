# Data API Client Samples

Runnable examples and step-by-step tutorials for the **astra-db-java** SDK.

## Prerequisites

- **Java 17+**
- **Maven** (to build and run the samples)
- A running database — either:
  - [DataStax Astra](https://astra.datastax.com/) cloud instance with an application token, or
  - A local [HCD](https://docs.datastax.com/) instance (default endpoint `http://localhost:8181`)
- For vectorize samples: an embedding-provider API key (e.g. OpenAI)

## Tutorials

Step-by-step guides that walk through a sample from start to finish.

| Tutorial | Description |
|----------|-------------|
| [HCD Quickstart with Vectorize](tutorials/quickstart-hcd.md) | Connect to local HCD, create a keyspace, set up vectorize with OpenAI, and run a similarity search |

## Samples

Each sample is a standalone `main()` class you can run after setting your credentials.

| Category | Sample | Description |
|----------|--------|-------------|
| **Quickstart** | [`SampleQuickstartHCD`](src/main/java/com/datastax/astra/samples/quickstart/SampleQuickstartHCD.java) | End-to-end HCD quickstart: connect, create keyspace, vectorize query |
| **Client** | [`SampleClientConfiguration`](src/main/java/com/datastax/astra/samples/client/SampleClientConfiguration.java) | Full configuration cookbook: HTTP settings, timeouts, proxies, observers |
| **Collections** | [`SampleCollectionInsertMany`](src/main/java/com/datastax/astra/samples/collections/SampleCollectionInsertMany.java) | Bulk insert with chunk size, concurrency, and ordering options |
| | [`SampleCollectionVectorize`](src/main/java/com/datastax/astra/samples/collections/SampleCollectionVectorize.java) | Server-side embeddings (vectorize) for insert and search |
| | [`SampleCollectionDates`](src/main/java/com/datastax/astra/samples/collections/SampleCollectionDates.java) | Working with `Calendar`, `Date`, and `Instant` fields |
| | [`SampleDocumentIds`](src/main/java/com/datastax/astra/samples/collections/SampleDocumentIds.java) | All supported `_id` types: UUID, UUIDv6/v7, ObjectId, etc. |
| | [`SampleHybridCollectionDefinition`](src/main/java/com/datastax/astra/samples/collections/SampleHybridCollectionDefinition.java) | Hybrid collection with vector, lexical, and reranking |
| | [`SampleFindAndRerank`](src/main/java/com/datastax/astra/samples/collections/SampleFindAndRerank.java) | Hybrid search with `findAndRerank` API |
| **Tables** | [`SampleTableRows`](src/main/java/com/datastax/astra/samples/tables/SampleTableRows.java) | Typed row builders and POJO mapping with `@Column` |
| | [`SampleTableVectorize`](src/main/java/com/datastax/astra/samples/tables/SampleTableVectorize.java) | Vectorize on table columns with similarity search |
| | [`SampleTableUdtObjectMapping`](src/main/java/com/datastax/astra/samples/tables/SampleTableUdtObjectMapping.java) | User-Defined Types (UDT) with nested POJO mapping |

## Tutorial Conventions

When adding a new tutorial, follow this pattern so all tutorials stay consistent:

1. **Create a markdown file** in `tutorials/` named `<topic>.md` (e.g. `vectorize-collections.md`, `hybrid-search.md`).

2. **Use this structure:**

   ```markdown
   # Tutorial: <Title>

   > **Companion code:** [`SampleXxx.java`](../src/main/java/.../SampleXxx.java)

   Brief introduction.

   ## Prerequisites
   - (what's needed for this specific tutorial)

   ## Step 1: <Action>
   Explanation...
   ```java
   // code snippet from the companion sample
   ​```

   ## Step 2: <Action>
   ...

   ## Summary
   What was covered, next steps.
   ```

3. **Register the tutorial** by adding a row to the Tutorials table above.

4. **Link to the companion sample** at the top of the tutorial so readers can jump straight to the full code.
