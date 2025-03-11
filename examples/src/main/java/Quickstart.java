import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindCursor;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;

import static com.datastax.astra.client.core.query.Sort.vector;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

public class Quickstart {

  public static void main(String[] args) {
    // Loading Arguments
    String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    //String astraApiEndpoint = System.getenv("ASTRA_DB_API_ENDPOINT");
    String astraApiEndpoint = "https://e6d17fde-3555-42af-941f-16ce090d49f8-us-east1.apps.astra.datastax.com";

    // Initialize the client.
    DataAPIClient client = new DataAPIClient(astraToken);
    System.out.println("Connected to AstraDB");

    // Initialize the database.
    Database db = client.getDatabase(astraApiEndpoint);
    System.out.println("Connected to Database.");
// end::init[]

// tag::collection[]
    // Create a collection. The default similarity metric is cosine.
    CollectionDefinition cd = new CollectionDefinition()
            .vectorDimension(5)
            .vectorSimilarity(COSINE);
    Collection<Document> col = db.createCollection("vector_test", cd);
    System.out.println("Created a collection");
// end::collection[]

// tag::data[]
    // Insert documents into the collection
    col.insertMany(
            new Document("1")
                    .append("text", "ChatGPT integrated sneakers that talk to you")
                    .vector(new float[]{0.1f, 0.15f, 0.3f, 0.12f, 0.05f}),
            new Document("2")
                    .append("text", "An AI quilt to help you sleep forever")
                    .vector(new float[]{0.45f, 0.09f, 0.01f, 0.2f, 0.11f}),
            new Document("3")
                    .append("text", "A deep learning display that controls your mood")
                    .vector(new float[]{0.1f, 0.05f, 0.08f, 0.3f, 0.6f}));
    System.out.println("Inserted documents into the collection");
// end::data[]

// tag::search[]
    // Perform a similarity search
    Filter filter = null;
    CollectionFindOptions options = new CollectionFindOptions()
            .sort(vector(new float[]{0.15f, 0.1f, 0.1f, 0.35f, 0.55f}))
            .limit(10);
    CollectionFindCursor<Document, Document> resultsSet = col.find(filter,options);
    resultsSet.forEach(System.out::println);
// end::search[]

// tag::cleanup[]
    // Delete the collection
    col.drop();
    System.out.println("Deleted the collection");
// end::cleanup[]

// tag::end[]
  }
}
// end::end[]