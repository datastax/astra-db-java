package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Filter;
import com.datastax.astra.client.core.query.Filters;
import com.datastax.astra.client.collections.commands.Update;
import com.datastax.astra.client.collections.commands.options.CollectionUpdateManyOptions;
import com.datastax.astra.client.collections.commands.results.CollectionUpdateResult;
import com.datastax.astra.client.collections.commands.Updates;

import static com.datastax.astra.client.core.query.Filters.lt;

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

        CollectionUpdateManyOptions options =
                new CollectionUpdateManyOptions().upsert(true);

        CollectionUpdateResult result = collection.updateMany(filter, update, options);
    }
}
