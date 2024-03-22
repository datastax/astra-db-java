package com.datastax.astra.integration.docker;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.observer.LoggerCommandObserver;
import com.datastax.astra.integration.AbstractDatabaseITTest;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class DockerDatabaseITTest extends AbstractDatabaseITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        Database db = DataAPIClients.localStargate().getDatabase("http://localhost:8181", NAMESPACE_NS1);
        db.registerListener("logger", new LoggerCommandObserver(Database.class));
        db.getDatabaseAdmin().createNamespace(NAMESPACE_NS1);
        return db;
    }

}
