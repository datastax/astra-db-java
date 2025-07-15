package com.datastax.astra.test.integration.astra;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.Database;
import org.junit.jupiter.api.Test;

public class DemoAstraDevVectorize {

    public static final String ASTRA_DB_TOKEN_TEST =
            "AstraCS:EEdyFLdZCZqGEkWyHCdjRFxB:1204d23e42803a9bcae9c712a89eed75678ec7cdfc89c07a27af945bf1cfbaa9";

    public static final String ASTRA_URL =
            "https://71589bc7-a75f-4153-ab16-9fce04ddf573-westus2.apps.astra-test.datastax.com";
            //"https://b527003e-aefb-43fe-a1e9-92eef7ba4f2b-us-east-1.apps.astra-test.datastax.com";

    private DataAPIClient getAstraDevDataApiClient() {
        DataAPIClientOptions options = new DataAPIClientOptions()
                .destination(DataAPIDestination.ASTRA_TEST)
                .logRequests();
        return new DataAPIClient(ASTRA_DB_TOKEN_TEST, options);
    }

    private Database getDatabase() {
        return getAstraDevDataApiClient().getDatabase(ASTRA_URL);
    }

    @Test
    public void should_list_collection() {
        getDatabase().listCollections().forEach(System.out::println);
    }

    @Test
    public void should_insert_vectorized_documents() {
        Collection<Document> collec = getDatabase()
                .getCollection("collectiion_openai");
//
//        collec.insertOne(new Document().id("doc1")
//                .vectorize("IT IS WORKING")
//                .append("model", "cedrick"));
    }


}
