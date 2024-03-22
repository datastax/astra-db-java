package com.datastax.astra.integration.docker;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.collections.CollectionOptions;
import com.datastax.astra.client.model.insert.InsertOneResult;
import com.datastax.astra.integration.AbstractDatabaseITTest;
import com.datastax.astra.internal.types.ObjectId;
import com.datastax.astra.internal.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.datastax.astra.client.model.collections.CollectionIdTypes.objectId;
import static com.datastax.astra.client.model.find.SimilarityMetric.cosine;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class DockerDatabaseITTest extends AbstractDatabaseITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return DataAPIClients.localDatabase();
    }

}
