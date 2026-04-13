# Tutorial: HCD Quickstart with Vectorize

> **Companion code:** [`SampleQuickstartHCD.java`](../src/main/java/com/datastax/astra/samples/quickstart/SampleQuickstartHCD.java)

This tutorial walks through connecting to a local Hyper-Converged Database (HCD)
instance, creating a keyspace, setting up a vectorize collection backed by
OpenAI embeddings, inserting documents, and running a similarity search.

## Prerequisites

- Java 17+
- A running HCD instance on `localhost:8181` (default Data API endpoint)
- An OpenAI API key for the embedding provider
- The `astra-db-java` SDK on your classpath

## Step 1: Build a Token and Initialize the Client

HCD uses Cassandra-style credentials. The SDK provides
`UsernamePasswordTokenProvider` to encode them into the expected token format.

```java
String token = new UsernamePasswordTokenProvider("cassandra", "cassandra")
        .getTokenAsString();

DataAPIClient client = new DataAPIClient(token,
        new DataAPIClientOptions().destination(DataAPIDestination.HCD));
```

Key points:
- `DataAPIDestination.HCD` tells the client to target a local HCD deployment.
- The default endpoint is `http://localhost:8181` (`DEFAULT_ENDPOINT_LOCAL`).

## Step 2: Create a Keyspace

Cast the admin to `DataAPIDatabaseAdmin` (HCD-specific) and create a keyspace
with `SimpleStrategy` replication.

```java
((DataAPIDatabaseAdmin) client
        .getDatabase(dataApiUrl)
        .getDatabaseAdmin())
        .createKeyspace("ks1", KeyspaceOptions.simpleStrategy(1));
```

## Step 3: Connect to the Database

Obtain a `Database` handle scoped to the keyspace you just created.

```java
Database db = client.getDatabase(dataApiUrl,
        new DatabaseOptions().keyspace("ks1"));
```

## Step 4: Create a Vectorize Collection

Define a collection that delegates embedding generation to OpenAI.

```java
CollectionDefinition cd = new CollectionDefinition()
        .vectorSimilarity(SimilarityMetric.COSINE)
        .vectorDimension(1536)
        .vectorize("openai", "text-embedding-3-small");

CreateCollectionOptions opts = new CreateCollectionOptions()
        .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("OPENAI_API_KEY"));

Collection<Document> lyrics = db.createCollection("lyrics", cd,
        Document.class, opts);
```

- `vectorize(provider, model)` enables server-side embedding generation.
- `EmbeddingAPIKeyHeaderProvider` forwards your API key to the embedding service.

## Step 5: Insert Documents

Use `insertMany` with the `.vectorize()` helper on each document. The server
generates embeddings automatically from the text you provide.

```java
lyrics.insertMany(
        new Document(1).append("band", "Dire Straits")
                .append("song", "Romeo And Juliet")
                .vectorize("A lovestruck Romeo sings the streets a serenade"),
        new Document(2).append("band", "Dire Straits")
                .append("song", "Romeo And Juliet")
                .vectorize("Says something like, You and me babe, how about it?"),
        // ... more documents
);
```

## Step 6: Run a Similarity Search

Find the most similar document matching a filter, using vectorize-based sorting.

```java
Optional<Document> doc = lyrics.findOne(
        eq("band", "Dire Straits"),
        new CollectionFindOneOptions()
                .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha"))
                .includeSimilarity(true));
```

- `Sort.vectorize(text)` embeds the query text server-side and sorts by similarity.
- `includeSimilarity(true)` adds a `$similarity` score to the result.

## Summary

In this tutorial you:

1. Connected to a local HCD instance using `UsernamePasswordTokenProvider`
2. Created a keyspace with `SimpleStrategy` replication
3. Defined a vectorize collection backed by OpenAI embeddings
4. Inserted documents with server-side embedding generation
5. Performed a filtered similarity search using `findOne` with `Sort.vectorize`

**Next steps:**
- Explore [`SampleCollectionVectorize`](../src/main/java/com/datastax/astra/samples/collections/SampleCollectionVectorize.java) for more vectorize patterns
- Try [`SampleHybridCollectionDefinition`](../src/main/java/com/datastax/astra/samples/collections/SampleHybridCollectionDefinition.java) for hybrid (vector + lexical) search
