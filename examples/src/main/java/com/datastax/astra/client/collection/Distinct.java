package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.DistinctIterable;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.FindOptions;

import static com.datastax.astra.client.model.Filters.lt;
import static com.datastax.astra.client.model.Projections.exclude;
import static com.datastax.astra.client.model.Projections.include;

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
