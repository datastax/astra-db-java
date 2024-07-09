package com.datastax.astra.samples;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.tool.loader.csv.CsvLoader;
import com.datastax.astra.tool.loader.csv.CsvRowMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Load a CSV to Astra
 */
@Slf4j
public class CsvLoaderListing {

    private static final String ASTRA_TOKEN  = "<replace_me>";
    private static final String API_ENDPOINT = "<replace_me>";

    public static void main(String[] args) throws Exception {
        // Get an empty Collection
        DataAPIClient        client = new DataAPIClient(ASTRA_TOKEN);
        Database             db = client.getDatabase(API_ENDPOINT);
        Collection<Document> collection = db.createCollection("airbnb");

        db.getCollection("airbnb").deleteAll();
        collection.registerListener("logger", new LoggingCommandObserver(CsvLoaderListing.class));
        collection.deleteAll();

        // Zou !
        String csvFilename = "/Users/cedricklunven/Downloads/listings.csv";
        CsvLoader.load(csvFilename, collection, new CsvRowMapper() {
            @Override
            public Document map(Document doc) {
                doc.append("_id", doc.get("id")).remove("id"); // rename field id to _id
                return doc;
            }
        });

    }

}
