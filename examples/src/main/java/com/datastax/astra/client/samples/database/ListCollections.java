package com.datastax.astra.client.samples.database;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionDefinition;

import java.util.stream.Stream;

public class ListCollections {
    public static void main(String[] args) {
        Database db = new Database("TOKEN", "API_ENDPOINT");

        // Get collection Names
        Stream<String> collectionNames = db.listCollectionNames();

        // Get Collection information (with options)
        Stream<CollectionDefinition> collections = db.listCollections();
        collections.map(CollectionDefinition::getOptions)
                   .forEach(System.out::println);

    }
}
