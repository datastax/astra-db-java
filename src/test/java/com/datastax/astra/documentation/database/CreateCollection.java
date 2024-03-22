package com.datastax.astra.documentation.database;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.exception.DataApiException;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.client.model.find.SimilarityMetric;

public class CreateCollection {
  public static void main(String[] args) {
    Database db = new Database("API_ENDPOINT", "TOKEN");

    // Create a non-vector collection
    Collection<Document> col = db.createCollection("col");

    // Create a vector collection
    Collection<Document> col_v = db.createCollection("col_v", CollectionOptions
      .builder()
      .withVectorDimension(1536)
      .withVectorSimilarityMetric(SimilarityMetric.euclidean)
      .build());

    // Create a collection with indexing (deny)
    Collection<Document> col_i = db.createCollection("col_i", CollectionOptions
              .builder()
              .withIndexingDeny("blob")
              .build());

    // Collection names should use snake case ([a-zA-Z][a-zA-Z0-9_]*)
    try {
      db.createCollection("invalid.name");
    } catch(DataApiException e) {
      // invalid.name is not valid
    }
  }
}
