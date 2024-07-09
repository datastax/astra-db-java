package com.datastax.astra.test.integration.test;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Integration tests against a Local Instance of Stargate.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
class Database_Azure_ITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initializeDatabase(AstraEnvironment.TEST, CloudProviderType.AZURE, "eastus");
    }

}
