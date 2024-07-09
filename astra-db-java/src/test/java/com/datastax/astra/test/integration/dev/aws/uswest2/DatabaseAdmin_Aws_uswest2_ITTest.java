package com.datastax.astra.test.integration.dev.aws.uswest2;

import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER", matches = "AWS")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION", matches = "us-west-2")
public class DatabaseAdmin_Aws_uswest2_ITTest extends AbstractDatabaseAdminITTest {

    @Override
    protected AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.DEV;
    }

    @Override
    protected CloudProviderType getCloudProvider() {
        return CloudProviderType.AWS;
    }

    @Override
    protected String getRegion() {
        return "us-west-2";
    }

}
