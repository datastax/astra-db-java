package com.datastax.astra.integration;

import com.datastax.astra.client.DataApiClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.observer.LoggerCommandObserver;

/**
 * Allow to test Collection information.
 */
class DockerCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        Database db = DataApiClients.localStargate().getDatabase("http://localhost:8181", NAMESPACE_NS1);
        db.registerListener("logger", new LoggerCommandObserver(Database.class));
        db.getDatabaseAdmin().createNamespace(NAMESPACE_NS1);
        return db;
    }

}
