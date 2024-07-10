package com.datastax.astra.test.integration.prod;

import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "GITHUB_ACTION", matches = "true")
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION", matches = ".*")
public class AstraProdDatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    public AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.DEV;
    }

    @Override
    public CloudProviderType getCloudProvider() {
        return CloudProviderType.valueOf(System.getenv("ASTRA_CLOUD_PROVIDER"));
    }

    @Override
    public String getRegion() {
        return System.getenv("ASTRA_CLOUD_REGION");
    }
}
