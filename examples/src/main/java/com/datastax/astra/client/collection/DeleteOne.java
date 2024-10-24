package com.datastax.astra.client.collection;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.commands.DeleteOneOptions;
import com.datastax.astra.client.collections.commands.DeleteResult;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.client.core.Filter;
import com.datastax.astra.client.core.Filters;
import com.datastax.astra.client.core.Sorts;

import static com.datastax.astra.client.core.Filters.lt;

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
        DeleteOneOptions options = new DeleteOneOptions()
                .sort(Sorts.ascending("field2"));
        DeleteResult result = collection.deleteOne(filter, options);
        System.out.println("Deleted Count:" + result.getDeletedCount());
    }
}
