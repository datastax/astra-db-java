package com.datastax.astra.test.integration.astra.collection;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.test.integration.EmbeddingModelType;
import org.junit.jupiter.api.Test;

public class DemoAstraDevVectorize {

    // TEST
    public static final String ASTRA_DB_TOKEN_TEST = System.getenv("ASTRA_DB_APPLICATION_TOKEN_TEST");

    public static final String ASTRA_URL =
            "https://71589bc7-a75f-4153-ab16-9fce04ddf573-westus2.apps.astra-test.datastax.com";

    //PROD
//    public static final String ASTRA_DB_TOKEN_TEST =
//            "AstraCS:JFnexmAARqMfLtmBHHLinlsY:d12d79a9c0bd49be240b2ce346f3d9e276cb25ce12f37fb4e9a6ff0783f333fe"; // Replace with your actual Astra DB token for testing
//    public static final String ASTRA_URL =
//            "https://4d3d14ac-5e95-4121-a38f-de3f1491f1ea-us-east-2.apps.astra.datastax.com";

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
        // Create a collection with Vectorize KMS
        String KMS_KEY_NAME = "KEY_KMS";

        CollectionDefinition collectionDefinition = new CollectionDefinition()
                .vectorDimension(EmbeddingModelType.OPENAI_3_SMALL.getDimension())
                .vectorSimilarity(SimilarityMetric.COSINE)
                .vectorize(EmbeddingModelType.OPENAI_3_SMALL.getProvider(),
                        EmbeddingModelType.OPENAI_3_SMALL.getName(),
                        KMS_KEY_NAME);

        getDatabase()
                .createCollection("collection_kms", collectionDefinition);



    }


}
