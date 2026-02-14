package com.datastax.astra.test.integration.astra;

import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.admin.commands.AstraAvailableRegionInfo;
import com.datastax.astra.client.admin.options.AstraFindAvailableRegionsOptions;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.test.integration.AbstractDataAPITest;
import com.datastax.astra.test.integration.utils.EnabledIfAstra;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfAstra
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Astra / AstraDBAdmin")
public class Astra_01_AstraDBAdminIT extends AbstractDataAPITest {

    protected static com.dtsx.astra.sdk.db.domain.Database devopsDb;

    static final String TMP_DB_NAME    = "tmp_devops_db";
    static final String TMP_DB_REGION  = "us-east-2";
    static final CloudProviderType TMP_DB_PROVIDER = CloudProviderType.AWS;

    @Test
    @Order(1)
    public void should_create_database() {
        Database db = getAstraDBAdmin()
                .createDatabase(TMP_DB_NAME, TMP_DB_PROVIDER, TMP_DB_REGION)
                .getDatabase();
        devopsDb = ((AstraDBDatabaseAdmin)db.getDatabaseAdmin()).getDatabaseInformations();
        assertThat(devopsDb).isNotNull();
        assertThat(devopsDb.getInfo()).isNotNull();
        assertThat(devopsDb.getInfo().getName()).isEqualTo(TMP_DB_NAME);
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
