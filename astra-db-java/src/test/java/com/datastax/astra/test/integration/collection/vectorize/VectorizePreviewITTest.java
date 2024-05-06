package com.datastax.astra.test.integration.collection.vectorize;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.CollectionOptions;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.InsertOneResult;
import com.datastax.astra.client.model.SimilarityMetric;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.test.integration.collection.AbstractCollectionITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.astra.client.model.FindOptions.Builder.sort;
import static com.dtsx.astra.sdk.utils.Utils.readEnvVariable;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class is used to test the vectorize feature. It is in a dedicated clas
 * to target a specific environment and disable if not available
 */
class VectorizePreviewITTest {

    static final String COLLECTION_VECTORIZE = "collection_vectorize";
    static final String NVIDIA_PROVIDER = "nvidia";
    static final String NVIDIA_MODEL = "NV-Embed-QA";
    static final Integer NVIDIA_DIMENSION = 1024;
    private static Database db;
    private static Collection<Document> collectionVectorize;

    /** {@inheritDoc} */
    @BeforeAll
    protected static void initDatabase() {
        DataAPIClient client = DataAPIClients
                .createForAstraDev(readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV").get());
        db = client.getAdmin()
                .createDatabase("astra_db_client",
                        CloudProviderType.AWS, "us-west-2")
                .getDatabase();
        collectionVectorize = db.getCollection(COLLECTION_VECTORIZE);
    }

    @Test
    void shouldCreateACollectionWithNvidia() {
        Collection<Document> collection = db.createCollection(COLLECTION_VECTORIZE,
                CollectionOptions.builder()
                        .vectorDimension(NVIDIA_DIMENSION)
                        .vectorSimilarity(SimilarityMetric.COSINE)
                        .vectorize(NVIDIA_PROVIDER, NVIDIA_MODEL)
                        .build());
        assertThat(collection).isNotNull();
        assertThat(db.listCollectionNames().collect(Collectors.toList())).contains(COLLECTION_VECTORIZE);
    }

    @Test
    void shouldInsertOneDocumentWithVectorize() {
        collectionVectorize.deleteAll();
        Document document = new Document()
                .append("name", "cedrick")
                .vectorize("Life is too short for Javascript");
        InsertOneResult res = collectionVectorize.insertOne(document);
        assertThat(res).isNotNull();
        assertThat(res.getInsertedId()).isNotNull();
    }

    @Test
    void testFindVectorize() {
        List<Document> doclist = collectionVectorize.find(
                        // FindOptions.Builder.sort(...), some options
                        sort("Life is too short for Javascript"))
                .all();
        System.out.println(doclist);
        assertThat(doclist).isNotNull().isNotEmpty();
    }
}
