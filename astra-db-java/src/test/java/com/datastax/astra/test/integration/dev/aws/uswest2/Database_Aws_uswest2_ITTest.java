package com.datastax.astra.test.integration.dev.aws.uswest2;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Integration tests against a Local Instance of Stargate.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER", matches = "AWS")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION", matches = "us-west-2")
class Database_Aws_uswest2_ITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initializeDatabase(AstraEnvironment.DEV, CloudProviderType.AWS, "us-west-2");
    }

}
