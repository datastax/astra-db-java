package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;

public class CreateCollection {
  public static void main(String[] args) {
    /* Using Astra
      String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
      String astraApiEndpoint = System.getenv("ASTRA_DB_API_ENDPOINT");
      Database db = new DataAPIClient(astraToken).getDatabase(System.getenv(astraApiEndpoint));
     */
    // Using Local DB
    Database db = DataAPIClients.localDbWithDefaultKeyspace();

    // Create a non-vector collection
    Collection<Document> simple1 = db.createCollection("col");

    // Default Id Collection
    Collection<Document> defaultId = db.createCollection("defaultId", new CollectionDefinition()
            .defaultId(CollectionDefaultIdTypes.OBJECT_ID));

    // -- Indexing
    Collection<Document> indexingDeny = db.createCollection("indexing1",
            new CollectionDefinition().indexingDeny("blob"));
    // Create a collection with indexing (allow) - cannot use allow and denay at the same time
    Collection<Document> indexingAllow = db.createCollection("allow1",
            new CollectionDefinition().indexingAllow("metadata"));

    // Vector
    CollectionDefinition cd = new CollectionDefinition()
            .vectorDimension(5)
            .vectorSimilarity(SimilarityMetric.COSINE);
    Collection<Document> vector1 = db.createCollection("vector1", cd);

    // Create a vector collection
    Collection<Document> vector2 = db.createCollection("vector2", new CollectionDefinition()
      .vectorDimension(1536)
      .vectorSimilarity(SimilarityMetric.EUCLIDEAN));

    // Create a collection for the db
    Collection<Document> collection_vectorize_header = db.createCollection(
            "collection_vectorize_header",
            // Create collection with a Service in vectorize (No API KEY)
            new CollectionDefinition()
                    .vector(1536, SimilarityMetric.DOT_PRODUCT)
                    .vectorize("openai", "text-embedding-ada-002"));

    // Create a collection for the db
    Collection<Document> collection_vectorize_shared_key = db.createCollection(
            "collection_vectorize_shared_key",
            // Create collection with a Service in vectorize (No API KEY)
            new CollectionDefinition()
                    .vectorDimension(1536)
                    .vectorSimilarity(SimilarityMetric.DOT_PRODUCT)
                    .vectorize("openai", "text-embedding-ada-002", "OPENAI_API_KEY" ));
  }
}
