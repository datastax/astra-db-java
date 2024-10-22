package com.datastax.astra.genai;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.core.CollectionIdTypes;
import com.datastax.astra.client.core.CollectionOptions;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.client.collections.commands.FindIterable;
import com.datastax.astra.client.collections.commands.FindOptions;
import com.datastax.astra.client.collections.commands.InsertManyResult;
import com.datastax.astra.client.core.vector.SimilarityMetric;

import java.util.UUID;

/**
 * This code shows how to use the DataStax Astra API to generate AI models.
 */
public class QuickStartNvidia {

    /**
     * Prerequisites:
     * ------------------
     * - Create an Astra Account
     * - Create an Astra Database
     * - Create a token
     * Documentation
     * https://d5rxiv0do0q3v.cloudfront.net/vector-395/astra-db-serverless/integrations/embedding-providers/nvidia.html
     */

    static final String ASTRA_DB_TOKEN  = "<change_me>";
    static final String ASTRA_DB_URL    = "<change_me>";

    public static void main(String[] args) {
        Database db = new DataAPIClient(ASTRA_DB_TOKEN).getDatabase(ASTRA_DB_URL);

        // 1/ Create a collection programmatically (if needed)
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions
         .builder()
         .vectorSimilarity(SimilarityMetric.COSINE)
         .defaultIdType(CollectionIdTypes.UUID)
         .vectorize("nvidia","NV-Embed-QA");
        Collection<Document> collection = db
          .createCollection("vectorize_test", builder.build());

        collection.deleteAll();
        InsertManyResult insertResult = collection.insertMany(
                new Document()
                        .id(UUID.fromString("018e65c9-df45-7913-89f8-175f28bd7f74"))
                        .vectorize("Chat bot integrated sneakers that talk to you"),
                new Document()
                        .id(UUID.fromString("018e65c9-e1b7-7048-a593-db452be1e4c2"))
                        .vectorize("Finds a streetlight, steps out of the shade"),
                new Document()
                        .id(UUID.fromString("018e65c9-e33d-749b-9386-e848739582f0"))
                        .vectorize("Says something like, You and me babe, how about it?")
        );
        System.out.println("Insert " + insertResult.getInsertedIds().size() + " items.");

        // Find the document
        FindOptions findOptions = new FindOptions()
                .limit(2)
                .includeSimilarity()
                .sort("I'd like some talking shoes");
        FindIterable<Document> results = collection.find(findOptions);
        for (Document document : results) {
            System.out.println("Document: " + document);
        }
    }

}
