package com.datastax.astra.test.integration.dev;

import com.datastax.astra.test.integration.AbstractCollectionITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Allow to test Collection information
 * AstraEnvironment.DEV, CloudProviderType.GCP, "europe-west4"
 * AstraEnvironment.DEV, CloudProviderType.GCP, "us-central1"
 */
//@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN_DEV", matches = "Astra.*")
//@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_PROVIDER_DEV", matches = ".*")
//@EnabledIfEnvironmentVariable(named = "ASTRA_CLOUD_REGION_DEV", matches = ".*")
class AstraDevCollectionITTest extends AbstractCollectionITTest {

    @Override
    public AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.DEV;
    }

    @Override
    public CloudProviderType getCloudProvider() {
        if (System.getenv("ASTRA_CLOUD_PROVIDER_DEV") == null) {
            return CloudProviderType.GCP;
        }
        return CloudProviderType.valueOf(System.getenv("ASTRA_CLOUD_PROVIDER_DEV"));
    }

    @Override
    public String getRegion() {
        if (System.getenv("ASTRA_CLOUD_REGION_DEV") == null) {
            return "europe-west4";
        }
        return System.getenv("ASTRA_CLOUD_REGION_DEV");
    }

}
