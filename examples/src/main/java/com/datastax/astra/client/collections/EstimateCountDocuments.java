package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.options.EstimatedCountDocumentsOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.internal.command.LoggingCommandObserver;

public class EstimateCountDocuments {

    public static void main(String[] args)  {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Count with no filter
        long estimatedCount = collection.estimatedDocumentCount();

        // Count with options (adding a logger)
        EstimatedCountDocumentsOptions options = new EstimatedCountDocumentsOptions()
                    .registerObserver("logger", new LoggingCommandObserver(DataAPIClient.class));
        long estimateCount2 = collection.estimatedDocumentCount(options);
    }


}
