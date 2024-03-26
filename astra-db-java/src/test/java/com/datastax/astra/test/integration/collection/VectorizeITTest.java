package com.datastax.astra.test.integration.collection;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.LoggingCommandObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.astra.client.model.FindOptions.vectorize;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class is used to test the vectorize feature. It is in a dedicated clas
 * to target a specific environment and disable if not available
 */
@Disabled
class VectorizeITTest {

    static final String COLLECTION_VECTORIZE = "collection_vectorize";
    static final String NVIDIA_PROVIDER = "nvidia";
    static final String NVIDIA_MODEL = "NV-Embed-QA";
    static final Integer NVIDIA_DIMENSION= 1024;
    private static Database db;
    private static Collection<Document> collectionVectorize;

    @BeforeAll
    public static void setup() {
        // Dev KUBERNETES, aws, us-west-2
        db = DataAPIClients.astraDev("<redacted>").getDatabase(UUID.fromString("<redacted>"));
    }

    private Collection<Document> getCollectionVectorize() {
        if (collectionVectorize == null) {
            collectionVectorize = db.getCollection(COLLECTION_VECTORIZE);
            collectionVectorize.registerListener("logger", new LoggingCommandObserver(VectorizeITTest.class));
        }
        return collectionVectorize;
    }

    @Test
    public void shouldCreateACollectionWithNvidia() {
        db.registerListener("logger", new LoggingCommandObserver(VectorizeITTest.class));

        Collection<Document> collection = db.createCollection(COLLECTION_VECTORIZE, CollectionOptions.builder()
                .withVectorDimension(NVIDIA_DIMENSION)
                .withVectorSimilarityMetric(SimilarityMetric.cosine)
                .withVectorize(NVIDIA_PROVIDER, NVIDIA_MODEL)
                .build());
        assertThat(collection).isNotNull();
        assertThat(db.listCollectionNames().collect(Collectors.toList())).contains(COLLECTION_VECTORIZE);
    }

    @Test
    public void shouldInsertOneDocumentWithVectorize() {
        getCollectionVectorize().deleteAll();
        Document document = new Document()
                .append("name", "cedrick")
                .vectorize("Life is too short for Javascript");
        InsertOneResult res = getCollectionVectorize().insertOne(document);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isNotNull();
        System.out.println(res.getInsertedId());
    }

    @Test
    public void testFindVectorize() {
        getCollectionVectorize()
                .find(vectorize("Life is too short for Javascript"))
                .forEach(doc -> System.out.println(doc.toJson()));
    }

}
