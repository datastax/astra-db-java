import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.paging.FindIterable;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.databases.Database;

import static com.datastax.astra.client.core.query.Sort.vector;
import static com.datastax.astra.client.core.vector.SimilarityMetric.COSINE;

public class QuickStartTraining {

    public static void main(String[] args) {
        String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        String astraApiEndpoint = System.getenv("ASTRA_DB_API_ENDPOINT");

        DataAPIClient client = new DataAPIClient(astraToken, new DataAPIClientOptions()
                .embeddingAPIKey("sfdsfdsfd"));
        System.out.println("Connected to AstraDB");

        Database db = client.getDatabase(astraApiEndpoint);
        System.out.println("Connected to Database.");

        // Create a collection. The default similarity metric is cosine.
        CollectionDefinition collectionDefinition = new CollectionDefinition()
                .vectorDimension(5)
                .vectorSimilarity(COSINE);
        Collection<Document> collection = db.createCollection("vector2", collectionDefinition);

        db.createCollection("sdfdsf", new CollectionDefinition()
                .vectorize("openai", "eeneee"));
        System.out.println("Created a collection");

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

        // Perform a similarity search
        Filter filter = null;
        CollectionFindOptions options = new CollectionFindOptions()
                .sort(vector(new float[]{0.15f, 0.1f, 0.1f, 0.35f, 0.55f}))
                .limit(10);
        FindIterable<Document> resultsSet = collection.find(filter,options);
        resultsSet.forEach(System.out::println);

        // Delete the collection
        collection.drop();
        System.out.println("Deleted the collection");
    }
}
