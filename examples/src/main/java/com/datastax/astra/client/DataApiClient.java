package com.datastax.astra.client;

import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.model.Document;

import java.util.List;
import java.util.UUID;

import static com.datastax.astra.client.model.SimilarityMetric.COSINE;

public class DataApiClient {
    public static void main(String[] args) {
        DataAPIClient client = new DataAPIClient("TOKEN");
        Database database0 = client.getDatabase("API_ENDPOINT");
        Collection<Document> collection0 = database0.createCollection("movies", 2, COSINE);
        collection0.insertOne(new Document().append("title", "The Title").vector(new float[]{1.0f, 1.0f}));
        Database database1 = client.getDatabase(UUID.fromString("01234567-..."));
        Database database2 = client.getDatabase(UUID.fromString("01234567-..."), "*NAMESPACE*", "us-east1");
        AstraDBAdmin admin1 = client.getAdmin();
        AstraDBAdmin admin2 = client.getAdmin("more_powerful_token_override");
        List<String> databases = admin1.listDatabaseNames();
    }
}
