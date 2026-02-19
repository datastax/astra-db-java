package com.datastax.astra.samples.quickstart;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.commands.options.CreateCollectionOptions;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceOptions;

import java.util.Optional;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static com.datastax.astra.client.core.query.Filters.eq;

/**
 * End-to-end HCD quickstart: {@link UsernamePasswordTokenProvider},
 * {@link DataAPIDatabaseAdmin} cast, {@code createKeyspace(simpleStrategy)},
 * vectorize collection with query.
 *
 * @see DataAPIDatabaseAdmin
 * @see UsernamePasswordTokenProvider
 */
@SuppressWarnings("unused")
public class SampleQuickstartHCD {

    static void quickstartHCD() {
        // Settings
        String cassandraUserName     = "cassandra";
        String cassandraPassword     = "cassandra";
        String keyspaceName          = "ks1";
        String collectionName        = "lyrics";
        String dataApiUrl            = DEFAULT_ENDPOINT_LOCAL; // http://localhost:8181

        // OpenAI Embeddings config
        String openAiProvider        = "openai";
        String openAiKey             = "OPENAI_API_KEY";
        String openAiModel           = "text-embedding-3-small";
        int openAiEmbeddingDimension = 1536;

        // Build token: Cassandra:base64(username):base64(password)
        String token = new UsernamePasswordTokenProvider(cassandraUserName, cassandraPassword).getTokenAsString();

        // Initialize client for HCD destination
        DataAPIClient client = new DataAPIClient(token,
                new DataAPIClientOptions().destination(DataAPIDestination.HCD));

        // Create keyspace via DataAPIDatabaseAdmin
        ((DataAPIDatabaseAdmin) client
                .getDatabase(dataApiUrl)
                .getDatabaseAdmin())
                .createKeyspace(keyspaceName, KeyspaceOptions.simpleStrategy(1));

        // Connect to database with keyspace
        Database db = client.getDatabase(dataApiUrl,
                new DatabaseOptions().keyspace(keyspaceName));

        // Create vectorize collection with OpenAI
        CollectionDefinition cd = new CollectionDefinition()
                .vectorSimilarity(SimilarityMetric.COSINE)
                .vectorDimension(openAiEmbeddingDimension)
                .vectorize(openAiProvider, openAiModel);
        CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions()
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(openAiKey));
        Collection<Document> collectionLyrics = db.createCollection(collectionName, cd,
                Document.class, createCollectionOptions);

        // Insert documents with vectorize
        collectionLyrics.insertMany(
                new Document(1).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("He's underneath the window"),
                new Document(6).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).append("band", "Dire Straits").append("song", "Romeo And Juliet")
                        .vectorize("Anyway, what you gonna do about it?"));

        // Find with vectorize and similarity
        Optional<Document> doc = collectionLyrics.findOne(
                eq("band", "Dire Straits"),
                new CollectionFindOneOptions()
                        .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha"))
                        .includeSimilarity(true));
    }
}
