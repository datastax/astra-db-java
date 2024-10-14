package com.datastax.astra.samples;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.collections.Document;
import com.datastax.astra.tool.loader.csv.CsvLoader;
import com.datastax.astra.tool.loader.csv.CsvRowMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Load a CSV to Astra
 */
@Slf4j
public class CsvPhilosophers {

    private static final String ASTRA_TOKEN  = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    private static final String API_ENDPOINT = "https://ba006059-a932-405c-857d-50921694078a-us-east1.apps.astra.datastax.com";

    public static void main(String[] args) throws Exception {
        // Get an empty Collection
        DataAPIClient        client = new DataAPIClient(ASTRA_TOKEN);
        Database             db = client.getDatabase(API_ENDPOINT);
        Collection<Document> collection = db.getCollection("openai_clun");

        collection.deleteAll();

        // Zou !
        String csvFilename = "/Users/cedricklunven/dev/datastax/JAVA/astra-db-java/tools/src/test/resources/philosopher-quotes.csv";
        CsvLoader.load(csvFilename, collection, new CsvRowMapper() {
            @Override
            public Document map(Document csvRow) {
                // Tags should be an Array
                csvRow.vectorize(csvRow.getString("quote"));
                csvRow.append("tags",csvRow.getString("tags").split(";"));
                return csvRow;
            }
        });
        Thread.sleep(10000);

    }

}
