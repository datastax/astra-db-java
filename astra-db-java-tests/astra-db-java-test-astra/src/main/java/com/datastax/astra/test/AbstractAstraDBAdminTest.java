package com.datastax.astra.test;

import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.admin.commands.AstraAvailableRegionInfo;
import com.datastax.astra.client.admin.options.AstraFindAvailableRegionsOptions;
import com.datastax.astra.client.databases.Database;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

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

    @Test
    @Order(2)
    public void should_list_database_names() {
        assertThat(getAstraDBAdmin().listDatabaseNames()).contains(devopsDb.getInfo().getName());
    }

    @Test
    @Order(3)
    public void should_list_database() {
        assertThat(getAstraDBAdmin().listDatabases()).isNotEmpty();
    }

    @Test
    @Order(4)
    public void should_database_exists() {
        getAstraDBAdmin().databaseExists(devopsDb.getInfo().getName());
    }

    @Test
    @Order(5)
    public void should_find_available_regions() {
        List<AstraAvailableRegionInfo> list = getAstraDBAdmin()
         .findAvailableRegions(new AstraFindAvailableRegionsOptions());
        assertThat(list).isNotEmpty();
        list.forEach(r -> log.info("Region : {} / {} / {}", r.getCloudProvider(), r.getName(), r.getClassification()));
    }

    @Test
    @Order(6)
    public void should_drop_database() {
        //getAstraDBAdmin().dropDatabase(devopsDb.getInfo().getName());
        //assertThat(getAstraDBAdmin().databaseExists(devopsDb.getInfo().getName())).isFalse();
    }

}
