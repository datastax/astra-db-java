package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.FindOptions;
import com.datastax.astra.client.model.Sort;
import com.datastax.astra.client.model.Sorts;

import static com.datastax.astra.client.model.Filters.lt;

public class WorkingWithSorts {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Sort Clause for a vector
        Sorts.vector(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f});;

        // Sort Clause for other fields
        Sort s1 = Sorts.ascending("field1");
        Sort s2 = Sorts.descending("field2");

        // Build the sort clause
        FindOptions.Builder.sort(s1, s2);

        // Adding vector
        FindOptions.Builder.sort(new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f}, s1, s2);

    }
}
