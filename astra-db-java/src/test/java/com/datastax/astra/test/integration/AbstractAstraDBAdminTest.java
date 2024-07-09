package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.DatabaseInfo;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractAstraDBAdminTest implements TestDataSet {

    protected AstraDBAdmin astraDbAdmin;

    protected AstraDBAdmin getAstraDbAdmin() {
        if (astraDbAdmin == null) {
            astraDbAdmin = getAstraDBAdmin(getAstraEnvironment());
        }
        return astraDbAdmin;
    }

    protected abstract AstraEnvironment getAstraEnvironment();
    protected abstract CloudProviderType getCloudProvider();
    protected abstract String getRegion();

    protected static com.dtsx.astra.sdk.db.domain.Database devopsDb;


    @Test
    @Order(1)
    public void should_create_database() {
        Database db = initializeDatabase(getAstraEnvironment(), getCloudProvider(), getRegion());
        devopsDb = ((AstraDBDatabaseAdmin)db.getDatabaseAdmin()).getDatabaseInformations();
        assertThat(devopsDb).isNotNull();
        assertThat(devopsDb.getInfo()).isNotNull();
        assertThat(getAstraDbAdmin().databaseExists(devopsDb.getId())).isTrue();
        assertThat(getAstraDbAdmin().databaseExists(devopsDb.getInfo().getName())).isTrue();
        assertThat(getAstraDbAdmin().listDatabaseNames()).contains(devopsDb.getInfo().getName());
    }


}
