package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.collections.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.options.CollectionFindOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.types.DataAPIKeywords;
import com.datastax.astra.client.databases.Database;
import org.junit.jupiter.api.Test;

public class WikiDataTestVectorize {

    @Test
    public void shouldConnectToAstra() {
        String token = "omitted";
        DataAPIClient client = new DataAPIClient(token);
        Database db = client.getDatabase("https://fd0957cf-dcdc-41ea-bf50-77367bacaf7a-us-east-2.apps.astra.datastax.com");
        Collection<Document> qids = db.getCollection("qids_nvidia");
        System.out.println(qids.estimatedDocumentCount());

        CollectionFindOptions findOptions = new CollectionFindOptions()
                .sort(Sort.vectorize("Battleground"))
                .projection(Projection.exclude(DataAPIKeywords.VECTOR.getKeyword()))
                .limit(10)
                .includeSimilarity(true);

        qids.find(null, findOptions).forEach(doc -> {
            System.out.println(doc);
        });

    }
}
