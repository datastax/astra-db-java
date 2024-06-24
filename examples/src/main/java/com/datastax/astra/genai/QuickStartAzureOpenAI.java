package com.datastax.astra.genai;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOneOptions;

import java.util.Map;
import java.util.Optional;

/**
 * This code shows how to use the DataStax Astra API to generate AI models.
 */
public class QuickStartAzureOpenAI {

    /**
     * Prerequisites:
     * ------------------
     * - Create an Astra Account
     * - Create an Astra Database
     * - Create a token
     * - Create an Azure OpenAI account
     * - Get
     * - Create an Azure OpenAI deployment (deploymentId)
     * - Create an Azure OpenAI resource (resourceName)
     * Documentation
     * https://d5rxiv0do0q3v.cloudfront.net/vector-395/astra-db-serverless/integrations/embedding-providers/azure-openai.html
     */

    /**
     * Assuming you define an key in Integration for Azure OpenAU
     */
    static final String ASTRA_DB_TOKEN             = "<change_me>";
    static final String ASTRA_DB_URL               = "<change_me>";
    static final String API_KEY_NAME                 = "<change_me>";
    static final String DEPLOYMENT_ID         = "<change_me>";
    static final String RESOURCE_NAME         = "<change_me>";
    static final String AZURE_OPENAI_PROVIDER      = "azureOpenAI";
    static final String AZURE_OPENAI_MODEL_NAME    = "text-embedding-3-small";

    public static void main(String[] args) {
        Database db = new DataAPIClient(ASTRA_DB_TOKEN).getDatabase(ASTRA_DB_URL);

        // 1/ Create a collection programmatically (if needed)
        CollectionOptions options = CollectionOptions
                .builder()
                .vectorize(
                        "azureOpenAI",
                        "text-embedding-ada-002",
                        API_KEY_NAME,
                  Map.of("deploymentId", DEPLOYMENT_ID,
                         "resourceName", RESOURCE_NAME))
                .build();
        Collection<Document> collectionAzureOpenAI = db
                .createCollection("vectorize_test", options);

        // If you create collection from the portal you can
        //Collection<Document> collectionAzureOpenAI = db.getCollection("collection_azure_openai");

        collectionAzureOpenAI.deleteAll();
        collectionAzureOpenAI.insertMany(
                new Document(1).vectorize("A lovestruck Romeo sings the streets a serenade"),
                new Document(2).vectorize("Finds a streetlight, steps out of the shade"),
                new Document(3).vectorize("Says something like, You and me babe, how about it?"),
                new Document(4).vectorize("Juliet says,Hey, it's Romeo, you nearly gimme a heart attack"),
                new Document(5).vectorize("He's underneath the window"),
                new Document(6).vectorize("She's singing, Hey la, my boyfriend's back"),
                new Document(7).vectorize("You shouldn't come around here singing up at people like that"),
                new Document(8).vectorize("Anyway, what you gonna do about it?"));

        // Find the document
        Optional<Document> doc = collectionAzureOpenAI.findOne(new FindOneOptions()
                .sort("You shouldn't come around here singing up at people like tha"));
        System.out.println("A document has found been : " + doc);
    }

}
