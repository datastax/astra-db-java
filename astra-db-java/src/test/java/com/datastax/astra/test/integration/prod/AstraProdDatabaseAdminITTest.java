package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public class AstraProdDatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    protected DatabaseAdmin initDatabaseAdmin() {
        return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "eu-west-1").getDatabaseAdmin();
    }

}
