package com.datastax.astra;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;

import static com.datastax.astra.client.model.SimilarityMetric.COSINE;

public class LIveCoding {

    public static void main(String[] args) {
        String astraToken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        String astraApiEndpoint = System.getenv("ASTRA_DB_API_ENDPOINT");


        DataAPIClient client = new DataAPIClient(astraToken, DataAPIOptions.builder().build());
        System.out.println("Connected to AstraDB");

        Database db = client.getDatabase(astraApiEndpoint, "default_keyspace");
        System.out.println("Connected to Database.");

        Collection<Document> collection = db
                .createCollection("vector_test", CollectionOptions.builder()
                        .build());

        System.out.println("Created a collection");


    }
}
