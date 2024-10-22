package com.datastax.astra.client.collection;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.core.Document;
import com.datastax.astra.client.core.Filter;
import com.datastax.astra.client.core.Filters;
import com.datastax.astra.client.core.Projections;
import com.datastax.astra.client.collections.commands.ReplaceOneOptions;
import com.datastax.astra.client.core.Sorts;
import com.datastax.astra.client.collections.commands.UpdateResult;

import static com.datastax.astra.client.core.Filters.lt;

public class ReplaceOne {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Building a filter
        Filter filter = Filters.and(
                Filters.gt("field2", 10),
                lt("field3", 20),
                Filters.eq("field4", "value"));

        ReplaceOneOptions options = new ReplaceOneOptions().upsert(true);

        Document docForReplacement = new Document()
                .append("field1", "value1")
                .append("field2", 20)
                .append("field3", 30)
                .append("field4", "value4");

        // It will return the document before deleting it
        UpdateResult res = collection.replaceOne(filter, docForReplacement, options);
        System.out.println("How many matches ?"+ res.getMatchedCount());
        System.out.println("How many has been modified ?"+ res.getModifiedCount());
        System.out.println("How many have been inserted ?"+ res.getUpsertedId());

    }
}
