package com.datastax.astra.integration.docker;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.integration.AbstractDatabaseITTest;

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
