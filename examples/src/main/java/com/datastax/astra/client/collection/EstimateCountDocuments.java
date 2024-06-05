package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.EstimatedCountDocumentsOptions;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.internal.command.LoggingCommandObserver;

import static com.datastax.astra.client.model.Filters.lt;

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
