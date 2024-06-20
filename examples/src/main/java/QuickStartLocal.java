import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.internal.auth.UsernamePasswordTokenProvider;

import static com.datastax.astra.client.DataAPIOptions.DataAPIDestination.CASSANDRA;
import static com.datastax.astra.client.DataAPIOptions.builder;
import static com.datastax.astra.client.model.SimilarityMetric.COSINE;

public class QuickStartLocal {

    public static void main(String[] args) {

        // Create a Token
        String token = new UsernamePasswordTokenProvider("cassandra", "cassandra").getTokenAsString();
        System.out.println("Token: " + token);

        // Initialize the client
        DataAPIClient client = new DataAPIClient(token, builder().withDestination(CASSANDRA).build());
        System.out.println("Connected to Data API");

        Database db = client.getDatabase("http://localhost:8181", "default_keyspace");
        System.out.println("Connected to Database");

        // Create a collection. The default similarity metric is cosine.
        Collection<Document> collection = db.createCollection("vector_test", 5, COSINE);
        System.out.println("Created a Collection");

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

        FindIterable<Document> resultsSet = collection.find(
                new float[]{0.15f, 0.1f, 0.1f, 0.35f, 0.55f},
                10
        );
        resultsSet.forEach(System.out::println);
        collection.drop();
        System.out.println("Deleted the collection");
    }
}
