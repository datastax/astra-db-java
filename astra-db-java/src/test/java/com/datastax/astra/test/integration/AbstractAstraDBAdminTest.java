package com.datastax.astra.test.integration;

import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractAstraDBAdminTest extends AbstractDataAPITest {

    protected static com.dtsx.astra.sdk.db.domain.Database devopsDb;

    @Test
    @Order(1)
    public void should_create_database() {
        Database db = getDatabase();
        devopsDb = ((AstraDBDatabaseAdmin)db.getDatabaseAdmin()).getDatabaseInformations();
        assertThat(devopsDb).isNotNull();
        assertThat(devopsDb.getInfo()).isNotNull();
        assertThat(getAstraDBAdmin().listDatabaseNames()).contains(devopsDb.getInfo().getName());
    }

}
