package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.client.core.Filter;
import com.datastax.astra.client.core.Filters;
import com.datastax.astra.client.collections.commands.FindIterable;
import com.datastax.astra.client.collections.commands.FindOptions;
import com.datastax.astra.client.core.Sorts;

import static com.datastax.astra.client.core.Filters.lt;
import static com.datastax.astra.client.core.Projections.exclude;
import static com.datastax.astra.client.core.Projections.include;

public class Find {
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

        // Find Options
        FindOptions options = new FindOptions()
                .projection(include("field", "field2", "field3")) // select fields
                .projection(exclude("_id")) // exclude some fields
                .sort(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f}) // similarity vector
                .skip(1) // skip first item
                .limit(10) // stop after 10 items (max records)
                .pageState("pageState") // used for pagination
                .includeSimilarity(); // include similarity

        // Execute a find operation
        FindIterable<Document> result = collection.find(filter, options);

        // Iterate over the result
        for (Document document : result) {
            System.out.println(document);
        }
    }
}
