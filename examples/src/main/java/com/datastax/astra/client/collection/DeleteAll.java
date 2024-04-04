package com.datastax.astra.client.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.model.DeleteResult;
import com.datastax.astra.client.model.Document;

public class DeleteAll {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Show the deleted count
        DeleteResult result = collection.deleteAll();
        System.out.println("Deleted Count:" + result.getDeletedCount());
    }
}
