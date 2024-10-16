package com.dtsx.cassio;

import com.datastax.oss.driver.api.core.CqlSession;
import com.dtsx.astra.sdk.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.dtsx.cassio.AbstractCassandraTable.PARTITION_ID;

public class CassIOClusteredMetadataVectorTable {

    @Test
    @EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
    public void should_create_table() {

            // Create db if not exists
            UUID databaseId = UUID.fromString("825bee35-f395-41d7-8683-93e6bb1f6381");

            // Initializing CqlSession
            try (CqlSession cqlSession = CassIO.init(System.getenv("ASTRA_DB_APPLICATION_TOKEN"),
                    databaseId, "us-east-2", "default_keyspace")) {

                // Initializing table with the dimension
                ClusteredMetadataVectorTable vector_Store = CassIO
                        .clusteredMetadataVectorTable("vector_store", 1536);
                vector_Store.create();

                // Insert Vectors
                String partitionId = UUID.randomUUID().toString();
                ClusteredMetadataVectorRecord record = new ClusteredMetadataVectorRecord();
                record.setVector(List.of(0.1f, 0.2f, 0.3f, 0.4f));
                record.setMetadata(Map.of("key", "value"));
                record.setPartitionId(partitionId);
                record.setBody("Sample text fragment");
                record.setAttributes("handy field for special attributes");
                vector_Store.put(record);

                // Semantic Search
                AnnQuery query = AnnQuery
                        .builder()
                        .embeddings(List.of(0.1f, 0.2f, 0.3f, 0.4f))
                        .metaData(Map.of(PARTITION_ID, partitionId))
                        .build();

                vector_Store.similaritySearch(query).forEach(result -> {
                    System.out.println("Similarity : " + result.getSimilarity());
                    System.out.println("Record : " + result.getEmbedded().getBody());
                });
            }
    }
}
