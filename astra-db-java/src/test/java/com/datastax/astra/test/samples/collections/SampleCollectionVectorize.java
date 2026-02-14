package com.datastax.astra.test.samples.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.query.Sort;

import static com.datastax.astra.client.core.query.Sort.vectorize;

/**
 * Demonstrates vectorize usage in find ({@link Sort#vectorize(String)} with
 * {@link EmbeddingAPIKeyHeaderProvider}) and insert ({@link Document#vectorize(String)}).
 *
 * @see Collection
 * @see Sort#vectorize(String)
 */
@SuppressWarnings("unused")
public class SampleCollectionVectorize {

    /** Find with vectorize and vector sort options. */
    static void findWithVectorize() {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        Sort s1 = Sort.ascending("field1");
        Sort s2 = Sort.descending("field2");

        // Vectorize sort with embedding API key
        CollectionFindOptions options1 = new CollectionFindOptions()
                .embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider("MY KEY"))
                .sort(vectorize("String to Vectorize"), s1, s2);

        // Vector sort with raw float array
        float[] vector = new float[] {0.25f, 0.25f, 0.25f, 0.25f, 0.25f};
        CollectionFindOptions options2 = new CollectionFindOptions()
                .sort(Sort.vector(vector), s2);
    }

    /** Insert documents with vector and vectorize. */
    static void insertWithVectorize() {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        float[] vector = new float[] {0.25f, 0.25f, 0.25f, 0.25f, 0.25f};

        Document doc1 = new Document()
                .vector(vector)
                .append("metadata1", "value1");

        Document doc2 = new Document()
                .vectorize("String to Vectorize")
                .append("metadata1", "value1");

        collection.insertMany(doc1, doc2);
    }
}
