package com.datastax.astra.test.integration.collection.vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
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

    protected Collection<Document> createCollection(EmbeddingModel model) {
        return getDatabase().createCollection(model.name().toLowerCase(),
                CollectionOptions.builder()
                        .vectorDimension(model.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(model.getProvider(), model.getName())
                        .build());
    }

    protected Collection<Document> createCollectionVertex(EmbeddingModel model, String projectID) {
        return getDatabase().createCollection(model.name().toLowerCase(),
                CollectionOptions.builder()
                        .vectorDimension(model.getDimension())
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(model.getProvider(), model.getName(), Map.of("PROJECT_ID", projectID))
                        .build());
    }



}
