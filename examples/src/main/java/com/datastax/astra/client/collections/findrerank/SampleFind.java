package com.datastax.astra.client.collections.findrerank;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.RerankedResult;

public class SampleFind {

    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

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
    }
}
