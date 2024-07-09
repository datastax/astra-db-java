package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;

public abstract class AbstractAstraDBAdminTest {


    /**
     * Access AstraDBAdmin for different environment (to create DB).
     *
     * @param env
     *      astra environment
     * @return
     *      instance of AstraDBAdmin
     */
    private AstraDBAdmin getAstraDBClient(AstraEnvironment env) {
        switch (env) {
            case DEV:
                return DataAPIClients.createForAstraDev(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_DEV")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_DEV'")))
                        .getAdmin();
            case PROD:
                return DataAPIClients.create(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN'")))
                        .getAdmin();
            case TEST:
                return DataAPIClients.createForAstraTest(Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN_TEST")
                                .orElseThrow(() -> new IllegalStateException("Please define env variable 'ASTRA_DB_APPLICATION_TOKEN_TEST'")))
                        .getAdmin();
            default:
                throw new IllegalArgumentException("Invalid Environment");
        }
    }

    protected AstraDBAdmin astraDbAdmin;

    protected AstraDBAdmin getAstraDbAdmin() {
        if (astraDbAdmin == null) {
            astraDbAdmin = getAstraDBClient(pickAstraEnvironment());
        }
        return astraDbAdmin;
    }

    protected abstract AstraEnvironment pickAstraEnvironment();

}
