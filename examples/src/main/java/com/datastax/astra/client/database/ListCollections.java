package com.datastax.astra.client.database;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionInfo;

import java.util.stream.Stream;

public class ListCollections {
    public static void main(String[] args) {
        Database db = new Database("TOKEN", "API_ENDPOINT");

        // Get collection Names
        Stream<String> collectionNames = db.listCollectionNames();

        // Get Collection information (with options)
        Stream<CollectionInfo> collections = db.listCollections();
        collections.map(CollectionInfo::getOptions).forEach(System.out::println);
    }
}
