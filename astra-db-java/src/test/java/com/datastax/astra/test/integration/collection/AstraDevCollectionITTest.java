package com.datastax.astra.test.integration.collection;

import com.datastax.astra.client.Database;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

/**
 * Allow to test Collection information.
 */
class AstraDevCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initAstraDatabase(AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4");
        // return initAstraDatabase(AstraEnvironment.TEST, CloudProviderType.GCP, "us-central1");
        // return initAstraDatabase(AstraEnvironment.TEST, CloudProviderType.AWS, "us-west-2");
    }



}
