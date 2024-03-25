package com.datastax.astra.integration.database;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class LocalDatabaseITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return DataAPIClients.localDatabase();
    }

}
