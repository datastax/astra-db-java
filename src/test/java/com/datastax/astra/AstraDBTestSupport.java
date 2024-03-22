package com.datastax.astra;


import com.datastax.astra.client.AstraDBAdmin;
import com.datastax.astra.client.DataApiClients;
import com.datastax.astra.client.Database;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * This class will help us generate database or select the environment
 * we are targeting.
 */
@Slf4j
public class AstraDBTestSupport {

    /**
     * Test Constants
     */
    public static final String DATABASE_NAME = "astra_db_client";

    public static AstraDBAdmin getAstraDBClient(AstraEnvironment env) {
        switch (env) {
            case DEV:
                return DataApiClients.astraDev(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_DEV'")))
                        .getAdmin();
            case PROD:
                return DataApiClients.astra(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN'")))
                        .getAdmin();
            case TEST:
                return DataApiClients.astraTest(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_TEST")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_TEST'")))
                        .getAdmin();
            default:
                throw new IllegalArgumentException("Invalid Environment");
         }
    }

    public static Database initializeDb(AstraEnvironment env, CloudProviderType cloud, String region) {
        log.info("Working in environment '{}'", env.name());
        AstraDBAdmin client = getAstraDBClient(env);
        UUID databaseId =  client.createDatabase(DATABASE_NAME, cloud, region);
        log.info("Working with api Endpoint '{}'", ApiLocator.getApiJsonEndpoint(env, databaseId.toString(), region));
        return client.getDatabase(databaseId);
    }


}
