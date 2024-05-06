package com.datastax.astra.genai;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.DataAPIDatabaseAdmin;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.DataAPIKeywords;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.auth.TokenProviderStargateV2;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static com.datastax.astra.client.admin.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * This demo want to illustrate how to use the java client in GenAI Context
 */
@Slf4j
public class QuickStartOpenAI {

    public static void main(String[] args) {

        EmbeddingModelType embeddingModel  = EmbeddingModelType.OPENAI_3_SMALL;
        String             embeddingApiKey = System.getenv("OPENAI_API_KEY");

        // Token Cassandra:Base64(username):Base64(password)
        String dataAPICassandraToken =  new TokenProviderStargateV2().getToken();

        // Create the Client, option is provided at top level and will be available
        DataAPIClient localDataAPI = new DataAPIClient(dataAPICassandraToken, DataAPIOptions.builder()
              .withDestination(DataAPIOptions.DataAPIDestination.CASSANDRA)
              .withEmbeddingAPIKey(embeddingApiKey)
              .withObserver(new LoggingCommandObserver(DataAPIClient.class))
              .build());

        // Access to the database
        Database localDb = localDataAPI.getDatabase(DEFAULT_ENDPOINT_LOCAL, DEFAULT_NAMESPACE);

        // Create a Namespace if Needed
        DataAPIDatabaseAdmin dbAdmin = (DataAPIDatabaseAdmin) localDb.getDatabaseAdmin();
        dbAdmin.createNamespace(DEFAULT_NAMESPACE);

        // Create a collection for the provider
        Collection<Document> collection = localDb.createCollection(
                embeddingModel.name().toLowerCase(),
                // Create collection with a Service in vectorize
                CollectionOptions.builder()
                        .vectorDimension(embeddingModel.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(embeddingModel.getProvider(), embeddingModel.getName())
                        .build(),
                // Save API Key at collection level
                new CommandOptions<>().embeddingAPIKey(embeddingApiKey));

        // Insert documents
        collection.deleteAll();
        collection.insertMany(
            new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
            new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
            new Document(3).vectorize("Says something like, You and me babe, how about it?"),
            new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
            new Document(5).vectorize("He's underneath the window"),
            new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
            new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
            new Document(8).vectorize("Anyway, what you gonna do about it?"));

        // Find the document
        Optional<Document> doc = collection.findOne(new FindOneOptions()
                        .sort("You shouldn't come around here singing up at people like tha")
                        .projection(Projections.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                        .embeddingAPIKey(embeddingApiKey)
                        .includeSimilarity());

       log.info("A document has found been : " + doc);

    }
}
