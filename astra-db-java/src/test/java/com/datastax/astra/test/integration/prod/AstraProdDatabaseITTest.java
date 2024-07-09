package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

/**
 * Integration tests against a Local Instance of Stargate.
 */
class AstraProdDatabaseITTest extends AbstractDatabaseTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "eu-west-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-2");
    }

}
