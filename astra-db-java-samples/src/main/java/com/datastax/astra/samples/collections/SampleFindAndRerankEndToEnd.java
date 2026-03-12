package com.datastax.astra.samples.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.cursor.CollectionFindAndRerankCursor;
import com.datastax.astra.client.collections.commands.options.CollectionFindAndRerankOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.rerank.RerankedResult;
import lombok.Data;

/**
 * Demonstrates {@code findAndRerank} with {@link Hybrid}, projection to custom POJO,
 * {@code rerankOn("$lexical")}, and {@code rerankQuery()}.
 *
 * @see Collection#findAndRerank(Filter, CollectionFindAndRerankOptions, Class)
 * @see Hybrid
 */
@SuppressWarnings("unused")
public class SampleFindAndRerankEndToEnd {

    public static void main(String[] args) {
        // Connect

        // Creqte


        {"name":"test_collection","options":{"vector":{"dimension":1024,"metric":"cosine","sourceModel":"other","service":{"provider":"nvidia","modelName":"nvidia/nv-embedqa-e5-v5"}},"lexical":{"enabled":true,"analyzer":{"tokenizer":{"name":"standard"},"filters":[{"name":"lowercase"},{"name":"stop"},{"name":"porterstem"},{"name":"asciifolding"}]}},"rerank":{"enabled":true,"service":{"provider":"nvidia","modelName":"nvidia/llama-3.2-nv-rerankqa-1b-v2"}}}},
    }

    @Data
    public static class Projected {
        boolean isCheckedOut;
        String title;
    }

    static void findAndRerankWithHybrid() {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        Hybrid hybrid = new Hybrid()
                .vector(new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f})
                .lexical("lexical query");

        Filter filter = null;
        CollectionFindAndRerankCursor<Document, Projected> cursor = collection
                .findAndRerank(filter, new CollectionFindAndRerankOptions()
                        .projection(Projection.include("isCheckedOut", "title"))
                        .sort(Sort.hybrid(hybrid))
                        .rerankQuery("rerank query")
                        .rerankOn("$lexical"), Projected.class);

        for (RerankedResult<Projected> results : cursor) {
            // Process reranked results
        }
    }
}
