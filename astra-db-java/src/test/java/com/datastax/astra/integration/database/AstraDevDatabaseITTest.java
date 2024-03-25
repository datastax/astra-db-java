package com.datastax.astra.integration.database;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class AstraDevDatabaseITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initAstraDatabase(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        // return initAstraDatabase(AstraEnvironment.TEST, CloudProviderType.GCP, "us-central1");
        // return initAstraDatabase(AstraEnvironment.TEST, CloudProviderType.AWS, "us-west-2");
    }

}
