import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionDefinition;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.paging.FindIterable;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;

import static com.datastax.astra.client.DataAPIDestination.CASSANDRA;
import static com.datastax.astra.client.core.query.Sort.vector;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

public class QuickStartLocal {

    public static void main(String[] args) {

        // Create a Token
        String token = new UsernamePasswordTokenProvider("cassandra", "cassandra")
                .getTokenAsString();
        System.out.println("Token: " + token);

        // Initialize the client
        DataAPIClientOptions dataApiOptions = new DataAPIClientOptions().destination(CASSANDRA);
        DataAPIClient client = new DataAPIClient(token, dataApiOptions);
        System.out.println("Connected to Data API");

        // Initialize the database
        DatabaseOptions dbOptions = new DatabaseOptions().keyspace("default_keyspace");
        Database db = client.getDatabase("http://localhost:8181", dbOptions);
        System.out.println("Connected to Database");

        // Create a collection. The default similarity metric is cosine.
        CollectionDefinition collectionDefinition = new CollectionDefinition()
                .vectorDimension(5)
                .vectorSimilarity(COSINE);
        Collection<Document> collection = db.createCollection("vector_test", collectionDefinition);
        System.out.println("Collection created");

        collection.insertMany(
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

        Filter filter = null;
        CollectionFindOptions options = new CollectionFindOptions()
                .sort(vector(new float[]{0.15f, 0.1f, 0.1f, 0.35f, 0.55f}))
                .limit(10);
        FindIterable<Document> resultsSet = collection.find(filter,options);
        resultsSet.forEach(System.out::println);
        collection.drop();
        System.out.println("Deleted the collection");
    }
}
