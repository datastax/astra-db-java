package com.datastax.astra.samples;

import com.datastax.astra.client.collections.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.collections.CollectionDefinition;
import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.vector.SimilarityMetric;
import com.datastax.astra.tool.loader.csv.CsvLoader;
import com.datastax.astra.tool.loader.csv.CsvRowMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Load a CSV to Astra
 */
@Slf4j
public class CsvLoaderWiki {

    private static final String ASTRA_TOKEN  = "<CHANGE_ME>";
    private static final String API_ENDPOINT = "<CHANGE_ME>";
    private static final String CSV_FILE     = "<CHANGE_ME>";

    public static void main(String[] args) throws Exception {
        // Get an empty Collection
        DataAPIClient client = new DataAPIClient(ASTRA_TOKEN);
        Database wikiDataDb  = client.getDatabase(API_ENDPOINT);

        Collection<Document> wiki = wikiDataDb.createCollection(
                "wiki",
                // Create collection with a Service in vectorize
                CollectionDefinition.builder()
                        .vectorDimension(768) // found from the CSV
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .build());

        // Optionally delete all documents
        //wiki.deleteAll();

        CsvLoader.load(CSV_FILE, wiki, new CsvRowMapper() {
            int line = 0;
            @Override
            public Document map(Document doc) {
                line++;
                if (line % 1000 == 0) {
                   log.info("{} lines have been submitted ",  line);
                }
                doc.put("_id", doc.get("", String.class));
                doc.remove("");
                String embedding = doc.get("embedding", String.class);
                embedding = embedding.replaceAll("\\[", "");
                embedding = embedding.replaceAll("\\]", "");
                embedding = embedding.replaceAll("\\\n", "");
                String[] parts = embedding.trim().split("\\s+");
                float[] floatArray = new float[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    floatArray[i] = Float.parseFloat(parts[i]);
                }
                doc.put("$vector", floatArray);
                doc.remove("embedding");
                return doc;
            }
        });

    }

}
