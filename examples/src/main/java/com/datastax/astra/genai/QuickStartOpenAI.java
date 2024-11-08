package com.datastax.astra.genai;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.CollectionIdTypes;
import com.datastax.astra.client.collections.CollectionOptions;
import com.datastax.astra.client.collections.results.CollectionInsertManyResult;
import com.datastax.astra.client.core.paging.FindIterable;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This code shows how to use the DataStax Astra API to generate AI models.
 */
public class QuickStartOpenAI {

    /**
     * Prerequisites:
     * ------------------
     * - Create an Astra Account
     * - Create an Astra Database
     * - Create a token
     * - Create an OpenAI account
     * - Create an OpenAI project (projectId)
     * - Create an OpenAI key
     * https://d5rxiv0do0q3v.cloudfront.net/vector-395/astra-db-serverless/integrations/embedding-providers/openai.html
     */

    static final String ASTRA_DB_TOKEN  = "<change_me>";
    static final String ASTRA_DB_URL    = "<change_me>";
    static final String API_KEY_NAME     = "<change_me>";
    static final String ORGANIZATION_ID  = "<change_me>";
    static final String PROJECT_ID       = "<change_me>";

    public static void main(String[] args) {
        Database db = new DataAPIClient(ASTRA_DB_TOKEN).getDatabase(ASTRA_DB_URL);

        // 1/ Create a collection programmatically (if needed)
        Map<String, Object > params = new HashMap<>();
        params.put("organizationId", ORGANIZATION_ID);
        params.put("projectId", PROJECT_ID);
        CollectionOptions.CollectionOptionsBuilder builder = CollectionOptions
                .builder()
                .vectorSimilarity(SimilarityMetric.COSINE)
                .defaultIdType(CollectionIdTypes.UUID)
                .vectorize("openai","text-embedding-ada-002", API_KEY_NAME,params);
        Collection<Document> collection = db
                .createCollection("vectorize_test", builder.build());

        collection.deleteAll();
        CollectionInsertManyResult insertResult = collection.insertMany(
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
        Sort s = Sort.vectorize("I'd like some talking shoes");
        CollectionFindOptions findOptions = new CollectionFindOptions()
                .sort(s)
                .limit(2)
                .includeSimilarity(true);
        FindIterable<Document> results = collection.find(findOptions);
        for (Document document : results) {
            System.out.println("Document: " + document);
        }
    }

}
