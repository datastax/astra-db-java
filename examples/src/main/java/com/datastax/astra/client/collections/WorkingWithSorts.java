package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Sort;

public class WorkingWithSorts {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Sort Clause for a vector
        Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f});;

        // Sort Clause for other fields
        Sort s1 = Sort.ascending("field1");
        Sort s2 = Sort.descending("field2");

        // Build the sort clause
        new CollectionFindOptions().sort(s1, s2);

        // Adding vector
        new CollectionFindOptions().sort(
                Sort.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f}), s1, s2);

    }
}
