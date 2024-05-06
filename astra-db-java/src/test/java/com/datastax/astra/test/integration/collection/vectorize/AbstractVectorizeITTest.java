package com.datastax.astra.test.integration.collection.vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.SimilarityMetric;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
abstract class AbstractVectorizeITTest {

    /**
     * Reference to working DataApiNamespace
     */
    protected static Database database;

    /**
     * Initialization of the DataApiNamespace.
     *
     * @return
     *      the instance of Data ApiNamespace
     */
    protected abstract Database initDatabase();

    /**
     * Initialization of the working Namespace.
     *
     * @return
     *      current Namespace
     */
    protected Database getDatabase() {
        if (database == null) {
            database = initDatabase();
        }
        return database;
    }

    protected void dropCollection(String name) {
        getDatabase().dropCollection(name);
        log.info("Collection {} dropped", name);
    }

    protected void dropAllCollections() {
        getDatabase().listCollections().forEach(collection -> {
            getDatabase().dropCollection(collection.getName());
            log.info("Collection {} dropped", collection.getName());
        });
    }
    protected void dropCollection(EmbeddingModel model) {
        getDatabase().dropCollection(model.name().toLowerCase());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Error while waiting for the drop", e);
        }
        log.info("Collection {} dropped", model.name().toLowerCase());
    }

    protected Collection<Document> createCollectionVertex(EmbeddingModel model, String projectID) {

        getDatabase().createCollection("collection",
                // Create Collection Payload
                CollectionOptions.builder()
                .vectorDimension(model.getDimension())
                .vectorSimilarity(SimilarityMetric.COSINE)
                .vectorize(model.getProvider(), model.getName(), Map.of("PROJECT_ID", projectID))
                .build(),
                // Will be use to shape all commands
                new CommandOptions<>().embeddingAPIKey("aaa")
        );
        return getDatabase().createCollection(model.name().toLowerCase(),
                CollectionOptions.builder()
                        .vectorDimension(model.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(model.getProvider(), model.getName(), Map.of("PROJECT_ID", projectID))
                        .build());
    }



}
