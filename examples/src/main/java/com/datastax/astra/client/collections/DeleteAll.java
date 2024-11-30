package com.datastax.astra.client.collections;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.commands.results.CollectionDeleteResult;
import com.datastax.astra.client.collections.definition.documents.Document;

public class DeleteAll {
    public static void main(String[] args) {
        Collection<Document> collection = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT")
                .getCollection("COLLECTION_NAME");

        // Show the deleted count
        CollectionDeleteResult result = collection.deleteAll();
        System.out.println("Deleted Count:" + result.getDeletedCount());
    }
}
