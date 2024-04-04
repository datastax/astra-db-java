package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.FindOneOptions;

import java.util.Optional;

import static com.datastax.astra.client.model.Filters.and;
import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Filters.gt;
import static com.datastax.astra.client.model.Filters.lt;
import static com.datastax.astra.client.model.FindOneOptions.Builder.sort;
import static com.datastax.astra.client.model.Projections.exclude;
import static com.datastax.astra.client.model.Projections.include;

public class FindOne {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Complete FindOne
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));
        FindOneOptions options = new FindOneOptions()
                .projection(include("field", "field2", "field3"))
                .projection(exclude("_id"))
                .sort(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f})
                .includeSimilarity();
        Optional<Document> result = collection.findOne(filter, options);

        // with the import Static Magic
        collection.findOne(and(
                gt("field2", 10),
                lt("field3", 20),
                eq("field4", "value")),
               sort(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f})
                .projection(include("field", "field2", "field3"))
                .projection(exclude("_id"))
                .includeSimilarity()
        );

    }
}
