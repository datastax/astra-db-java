package com.datastax.astra.integration;

import com.datastax.astra.devops.utils.AstraEnvironment;
import io.stargate.sdk.data.client.DataApiCollection;
import io.stargate.sdk.data.client.DataApiNamespace;
import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.collections.CollectionOptions;
import io.stargate.sdk.data.test.integration.AbstractCollectionITTest;
import io.stargate.sdk.types.ObjectId;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.AstraDBTestSupport.createDatabase;
import static io.stargate.sdk.data.client.model.SimilarityMetric.cosine;
import static io.stargate.sdk.data.client.model.collections.CollectionIdTypes.objectId;

public class AstraCollectionITTest extends AbstractCollectionITTest {

    @Override
    public DataApiNamespace initNamespace() {
        return createDatabase(AstraEnvironment.TEST);
    }

    @Test
    public void testCollectionWithObjectId() {
        DataApiCollection<Document> collectionObjectId = getDataApiNamespace()
                .createCollection("collection_objectid", CollectionOptions
                        .builder()
                        .withDefaultId(objectId)
                        .withVectorDimension(14)
                        .withVectorSimilarityMetric(cosine)
                        .build());

        Document doc1 = new Document().id(new ObjectId());

    }

    @Test
    public void testCollectionWithUUID() {}

    @Test
    public void testCollectionWithUUIDv6() {}

    @Test
    public void testCollectionWithUUIDv7() {}

    @Test
    public void testCollectionWithVectorize() {}


}
