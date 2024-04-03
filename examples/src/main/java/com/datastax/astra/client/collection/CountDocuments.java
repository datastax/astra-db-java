package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.exception.TooManyDocumentsToCountException;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;

import static com.datastax.astra.client.model.Filters.lt;

public class CountDocuments {
    public static void main(String[] args)  {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Building a filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        try {
            // Count with no filter
            collection.countDocuments(500);

            // Count with a filter
            collection.countDocuments(filter, 500);

        } catch(TooManyDocumentsToCountException tmde) {
            // Explicit error if the count is above the upper limit or above the 1000 limit
        }

    }


}
