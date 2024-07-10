package com.datastax.astra.test.integration.dev;

import com.datastax.astra.test.integration.AbstractDatabaseAdminITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER_DEV", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION_DEV", matches = ".*")
public class AstraDevDatabaseAdminITTest extends AbstractDatabaseAdminITTest {

    @Override
    public AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.DEV;
    }

    @Override
    public CloudProviderType getCloudProvider() {
        return CloudProviderType.valueOf(System.getenv("ASTRA_CLOUD_PROVIDER_DEV"));
    }

    @Override
    public String getRegion() {
        return System.getenv("ASTRA_CLOUD_REGION_DEV");
    }
}
