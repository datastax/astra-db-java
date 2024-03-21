package com.datastax.astra.documentation.db;

import com.datastax.astra.db.AstraCollection;
import com.datastax.astra.AstraDatabase;
import io.stargate.sdk.data.client.exception.DataApiException;
import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.SimilarityMetric;
import io.stargate.sdk.data.client.model.collections.CreateCollectionOptions;

public class CreateCollection {
  public static void main(String[] args) {
    AstraDatabase db =
      new AstraDatabase("API_ENDPOINT", "TOKEN");

    // Create a non-vector collection
    AstraCollection<Document> col =
      db.createCollection("col");

    // Create a vector collection
    AstraCollection<Document> col_v = db.createCollection("col_v", CreateCollectionOptions
      .builder()
      .withVectorDimension(1536)
      .withVectorSimilarityMetric(SimilarityMetric.euclidean)
      .build());

    // Create a collection with indexing (deny)
    AstraCollection<Document> col_i = db.createCollection("col_i", CreateCollectionOptions
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
