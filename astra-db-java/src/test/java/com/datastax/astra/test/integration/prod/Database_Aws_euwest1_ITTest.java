package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Integration tests against a Local Instance of Stargate.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
class Database_Aws_euwest1_ITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initializeDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "eu-west-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-2");
    }

}
