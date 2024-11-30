package com.datastax.astra.client.database;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.CollectionDescriptor;
import com.datastax.astra.client.databases.Database;

import java.util.List;
import java.util.stream.Stream;

public class ListCollections {
    public static void main(String[] args) {
        Database db = new DataAPIClient("TOKEN")
                .getDatabase("API_ENDPOINT");

        // Get collection Names
        List<String> collectionNames = db.listCollectionNames();

        // Get Collection information (with options)
        List<CollectionDescriptor> collections = db.listCollections();
        collections.stream().map(CollectionDescriptor::getOptions).forEach(System.out::println);
    }
}
