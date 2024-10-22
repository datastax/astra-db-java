package com.datastax.astra.client.collection;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.commands.DistinctIterable;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.client.core.Filter;
import com.datastax.astra.client.core.Filters;

import static com.datastax.astra.client.core.Filters.lt;
import static com.datastax.astra.client.core.Projections.exclude;
import static com.datastax.astra.client.core.Projections.include;

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
        DistinctIterable<Document, String> result = collection
                .distinct("field", String.class);
        DistinctIterable<Document, String> result2 = collection
                .distinct("field", filter, String.class);

        // Iterate over the result
        for (String fieldValue : result) {
            System.out.println(fieldValue);
        }
    }
}
