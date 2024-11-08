package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.paging.FindIterable;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;

import static com.datastax.astra.client.core.query.Filters.lt;
import static com.datastax.astra.client.core.query.Projection.exclude;
import static com.datastax.astra.client.core.query.Projection.include;

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
        CollectionFindOptions options = new CollectionFindOptions()
                .projection(include("field", "field2", "field3")) // select fields
                .projection(exclude("_id")) // exclude some fields
                .sort(Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f})) // similarity vector
                .skip(1) // skip first item
                .limit(10) // stop after 10 items (max records)
                .pageState("pageState") // used for pagination
                .includeSimilarity(true); // include similarity

        // Execute a find operation
        FindIterable<Document> result = collection.find(filter, options);

        // Iterate over the result
        for (Document document : result) {
            System.out.println(document);
        }
    }
}
