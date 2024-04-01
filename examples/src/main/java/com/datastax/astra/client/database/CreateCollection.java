package com.datastax.astra.client.database;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;

public class CreateCollection {
  public static void main(String[] args) {
    Database db = new Database("API_ENDPOINT", "TOKEN");

    // Create a non-vector collection
    Collection<Document> simple1 = db.createCollection("col");

    Collection<Document> vector1 = db
            .createCollection("vector1", 14, SimilarityMetric.DOT_PRODUCT);

    // Create a vector collection
    Collection<Document> vector2 = db.createCollection("vector2", CollectionOptions
      .builder()
      .vectorDimension(1536)
      .vectorSimilarity(SimilarityMetric.EUCLIDEAN)
      .build());

    // Create a collection with indexing (deny)
    Collection<Document> indexing1 = db.createCollection("indexing1", CollectionOptions
              .builder()
              .indexingDeny("blob")
              .build());

    // Create a collection with indexing (allow) - cannot use allow and denay at the same time
    Collection<Document> allow1 = db.createCollection("allow1", CollectionOptions
            .builder()
            .indexingAllow("metadata")
            .build());

    // Enforce default id type could be objectid, uuid, uuivv6, uuidv7
    Collection<Document> defaultId = db.createCollection("defaultId", CollectionOptions
            .builder()
            .defaultIdType(CollectionIdTypes.OBJECT_ID)
            .build());
  }
}
