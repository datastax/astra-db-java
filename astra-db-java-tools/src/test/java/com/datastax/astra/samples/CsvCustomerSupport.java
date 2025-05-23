package com.datastax.astra.samples;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.collections.definition.documents.Document;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.tool.loader.csv.CsvLoader;
import com.datastax.astra.tool.loader.csv.CsvRowMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Load a CSV to Astra
 */
@Slf4j
public class CsvCustomerSupport {

    private static final String ASTRA_TOKEN  = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
    private static final String API_ENDPOINT = "https://e0ac686e-7942-4b25-832a-664ede8b1da0-us-east-2.apps.astra.datastax.com";

    public static void main(String[] args) throws Exception {
        // Get an empty Collection
        DataAPIClient        client = new DataAPIClient(ASTRA_TOKEN);
        Database             db = client.getDatabase(API_ENDPOINT);
        Collection<Document> collection = db.getCollection("customer_support");

        collection.deleteAll();

        // Zou !
        String csvFilename = "/Users/cedricklunven/dev/datastax/JAVA/astra-db-java/tools/src/test/resources/customer_support_tickets.csv";
        CsvLoader.load(csvFilename, collection, new CsvRowMapper() {
            @Override
            public Document map(Document csvRow) {
                csvRow.vectorize(csvRow.getString("ticket_description"));
                return csvRow;
            }
        });
        Thread.sleep(10000);

    }

}
