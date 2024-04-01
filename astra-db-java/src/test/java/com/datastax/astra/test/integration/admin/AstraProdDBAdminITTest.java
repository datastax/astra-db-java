package com.datastax.astra.test.integration.admin;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.model.DatabaseInfo;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AstraProdDBAdminITTest extends AbstractAstraDBAdminTest {

    private static final String TMP_VECTOR_DB = "astra_db_admin_test";

    @Override
    protected AstraEnvironment pickAstraEnvironment() {
        return AstraEnvironment.PROD;
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
        boolean deleted = getAstraDbAdmin().dropDatabase(databaseId);

    }

}