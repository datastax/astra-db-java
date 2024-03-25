package com.datastax.astra.integration.collection;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

/**
 * Allow to test Collection information.
 */
class AstraProdCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "eu-west-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-2");
    }


}
