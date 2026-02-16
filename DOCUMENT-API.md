 # Working with Documents

The `Document` class is the primary data container for collections. It stores data as a `Map<String, Object>` and provides two distinct access paths:

- **Plain access** (`put`/`get`) — keys are used as-is, no parsing
- **Escaping-aware access** (`append`/`read`) — dot-notation navigates nested maps

## Creating Documents

```java
// Empty document
Document doc = new Document();
Document doc = Document.create();

// With an _id
Document doc = new Document("myId");

// From an existing map
Document doc = new Document(Map.of("key", "value"));

// From JSON
Document doc = Document.parse("{\"name\": \"Alice\"}");
```

## Writing Data

### `put()` — Plain write (literal key, no parsing)

```java
Document doc = new Document();
doc.put("name", "Alice");

// Stored as literal key "a.b" — does NOT create nested structure
doc.put("a.b", "literal");
// doc = { "name": "Alice", "a.b": "literal" }
```

### `append()` — Escaping-aware write (dot-notation, creates nested maps)

```java
Document doc = new Document();

// "metadata.key1" => creates { "metadata": { "key1": "value1" } }
doc.append("metadata.key1", "value1");
doc.append("metadata.key2", "value2");
// doc = { "metadata": { "key1": "value1", "key2": "value2" } }

// Escaped dot: "config&.v2" => literal key "config.v2" at top level
doc.append("config&.v2", "enabled");
// doc = { "metadata": {...}, "config.v2": "enabled" }
```

## Reading Data

### `get()` — Plain read (literal key)

```java
doc.get("name");       // "Alice"
doc.get("meta");       // { "key": "val" }  (the nested map itself)
doc.get("meta.key");   // null  (literal key "meta.key" doesn't exist)
```

### `read()` — Escaping-aware read (dot-notation, array indexes)

```java
Document doc = new Document();
doc.append("metadata.key1", "value1");
doc.put("items", List.of("alpha", "beta", "gamma"));
doc.put("users", List.of(Map.of("name", "Alice"), Map.of("name", "Bob")));

doc.read("metadata.key1");     // "value1"
doc.read("items[0]");          // "alpha"
doc.read("users[0].name");     // "Alice"
doc.read("metadata.missing");  // null
```

## Typed Convenience Getters

All typed getters use `get()` (plain access) internally and operate on top-level keys:

```java
Document doc = new Document()
    .put("name", "Alice")
    .put("age", 30)
    .put("active", true)
    .put("tags", List.of("admin", "user"));

doc.getString("name");              // "Alice"
doc.getInteger("age");              // 30
doc.getBoolean("active");           // true
doc.getList("tags", String.class);  // ["admin", "user"]
```

Available getters: `getString`, `getInteger`, `getLong`, `getDouble`, `getFloat`, `getBoolean`, `getShort`, `getByte`, `getCharacter`, `getDate`, `getCalendar`, `getInstant`, `getObjectId`, `getUUID`, `getList`, `getMap`, `getArray`.

## Escape Rules

The escape character is `&`, used to distinguish structural dots (path separators) from literal dots in field names:

| Expression | Meaning |
|------------|---------|
| `.` | Path separator — navigate into a nested map |
| `&.` | Escaped dot — literal `.` in the field name |
| `&&` | Escaped ampersand — literal `&` in the field name |

```java
// Escaped dot: accesses literal key "config.v2"
doc.read("config&.v2");

// Escaped ampersand: navigates to { "parent": { "child&name": ... } }
doc.append("parent.child&&name", "val");
```

## Identity, Vector, and Similarity

```java
// Identity
doc.id("doc-001");
doc.getId(String.class);

// Vector
doc.vector(new float[]{0.1f, 0.2f});
doc.getVector();                       // Optional<DataAPIVector>

// Vectorize (server-side embedding)
doc.vectorize("text to embed");
doc.getVectorize();                    // Optional<String>

// Similarity (populated by vector search)
doc.getSimilarity();                   // Optional<Double>
```

## Checking Existence and Removing Data

```java
// containsKey uses escaping-aware navigation
doc.containsKey("a.b.c");  // true if nested path exists

// remove uses escaping-aware navigation
doc.remove("a.b");          // removes key "b" from nested map "a"

// Clear all entries
doc.clear();
```

## Quick Reference

| Operation | Plain (literal key) | Escaping-aware (dot-notation) |
|-----------|--------------------|-----------------------------|
| **Write** | `put(key, value)` | `append(key, value)` |
| **Read** | `get(key)` | `read(key)` |
| **Read typed** | `get(key, clazz)` | `read(key, clazz)` |
| **Contains** | — | `containsKey(key)` |
| **Remove** | `documentMap.remove(key)` | `remove(key)` |
| **Bulk write** | `putAll(map)` | — |
