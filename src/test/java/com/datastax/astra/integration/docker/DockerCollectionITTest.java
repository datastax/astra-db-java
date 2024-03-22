package com.datastax.astra.integration.docker;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.integration.AbstractCollectionITTest;
import com.datastax.astra.internal.types.ObjectId;
import org.junit.jupiter.api.Test;

import static com.datastax.astra.client.model.collections.CollectionIdTypes.objectId;
import static com.datastax.astra.client.model.find.SimilarityMetric.cosine;

/**
 * Allow to test Collection information.
 */
class DockerCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return DataAPIClients.localDatabase();
    }



}
