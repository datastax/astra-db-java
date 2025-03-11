package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;

import java.util.Set;

import static com.datastax.astra.client.core.query.Filters.lt;

public class Distinct {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Building a filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        // Execute a find operation
        Set<String> result = collection.distinct("field", String.class);
        Set<String> result2 = collection.distinct("field", filter, String.class);

        // Iterate over the result
        for (String fieldValue : result) {
            System.out.println(fieldValue);
        }
    }
}
