package com.datastax.astra.integration.astra;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.observer.LoggerCommandObserver;
import com.datastax.astra.integration.AbstractDatabaseITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import static com.datastax.astra.AstraDBTestSupport.initializeDb;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class AstraDevDatabaseITTest extends AbstractDatabaseITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        Database db = initializeDb(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        db.registerListener("logger", new LoggerCommandObserver(Database.class));
        return db;
    }

}
