package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;

import java.util.Optional;

import static com.datastax.astra.client.core.query.Filters.lt;

public class FindOneAndDelete {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Building a filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        // It will return the document before deleting it
        Optional<Document> docBeforeRelease = collection.findOneAndDelete(filter);
    }
}
