import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceOptions;

import java.util.Optional;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static com.datastax.astra.client.DataAPIDestination.HCD;
import static com.datastax.astra.client.core.query.Filters.eq;

public class QuickStartHCD {

    public static void main(String[] args) {

        // Some Settings
        String cassandraUserName     = "cassandra";
        String cassandraPassword     = "cassandra";
        String keyspaceName          = "ks1";
        String collectionName        = "lyrics";
        String dataApiUrl            = DEFAULT_ENDPOINT_LOCAL;  // http://localhost:8181

        // OpenAI Embeddings
        String openAiProvider        = "openai";
        String openAiKey             = System.getenv("OPENAI_API_KEY");
        String openAiModel           = "text-embedding-3-small";
        int openAiEmbeddingDimension = 1536;

        // Build a token in the form of Cassandra:base64(username):base64(password)
        String token = new UsernamePasswordTokenProvider(cassandraUserName, cassandraPassword).getTokenAsString();
        System.out.println("1/7 - Creating Token: " + token);

        // Initialize the client
        DataAPIClient client = new DataAPIClient(token, new DataAPIClientOptions().destination(HCD));
        System.out.println("2/7 - Connected to Data API");

        // Create a default keyspace
        ((DataAPIDatabaseAdmin) client
                .getDatabase(dataApiUrl)
                .getDatabaseAdmin())
                .createKeyspace(keyspaceName, KeyspaceOptions.simpleStrategy(1));
        System.out.println("3/7 - Keyspace '" + keyspaceName + "'created ");

        DatabaseOptions options = new DatabaseOptions().keyspace(keyspaceName);
        Database db = client.getDatabase(dataApiUrl, options);
        System.out.println("4/7 - Connected to Database");

        // Create a collection with Vector embeddings OPEN AI
        CollectionDefinition cd = new CollectionDefinition()
                .vectorSimilarity(SimilarityMetric.COSINE)
                .vectorDimension(openAiEmbeddingDimension)
                .vectorize(openAiProvider, openAiModel);
        CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions()
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(openAiKey));
        Collection<Document> collectionLyrics = db.createCollection(collectionName, cd, Document.class,
                createCollectionOptions, new CollectionOptions());
        System.out.println("5/7 - Collection created with OpenAI embeddings");

        // Insert some documents
        collectionLyrics.insertMany(
                new Document(1).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("He's underneath the window"),
                new Document(6).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).append("band", "Dire Straits").append("song", "Romeo And Juliet").vectorize("Anyway, what you gonna do about it?"));
        System.out.println("6/7 - Collection populated");

        // Find one document
        Optional<Document> doc = collectionLyrics.findOne(
                eq("band", "Dire Straits"),
                new CollectionFindOneOptions()
                        .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha"))
                        .includeSimilarity(true));
        System.out.println("7/7 - Found document: " + doc);
    }

}
