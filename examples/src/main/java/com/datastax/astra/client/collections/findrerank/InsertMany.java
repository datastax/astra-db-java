package com.datastax.astra.client.collections.findrerank;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;

public class InsertMany {

    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        Document doc1 = new Document().append("name", "John Doe")
                .vector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f})
                .lexical("Text for lexical search");
        Document doc2 = new Document().append("name", "Mary Day")
                .vector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f})
                .lexical("Text for vector search");
        Document doc3 = new Document().append("name", "'Bobby'")
                .hybrid(new Hybrid("Common text for both vectorize and lexical search"));
        collection.insertMany(doc1, doc2, doc3);
    }
}


/*
// With Vectorize
        for (RerankedResult<Document> results : collection
                .findAndRerank(new CollectionFindAndRerankOptions()
                        .sort(Sort.hybrid("A tree on a hill")))) {
            System.out.println(results.getDocument());
        }

        // Without Vectorize
        Hybrid hybrid = new Hybrid()
                .vector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f})
                .lexical("A tree on a hill");
        for (RerankedResult<Document> results : collection
                .findAndRerank(new CollectionFindAndRerankOptions()
                        .sort(Sort.hybrid(hybrid))
                        .rerankOn("$lexical")
                        .rerankQuery("A house in the woods"))) {
            System.out.println(results.getDocument());
        }

        // Use a different query in the reranking step
        Hybrid hybrid2 = new Hybrid()
                .vector(new DataAPIVector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f}))
                .lexical("lexical query");
        for (RerankedResult<Document> results : collection
                .findAndRerank(new CollectionFindAndRerankOptions()
                        .sort(Sort.hybrid(hybrid2))
                        .rerankOn("$lexical")
                        .rerankQuery("A house in the woods"))) {
            System.out.println(results.getDocument());
        }

        // Use Filters
        Filter filter = Filters.and(
                Filters.eq("isCheckedOut", false),
                Filters.lt("numberOfPages", 300)
        );
 */
