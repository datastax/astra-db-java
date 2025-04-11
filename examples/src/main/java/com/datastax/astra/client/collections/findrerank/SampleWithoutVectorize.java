package com.datastax.astra.client.collections.findrerank;

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

public class SampleWithoutVectorize {

    @Data
    public static class Projected {
        boolean isCheckedOut;
        String title;
    }

    public static void main(String[] args) {
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

        // note he projection can also done at the cursor level
        //cursor.project(Projection.include("isCheckedOut", "title"));

        for (RerankedResult<Projected> results : cursor) {
            // Limit the number of properties resulted
        }
    }
}
