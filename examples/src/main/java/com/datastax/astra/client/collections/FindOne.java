package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.collections.options.CollectionFindOneOptions;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Sort;

import java.util.Optional;

import static com.datastax.astra.client.core.query.Filters.and;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.gt;
import static com.datastax.astra.client.core.query.Filters.lt;
import static com.datastax.astra.client.core.query.Projection.exclude;
import static com.datastax.astra.client.core.query.Projection.include;

public class FindOne {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Complete FindOne
        Filter filter = and(
                gt("field2", 10),
                lt("field3", 20),
                eq("field4", "value"));
        CollectionFindOneOptions options = new CollectionFindOneOptions()
                .projection(include("field", "field2", "field3"))
                .projection(exclude("_id"))
                .sort(Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f}))
                .includeSimilarity(true);
        Optional<Document> result = collection.findOne(filter, options);

        // with the import Static Magic
        collection.findOne(and(
                gt("field2", 10),
                lt("field3", 20),
                eq("field4", "value")),
               new CollectionFindOneOptions().sort(Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f}))
                .projection(include("field", "field2", "field3"))
                .projection(exclude("_id"))
                .includeSimilarity(true)
        );

        // find one with a vectorize
        collection.findOne(and(
                        gt("field2", 10),
                        lt("field3", 20),
                        eq("field4", "value")),
                new CollectionFindOneOptions().sort(Sort.vectorize("Life is too short to be living somebody else's dream."))
                        .projection(include("field", "field2", "field3"))
                        .projection(exclude("_id"))
                        .includeSimilarity(true)
        );

        collection.insertOne(new Document()
                .append("field", "value")
                .append("field2", 15)
                .append("field3", 15)
                .vectorize("Life is too short to be living somebody else's dream."));

    }
}
