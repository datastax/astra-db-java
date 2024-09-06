package com.datastax.astra.client.database;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionIdTypes;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;

public class CreateCollection {
  public static void main(String[] args) {

    Database db = new Database(
            System.getenv("ASTRA_DB_API_ENDPOINT"),
            System.getenv("ASTRA_DB_APPLICATION_TOKEN"));

    // Create a non-vector collection
    Collection<Document> simple1 = db.createCollection("col");

    // Default Id Collection
    Collection<Document> defaultId = db.createCollection("defaultId", CollectionOptions
            .builder()
            .defaultIdType(CollectionIdTypes.OBJECT_ID)
            .build());

    // -- Indexing
    Collection<Document> indexingDeny = db.createCollection("indexing1", CollectionOptions
              .builder()
              .indexingDeny("blob")
              .build());
    // Create a collection with indexing (allow) - cannot use allow and denay at the same time
    Collection<Document> indexingAllow = db.createCollection("allow1", CollectionOptions
            .builder()
            .indexingAllow("metadata")
            .build());

    // Vector
    Collection<Document> vector1 = db.createCollection("vector1", 14, SimilarityMetric.DOT_PRODUCT);

    // Create a vector collection
    Collection<Document> vector2 = db.createCollection("vector2", CollectionOptions
      .builder()
      .vectorDimension(1536)
      .vectorSimilarity(SimilarityMetric.EUCLIDEAN)
      .build());

    // Create a collection for the db
    Collection<Document> collection_vectorize_header = db.createCollection(
            "collection_vectorize_header",
            // Create collection with a Service in vectorize (No API KEY)
            CollectionOptions.builder()
                    .vectorDimension(1536)
                    .vectorSimilarity(SimilarityMetric.DOT_PRODUCT)
                    .vectorize("openai", "text-embedding-ada-002")
                    .build());

    // Create a collection for the db
    Collection<Document> collection_vectorize_shared_key = db.createCollection(
            "collection_vectorize_shared_key",
            // Create collection with a Service in vectorize (No API KEY)
            CollectionOptions.builder()
                    .vectorDimension(1536)
                    .vectorSimilarity(SimilarityMetric.DOT_PRODUCT)
                    .vectorize("openai", "text-embedding-ada-002", "OPENAI_API_KEY" )
                    .build());



  }
}
