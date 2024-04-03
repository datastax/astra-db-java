package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.FindIterable;
import com.datastax.astra.client.model.FindOptions;

import static com.datastax.astra.client.model.Filters.lt;
import static com.datastax.astra.client.model.Projections.exclude;
import static com.datastax.astra.client.model.Projections.include;

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
