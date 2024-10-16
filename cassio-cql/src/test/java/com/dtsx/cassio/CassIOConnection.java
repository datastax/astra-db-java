package com.dtsx.cassio;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.UUID;

public class CassIOConnection {

    @Test
    @EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
    public void testConnectCassandra() {
        String astratoken = System.getenv("ASTRA_DB_APPLICATION_TOKEN");
        UUID databaseId = UUID.fromString("825bee35-f395-41d7-8683-93e6bb1f6381");
        String region = "us-east-2";
        String keyspace = "default_keyspace";

        try (CqlSession cqlSession = CassIO.init(astratoken, databaseId, region, keyspace)) {
            System.out.println("DataCenter: " + cqlSession
                    .execute("SELECT data_center FROM system.local;")
                    .one()
                    .get("data_center", String.class));
        }
    }

}
