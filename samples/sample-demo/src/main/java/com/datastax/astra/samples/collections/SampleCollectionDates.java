package com.datastax.astra.samples.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.commands.options.CollectionFindOneOptions;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.core.query.Projection;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import static com.datastax.astra.client.collections.commands.Updates.set;
import static com.datastax.astra.client.core.query.Filters.eq;
import static com.datastax.astra.client.core.query.Filters.lt;

/**
 * Demonstrates inserting, filtering, and updating documents
 * with {@link Calendar}, {@link Date}, and {@link Instant} date types.
 *
 * @see Collection
 */
@SuppressWarnings("unused")
public class SampleCollectionDates {

    static void workingWithDates() {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Insert documents with different date types
        Calendar c = Calendar.getInstance();
        collection.insertOne(new Document().append("registered_at", c));
        collection.insertOne(new Document().append("date_of_birth", new Date()));
        collection.insertOne(new Document().append("just_a_date", Instant.now()));

        // Update using a Calendar filter
        collection.updateOne(
                eq("registered_at", c),
                set("message", "happy Sunday!"));

        // Find using a Date filter with projection
        collection.findOne(
                lt("date_of_birth", new Date(System.currentTimeMillis() - 1000 * 1000)),
                new CollectionFindOneOptions().projection(Projection.exclude("_id")));
    }
}
