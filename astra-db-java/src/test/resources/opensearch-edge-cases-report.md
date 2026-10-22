# OpenSearch Integration Edge Cases & Findings Report

This document records the exact findings, behaviors, patterns, and limitations discovered when testing advanced and edge-case data types with Data API collections and HCD $\rightarrow$ OpenSearch replication.

---

## 1. Multi-Level Nested Objects (2+ Levels Deep)

### Behavior
- When documents contain deeply nested objects (e.g. `company.department.name`), OpenSearch mapping must define nested `properties` blocks.
- HCD correctly extracts the top-level property (`company`) into `propertiesFromJsonFields` and passes the full nested schema to OpenSearch.

### Collection Mapping Pattern
```json
{
  "company": {
    "properties": {
      "name": { "type": "text" },
      "department": {
        "properties": {
          "name": { "type": "text" },
          "budget": { "type": "integer" }
        }
      }
    }
  }
}
```

### Querying
Dot-notation works in all `$openSearch` DSL clauses:
```json
{
  "find": {
    "filter": {
      "$openSearch": {
        "match": { "company.department.name": "Engineering" }
      }
    }
  }
}
```
```json
{
  "find": {
    "filter": {
      "$openSearch": {
        "range": { "company.department.budget": { "gte": 400000 } }
      }
    }
  }
}
```

---

## 2. OpenSearch `type: "nested"` (Array of Objects)

### Behavior
- In standard JSON / OpenSearch, an array of objects is flattened unless explicitly declared with `"type": "nested"`.
- When declared as `"type": "nested"`, each array element is indexed as a hidden separate document, preserving relationship boundaries across fields within the same item.

### Collection Mapping Pattern
```json
{
  "reviews": {
    "type": "nested",
    "properties": {
      "author": { "type": "keyword" },
      "score": { "type": "integer" }
    }
  }
}
```

### Document Example
```json
{
  "_id": "doc-1",
  "reviews": [
    { "author": "Alice", "score": 5 },
    { "author": "Bob", "score": 3 }
  ]
}
```

### Querying
OpenSearch `nested` query syntax works via `$openSearch`:
```json
{
  "find": {
    "filter": {
      "$openSearch": {
        "nested": {
          "path": "reviews",
          "query": {
            "bool": {
              "must": [
                { "term": { "reviews.author": "Alice" } },
                { "range": { "reviews.score": { "gte": 4 } } }
              ]
            }
          }
        }
      }
    }
  }
}
```
*(Querying `Alice` with `score <= 3` correctly returns 0 documents, confirming element-level isolation).*

---

## 3. EJSON Extension Types (`$date`, `$uuid`, `$binary`, `$objectId`)

### Root Mechanism
- Data API stores EJSON types inside `doc_json TEXT` as sub-objects (e.g. `{"publishedAt": {"$date": 1730419200000}}`).
- HCD replicates the document JSON verbatim into OpenSearch `_source`.
- Therefore, OpenSearch mappings must declare the nested property holding the `$` key.

### Mapping Patterns
| Type | Document Payload | OpenSearch Mapping | Query Path |
|------|------------------|--------------------|------------|
| **`$date`** | `{"publishedAt": {"$date": 1730419200000}}` | `"publishedAt": {"properties": {"$date": {"type": "date"}}}` | `"publishedAt.$date"` |
| **`$uuid`** | `{"userId": {"$uuid": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}}` | `"userId": {"properties": {"$uuid": {"type": "keyword"}}}` | `"userId.$uuid"` |
| **`byte[]` / binary** | `{"avatar": "aGVsbG8="}` (plain base64 string, Jackson default) | `"avatar": {"type": "binary"}` | not searchable (stored only, `_source`) |
| **`$objectId`**| `{"refId": {"$objectId": "507f1f77bcf86cd799439011"}}` | `"refId": {"properties": {"$objectId": {"type": "keyword"}}}` | `"refId.$objectId"` |

### Querying Examples
- Date range query:
  ```json
  {
    "find": {
      "filter": {
        "$openSearch": {
          "range": {
            "publishedAt.$date": { "gte": 1730000000000, "lte": 1731000000000 }
          }
        }
      }
    }
  }
  ```
- UUID term query:
  ```json
  {
    "find": {
      "filter": {
        "$openSearch": {
          "term": {
            "userId.$uuid": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
          }
        }
      }
    }
  }
  ```

---

## 4. Co-Existence with Data API Vector Search

### Behavior
- A collection can define both:
  1. Data API native vector search (`vector: {dimension: 3, metric: "cosine"}`)
  2. OpenSearch text / DSL search (`openSearch: {enabled: true, mappings: {...}}`)
- Documents store both `$vector` and other OpenSearch-indexed fields.
- Both search modes function independently on the same collection:
  - Vector search: `collection.find(null, new CollectionFindOptions().sort(vector))`
  - OpenSearch: `collection.find(openSearch(query))`

---

## 5. Java Client Gotchas (found while writing `HCD_Collections_03_OpenSearchEdgeCasesIT`)

- **`byte[]` in a Collection `Document` is sent as a plain base64 string** (Jackson default; `ByteArraySerializer` with `{"$binary": ...}` is Tables-only). Map it as `"avatar": {"type": "binary"}`, not as `{"properties": {"$binary": ...}}`. Declaring the `$binary` sub-property made the replication produce nothing, so the index count stayed at 0. Binary fields are stored in `_source` but cannot be queried.
- `Instant` -> `{"$date": epochMillis}` and `UUID` -> `{"$uuid": "..."}` serialize correctly for Collections.
- **Max 5 collections per database** on local HCD (`TOO_MANY_COLLECTIONS`). Leftover debug collections break later tests, so always drop them.
- Vector sort in Java: `new CollectionFindOptions().sort(Sort.vector(float[]))`.

## 6. Status

`HCD_Collections_03_OpenSearchEdgeCasesIT`: 7/7 tests pass (nested 2-level, `type: nested`, `$date`/`$uuid`/`$binary`, vector + OpenSearch co-existence).
