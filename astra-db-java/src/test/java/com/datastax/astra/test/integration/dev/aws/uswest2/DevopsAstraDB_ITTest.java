package com.datastax.astra.test.integration.dev.aws.uswest2;

import com.datastax.astra.test.integration.AbstractAstraDBAdminTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
public class DevopsAstraDB_ITTest extends AbstractAstraDBAdminTest {

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
