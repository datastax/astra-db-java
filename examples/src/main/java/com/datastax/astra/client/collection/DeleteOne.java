package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.DeleteResult;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.Sorts;

import static com.datastax.astra.client.model.Filters.lt;

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
