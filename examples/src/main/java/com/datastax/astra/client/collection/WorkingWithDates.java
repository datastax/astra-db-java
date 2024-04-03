package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Projections;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Filters.lt;
import static com.datastax.astra.client.model.FindOneOptions.Builder.projection;
import static com.datastax.astra.client.model.Updates.set;

public class WorkingWithDates {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        Calendar c = Calendar.getInstance();
        collection.insertOne(new Document().append("registered_at", c));
        collection.insertOne(new Document().append("date_of_birth", new Date()));
        collection.insertOne(new Document().append("just_a_date", Instant.now()));

        collection.updateOne(
                eq("registered_at", c), // filter clause
                set("message", "happy Sunday!")); // update clause

        collection.findOne(
                lt("date_of_birth", new Date(System.currentTimeMillis() - 1000 * 1000)),
                projection(Projections.exclude("_id")));
    }
}
