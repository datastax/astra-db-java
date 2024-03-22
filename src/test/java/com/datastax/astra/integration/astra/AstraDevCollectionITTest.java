package com.datastax.astra.integration.astra;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.internal.observer.LoggingCommandObserver;
import com.datastax.astra.integration.AbstractCollectionITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import static com.datastax.astra.AstraDBTestSupport.initializeDb;

public class AstraDevCollectionITTest extends AbstractCollectionITTest {

    @Override
    public Database initDatabase() {
        Database db = initializeDb(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        db.registerListener("logger", new LoggingCommandObserver(Database.class));
        return db;
    }

}
