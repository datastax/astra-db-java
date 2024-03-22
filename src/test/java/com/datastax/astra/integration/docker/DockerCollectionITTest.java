package com.datastax.astra.integration.docker;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.integration.AbstractCollectionITTest;

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
