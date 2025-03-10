package com.dtsx.cassio;

import com.datastax.oss.driver.api.core.CqlSession;
import com.dtsx.astra.sdk.utils.TestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CassIOMetadataVectorTable {

    public static void main(String[] args) {

        // Create db if not exists
        UUID databaseId = UUID.randomUUID();

        // Initializing CqlSession
        try (CqlSession cqlSession = CassIO.init("TOKEN",
                databaseId, TestUtils.TEST_REGION,
                "default_keyspace")) {

            // Initializing table with the dimension
            MetadataVectorTable vector_Store = CassIO.metadataVectorTable("vector_store", 1536);
            vector_Store.create();

            // Insert Vectors
            String partitionId = UUID.randomUUID().toString();
            MetadataVectorRecord record = new MetadataVectorRecord();
            record.setVector(List.of(0.1f, 0.2f, 0.3f, 0.4f));
            record.setMetadata(Map.of("key", "value"));
            record.setBody("Sample text fragment");
            record.setAttributes("handy field for special attributes");
            vector_Store.put(record);

            // Semantic Search
            AnnQuery query = AnnQuery
                    .builder()
                    .embeddings(List.of(0.1f, 0.2f, 0.3f, 0.4f))
                    .metaData(Map.of("key", "value"))
                    .build();

            vector_Store.similaritySearch(query).forEach(result -> {
                System.out.println("Similarity : " + result.getSimilarity());
                System.out.println("Record : " + result.getEmbedded().getBody());
            });
        }
    }
}
