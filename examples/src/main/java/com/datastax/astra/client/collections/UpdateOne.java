package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.client.core.Filter;
import com.datastax.astra.client.core.Filters;
import com.datastax.astra.client.collections.documents.Update;
import com.datastax.astra.client.collections.commands.UpdateResult;
import com.datastax.astra.client.collections.documents.Updates;

import static com.datastax.astra.client.core.Filters.lt;

public class UpdateOne {
    // Given an existing collection
    Collection<Document> collection = new DataAPIClient("TOKEN")
            .getDatabase("API_ENDPOINT")
            .getCollection("COLLECTION_NAME");

    // Building a filter
    Filter filter = Filters.and(
            Filters.gt("field2", 10),
            lt("field3", 20),
            Filters.eq("field4", "value"));

    // Building the update
    Update update = Updates.set("field1", "value1")
            .inc("field2", 1d)
            .unset("field3");

    UpdateResult result = collection.updateOne(filter, update);
}
