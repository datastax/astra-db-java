package com.datastax.astra.client.collections.vectorize;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.databases.Database;

import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.core.auth.UsernamePasswordTokenProvider;
import com.datastax.astra.client.databases.DatabaseOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static com.datastax.astra.client.DataAPIDestination.CASSANDRA;
import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;

/**
 * This demo want to illustrate how to use the java client in GenAI Context
 */
@Slf4j
public class QuickStartOpenAI {

    public static void main(String[] args) {

        EmbeddingModelType embeddingModel  = EmbeddingModelType.OPENAI_3_SMALL;
        String             embeddingApiKey = System.getenv("OPENAI_API_KEY");

        // Token Cassandra:Base64(username):Base64(password)
        String dataAPICassandraToken =  new UsernamePasswordTokenProvider("cassandra", "cassandra").getToken();

        // Create the Client, option is provided at top level and will be available
        DataAPIClient localDataAPI = new DataAPIClient(dataAPICassandraToken, new DataAPIClientOptions()
              .destination(CASSANDRA)
              .embeddingAPIKey(embeddingApiKey)
              .logRequests());

        // Access to the database
        Database localDb = localDataAPI
                .getDatabase(DEFAULT_ENDPOINT_LOCAL, new DatabaseOptions().keyspace("ks1"));

        // Create a Namespace if Needed
        localDb.getDatabaseAdmin().createKeyspace(DEFAULT_KEYSPACE);

        // Create a collection for the provider
        Collection<Document> collection = localDb.createCollection(
                embeddingModel.name().toLowerCase(),
                // Create collection with a Service in vectorize
                new CollectionDefinition()
                        .indexingAllow()
                        .vectorDimension(embeddingModel.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(embeddingModel.getProvider(), embeddingModel.getName()));

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
        Optional<Document> doc = collection.findOne(new CollectionFindOneOptions()
                .sort(Sort.vectorize("You shouldn't come around here singing up at people like tha")));

       log.info("A document has found been : " + doc);

    }
}
