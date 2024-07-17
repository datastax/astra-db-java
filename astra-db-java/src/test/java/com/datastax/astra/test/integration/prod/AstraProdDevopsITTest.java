package com.datastax.astra.test.integration.prod;

import com.datastax.astra.test.integration.AbstractAstraDBAdminTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER", matches = ".*")
@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION", matches = ".*")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AstraProdDevopsITTest extends AbstractAstraDBAdminTest {

    @Override
    protected AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.PROD;
    }

    @Override
    protected CloudProviderType getCloudProvider() {
        return CloudProviderType.valueOf(System.getenv("ASTRA_CLOUD_PROVIDER"));
    }

    @Override
    protected String getRegion() {
        return System.getenv("ASTRA_CLOUD_REGION");
    }

}