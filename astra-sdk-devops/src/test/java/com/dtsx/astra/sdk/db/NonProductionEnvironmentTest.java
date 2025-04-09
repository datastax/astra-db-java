package com.dtsx.astra.sdk.db;

import com.dtsx.astra.sdk.AbstractDevopsApiTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseInfo;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class NonProductionEnvironmentTest extends AbstractDevopsApiTest {

    static String tokenDev = System.getenv("ASTRA_DB_APPLICATION_TOKEN_DEV");
    static String tokenTest = System.getenv("ASTRA_DB_APPLICATION_TOKEN_TEST");

    @Test
    public void shouldListDatabasesDev() {
        AstraDBOpsClient opsClient = new AstraDBOpsClient(tokenTest, AstraEnvironment.DEV);
        opsClient.findAllNonTerminated().map(Database::getInfo).map(DatabaseInfo::getName).forEach(System.out::println);
        //opsClient.databaseByName("sdk_java_test_vector").accessLists();

        // Create Db in dev
        String dbId = opsClient.create(DatabaseCreationRequest
                .builder()
                .name(SDK_TEST_DB_VECTOR_NAME)
                .keyspace(SDK_TEST_KEYSPACE)
                .cloudProvider(CloudProviderType.AWS)
                .cloudRegion("us-west-2")
                .withVector()
                .build());
        //TestUtils.waitForDbStatus(getDatabasesClient().database(dbId), DatabaseStatusType.ACTIVE, 500);
    }
}
