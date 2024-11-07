package com.datastax.astra.client.collections.vectorize;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.collections.commands.FindOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.Sorts;

public class WorkingWithVectorize {


    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        Sort s1 = Sorts.ascending("field1");
        Sort s2 = Sorts.descending("field2");

        // == FIND with VECTOR ==

        // Vector and Vectorize in Sort Clause

        FindOptions options1 = new FindOptions()
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("MY KEY"))
                .sort("String to Vectorize", s1, s2);

        float[] vector = new float[] {0.25f, 0.25f, 0.25f,0.25f, 0.25f};
        FindOptions options2 = new FindOptions()
                .sort(vector, s2);

        // == INSERT with VECTOR ==

        Document doc1 = new Document()
                .vector(vector)
                .append("metadata1", "value1");

        Document doc2 = new Document()
                .vectorize("String to Vectorize")
                .append("metadata1", "value1");

        collection.insertMany(doc1, doc2);

    }

}
