package com.datastax.astra.genai;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.SimilarityMetric;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This demo want to illustrate how to use the java client in GenAI Context
 */
@Slf4j
public class AzureOpenAI_SharedSecret_AstraDev {

    public static void main(String[] args) {

        String astraToken   = System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV");
        String keyName      = "stefano";
        String providerName = "azureOpenAI";
        String modelName    = "text-embedding-3-small";
        int vectorDimension = 1536;

        DataAPIClient dataAPIClient =  new DataAPIClient(astraToken, DataAPIOptions
                .builder()
                .withDestination(DataAPIOptions.DataAPIDestination.ASTRA_DEV)
                .build());

        // Create a collection
        Database db = dataAPIClient.getDatabase(UUID.fromString("2341e6dc-c6b2-4031-9c36-a93b8c1549e0"));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("deploymentId", "text-embedding-3-small-steo");
        parameters.put("resourceName", "steo-azure-openai");
        Collection<Document> collection = db.createCollection(
                "collection_azure_openai",
                // Create collection with a Service in vectorize
                CollectionOptions.builder()
                        .vectorDimension(vectorDimension)
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(providerName, modelName, keyName, parameters)
                        .build(), new CommandOptions<>());

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
                .sort("You shouldn't come around here singing up at people like tha"));

       log.info("A document has found been : " + doc);

    }
}
