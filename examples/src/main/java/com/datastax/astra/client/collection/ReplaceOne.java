package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.Filters;
import com.datastax.astra.client.model.FindOneAndReplaceOptions;
import com.datastax.astra.client.model.Projections;
import com.datastax.astra.client.model.ReplaceOneOptions;
import com.datastax.astra.client.model.Sorts;
import com.datastax.astra.client.model.UpdateResult;

import java.util.Optional;

import static com.datastax.astra.client.model.Filters.lt;

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

        ReplaceOneOptions options = ReplaceOneOptions.Builder
                .upsert(true);

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
