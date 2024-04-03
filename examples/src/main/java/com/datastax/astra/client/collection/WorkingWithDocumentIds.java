package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.ObjectId;
import com.datastax.astra.client.model.UUIDv6;
import com.datastax.astra.client.model.UUIDv7;

import java.time.Instant;
import java.util.UUID;

import static com.datastax.astra.client.model.Filters.eq;
import static com.datastax.astra.client.model.Updates.set;

public class WorkingWithDocumentIds {
    public static void main(String[] args) {
        // Given an existing collection
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Ids can be different Json scalar
        // ('defaultId' options NOT set for collection)
        new Document().id("abc");
        new Document().id(123);
        new Document().id(Instant.now());

        // Working with UUIDv4
        new Document().id(UUID.randomUUID());

        // Working with UUIDv6
        collection.insertOne(new Document().id(new UUIDv6()).append("tag", "new_id_v_6"));
        UUID uuidv4 = UUID.fromString("018e77bc-648d-8795-a0e2-1cad0fdd53f5");
        collection.insertOne(new Document().id(new UUIDv6(uuidv4)).append("tag", "id_v_8"));

        // Working with UUIDv7
        collection.insertOne(new Document().id(new UUIDv7()).append("tag", "new_id_v_7"));

        // Working with ObjectIds
        collection.insertOne(new Document().id(new ObjectId()).append("tag", "obj_id"));
        collection.insertOne(new Document().id(new ObjectId("6601fb0f83ffc5f51ba22b88")).append("tag", "obj_id"));

        collection.findOneAndUpdate(
                eq((new ObjectId("6601fb0f83ffc5f51ba22b88"))),
                set("item_inventory_id", UUID.fromString("1eeeaf80-e333-6613-b42f-f739b95106e6")));
    }
}
