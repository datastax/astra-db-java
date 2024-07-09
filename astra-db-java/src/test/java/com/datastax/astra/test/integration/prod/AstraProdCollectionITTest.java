package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.test.integration.AbstractCollectionITTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Allow to test Collection information.
 */
@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
class AstraProdCollectionITTest extends AbstractCollectionITTest {

    /** {@inheritDoc} */
    @Override
    protected Database initDatabase() {
        return initializeDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "eu-west-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-1");
        //return initAstraDatabase(AstraEnvironment.PROD, CloudProviderType.AWS, "us-east-2");
    }

    @Test
    void shouldGetClient() {
        Optional<String> oToken = Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN");
        assertThat(oToken).isPresent();
        DataAPIClient client = new DataAPIClient(oToken.get());
        assertThat(client).isNotNull();
    }


}
