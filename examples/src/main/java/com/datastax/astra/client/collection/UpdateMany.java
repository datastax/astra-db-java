package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.Update;
import com.datastax.astra.client.model.UpdateManyOptions;
import com.datastax.astra.client.model.UpdateResult;
import com.datastax.astra.client.model.Updates;

import static com.datastax.astra.client.model.Filters.lt;

public class UpdateMany {

    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Building a filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        Update update = Updates.set("field1", "value1")
                .inc("field2", 1d)
                .unset("field3");

        UpdateManyOptions options =
                UpdateManyOptions.Builder.upsert(true);

        UpdateResult result = collection.updateMany(filter, update, options);
    }
}
