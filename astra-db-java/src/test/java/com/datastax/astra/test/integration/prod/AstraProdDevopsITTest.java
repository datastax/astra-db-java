package com.datastax.astra.test.integration.prod;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBDatabaseAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.DatabaseInfo;
import com.datastax.astra.test.integration.AbstractAstraDBAdminTest;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AstraProdDevopsITTest extends AbstractAstraDBAdminTest {

    private static final String TMP_VECTOR_DB = "astra_db_admin_test";
    private static final String TMP_VECTOR_DB2 = "astra_db_admin_test2";

    @Override
    protected AstraEnvironment getAstraEnvironment() {
        return AstraEnvironment.PROD;
    }

    @Override
    protected CloudProviderType getCloudProvider() {
        return null;
    }

    @Override
    protected String getRegion() {
        return "";
    }

    @Test
    @Order(1)
    void shouldConnectToDatabase() {
        // Given
        Assertions.assertNotNull(getAstraDbAdmin());

        // Create a DB
        Assertions.assertFalse(getAstraDbAdmin().databaseExists(TMP_VECTOR_DB));
        DatabaseAdmin dbAdmin = getAstraDbAdmin()
                .createDatabase(TMP_VECTOR_DB, CloudProviderType.GCP, "us-east1");

        // List DBs
        assertThat(getAstraDbAdmin().listDatabaseNames()).contains(TMP_VECTOR_DB);
        assertThat(getAstraDbAdmin().databaseExists(TMP_VECTOR_DB)).isTrue();

        Optional<DatabaseInfo> info = getAstraDbAdmin().listDatabases()
                .stream().filter(db -> db.getName().equals(TMP_VECTOR_DB)).findFirst();

        // Access DB
        assertThat(info).isPresent();
        assertThat(info.get().getName()).isEqualTo(TMP_VECTOR_DB);
        UUID databaseId = info.get().getId();
        Database db = getAstraDbAdmin().getDatabase(databaseId);
        Assertions.assertNotNull(db);
        assertThat(getAstraDbAdmin().databaseExists(databaseId)).isTrue();

        // Delete DB
        getAstraDbAdmin().dropDatabase(databaseId);
    }

    @Test
    @Order(2)
    void shouldCreateOtherDatabase() {
        Assertions.assertFalse(getAstraDbAdmin().databaseExists(TMP_VECTOR_DB2));
        DatabaseAdmin dbAdmin = getAstraDbAdmin().createDatabase(TMP_VECTOR_DB2);
        AstraDBDatabaseAdmin dbAmin2 = (AstraDBDatabaseAdmin) dbAdmin;
        UUID databaseId = UUID.fromString(dbAmin2.getDatabaseInformations().getId());

        Optional<String> oToken = Utils.readEnvVariable("ASTRA_DB_APPLICATION_TOKEN");
        assertThat(oToken).isPresent();
        DataAPIClient client = new DataAPIClient(oToken.get());
        assertThat(client.getDatabase(databaseId)).isNotNull();
        assertThat(client.getDatabase(databaseId, "default_keyspace")).isNotNull();

        Assertions.assertTrue(getAstraDbAdmin().databaseExists(TMP_VECTOR_DB2));
        getAstraDbAdmin().dropDatabase(TMP_VECTOR_DB2);
    }



}