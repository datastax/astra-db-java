package com.datastax.astra.integration;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.internal.types.ObjectId;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.AstraDBTestSupport.initializeDb;
import static com.datastax.astra.client.model.collections.CollectionIdTypes.objectId;
import static com.datastax.astra.client.model.find.SimilarityMetric.cosine;

public class AstraCollectionITTest extends AbstractCollectionITTest {

    @Override
    public Database initDatabase() {
        return initializeDb(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        //return createDatabase(AstraEnvironment.DEV, CloudProviderType.AWS, "us-east2");
        //return createDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east2");
    }

    @Test
    public void testCollectionWithObjectId() {
        Collection<Document> collectionObjectId = getDataApiNamespace()
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
