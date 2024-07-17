package com.datastax.astra.test.integration.prod;

import com.datastax.astra.test.integration.AbstractDatabaseTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Integration tests against a Local Instance of Stargate.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION", matches = ".*")
class AstraProdDatabaseITTest extends AbstractDatabaseTest {

    @Override
    public AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.PROD;
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
