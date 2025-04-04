package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.commands.options.CollectionDeleteOneOptions;
import com.datastax.astra.client.collections.commands.results.CollectionDeleteResult;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.core.query.Sort;

import static com.datastax.astra.client.core.query.Filters.lt;

public class DeleteOne {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Sample Filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        // Delete one options
        CollectionDeleteOneOptions options = new CollectionDeleteOneOptions()
                .sort(Sort.ascending("field2"));
        CollectionDeleteResult result = collection.deleteOne(filter, options);
        System.out.println("Deleted Count:" + result.getDeletedCount());
    }
}
