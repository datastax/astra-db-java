package com.datastax.astra.test.samples.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.collections.definition.documents.types.ObjectId;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv6;
import com.datastax.astra.client.collections.definition.documents.types.UUIDv7;

import java.time.Instant;
import java.util.UUID;

import static com.datastax.astra.client.collections.commands.Updates.set;
import static com.datastax.astra.client.core.query.Filters.eq;

/**
 * Demonstrates all supported document ID types: String, Integer, UUID, UUIDv6, UUIDv7,
 * ObjectId, and Instant.
 *
 * @see Document#id(Object)
 * @see ObjectId
 * @see UUIDv6
 * @see UUIDv7
 */
@SuppressWarnings("unused")
public class SampleDocumentIds {

    static void allIdTypes() {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Scalar ID types
        new Document().id("abc");
        new Document().id(123);
        new Document().id(Instant.now());

        // UUIDv4
        new Document().id(UUID.randomUUID());

        // UUIDv6
        collection.insertOne(new Document().id(new UUIDv6()).append("tag", "new_id_v_6"));
        UUID uuidv4 = UUID.fromString("018e77bc-648d-8795-a0e2-1cad0fdd53f5");
        collection.insertOne(new Document().id(new UUIDv6(uuidv4)).append("tag", "id_v_8"));

        // UUIDv7
        collection.insertOne(new Document().id(new UUIDv7()).append("tag", "new_id_v_7"));

        // ObjectId
        collection.insertOne(new Document().id(new ObjectId()).append("tag", "obj_id"));
        collection.insertOne(new Document().id(new ObjectId("6601fb0f83ffc5f51ba22b88")).append("tag", "obj_id"));

        // Find and update with ObjectId filter
        collection.findOneAndUpdate(
                eq((new ObjectId("6601fb0f83ffc5f51ba22b88"))),
                set("item_inventory_id", UUID.fromString("1eeeaf80-e333-6613-b42f-f739b95106e6")));
    }
}
