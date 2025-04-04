package com.datastax.astra.test.integration;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIClients;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.databases.Database;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.utils.Utils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static com.datastax.astra.client.DataAPIClients.DEFAULT_ENDPOINT_LOCAL;
import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;

/**
 * Constants for the test suite
 */
@Slf4j
public abstract class AbstractDataAPITest {

    public static final String ENV_VAR_DESTINATION            = "ASTRA_DB_JAVA_TEST_ENV";
    public static final String ENV_VAR_ASTRA_TOKEN            = "ASTRA_DB_APPLICATION_TOKEN";
    public static final String ENV_VAR_CLOUD_PROVIDER         = "ASTRA_CLOUD_PROVIDER";
    public static final String ENV_VAR_CLOUD_REGION           = "ASTRA_CLOUD_REGION";

    public static final String ENV_VAR_DESTINATION_LOCAL      = "local";
    public static final String ENV_VAR_DESTINATION_ASTRA_DEV  = "astra_dev";
    public static final String ENV_VAR_DESTINATION_ASTRA_PROD = "astra_prod";
    public static final String ENV_VAR_DESTINATION_ASTRA_TEST = "astra_test";

    public String readEnvVariable(String key) {
        String envVar       = System.getenv(key);
        String systemEnvVar = System.getProperty(key);
        if (Utils.hasLength(envVar)) {
            return envVar;
        } else if (Utils.hasLength(systemEnvVar)) {
            return systemEnvVar;
        }
        return null;
    }

    protected static DataAPIClient dataApiClient;

    protected static AstraDBAdmin astraDbAdmin;

    protected static Database database;

    protected static DatabaseAdmin databaseAdmin;

    protected static DataAPIDestination destination;

    protected static EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    protected EmbeddingModel getAllMiniLmL6V2EmbeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        }
        return embeddingModel;
    }

    protected DataAPIDestination getDataApiDestination() {
        if (destination == null) {
            String targetEnv = readEnvVariable(ENV_VAR_DESTINATION);
            if (targetEnv == null) {
                throw new IllegalArgumentException("Environment variable '" + ENV_VAR_DESTINATION + "' is not set");
            }
            switch (targetEnv) {
                case ENV_VAR_DESTINATION_ASTRA_DEV :
                    destination = DataAPIDestination.ASTRA_DEV;
                break;
                case ENV_VAR_DESTINATION_ASTRA_PROD:
                    destination = DataAPIDestination.ASTRA;
                break;
                case ENV_VAR_DESTINATION_ASTRA_TEST:
                    destination = DataAPIDestination.ASTRA_TEST;
                break;
                case ENV_VAR_DESTINATION_LOCAL:
                    destination = DataAPIDestination.HCD;
                break;
                default:
                    throw new IllegalArgumentException("Unsupported value for environment variable '"
                        + ENV_VAR_DESTINATION + "' should be in " + List.of(ENV_VAR_DESTINATION_ASTRA_DEV,
                        ENV_VAR_DESTINATION_ASTRA_TEST,
                        ENV_VAR_DESTINATION_ASTRA_PROD,
                        ENV_VAR_DESTINATION_LOCAL));
            };
        }
        return destination;
    }

    protected CloudProviderType getCloudProvider() {
        String cloudProvider = readEnvVariable(ENV_VAR_CLOUD_PROVIDER);
        return cloudProvider == null ? null : CloudProviderType.valueOf(cloudProvider);
    }

    protected String getCloudRegion() {
        return System.getProperty(ENV_VAR_CLOUD_REGION);
    }

    protected DataAPIClient getDataApiClient() {
        String token = readEnvVariable(ENV_VAR_ASTRA_TOKEN);
        if (dataApiClient == null) {
            log.info("Initializing DataAPI client for {}", getDataApiDestination().name());
            switch (getDataApiDestination()) {
                case ASTRA:
                    dataApiClient = DataAPIClients.astra(token);
                break;
                case ASTRA_DEV:
                    dataApiClient = DataAPIClients.astraDev(token);
                break;
                case ASTRA_TEST:
                    dataApiClient = DataAPIClients.astraTest(token);
                break;
                case HCD,DSE:
                    dataApiClient = DataAPIClients.clientHCD();
                break;
                case CASSANDRA:
                    dataApiClient = DataAPIClients.clientCassandra();
                break;
                default:
                    throw new IllegalArgumentException("Invalid Environment");
            }
        }
        return dataApiClient;
    }

    protected AstraDBAdmin getAstraDBAdmin() {
        if (astraDbAdmin == null) {
            astraDbAdmin = getDataApiClient().getAdmin();
        }
        return astraDbAdmin;
    }

    protected Database getDatabase() {
        if (database == null) {
            log.info("Initializing Database in {}", getDataApiDestination().name());
            final DataAPIDestination env = getDataApiDestination();
            switch(env) {
                // Create KEYSPACE if needed in LOCAL
                case HCD, CASSANDRA, DSE:
                    database = getDataApiClient().getDatabase(DEFAULT_ENDPOINT_LOCAL);
                    if (!database.getDatabaseAdmin().keyspaceExists(DEFAULT_KEYSPACE)) {
                        log.info("Creating keyspace {}", getDataApiDestination().name());
                        database.getDatabaseAdmin().createKeyspace(DEFAULT_KEYSPACE);
                    }
                break;
                // Create DB if needed in ASTRA
                case ASTRA,ASTRA_DEV,ASTRA_TEST:
                    String databaseName = env.name().toLowerCase() + "_"
                            + getCloudProvider().name().toLowerCase() + "_"
                            + getCloudRegion().replaceAll("-", "_");
                    database = getAstraDBAdmin()
                            .createDatabase(databaseName, getCloudProvider(), getCloudRegion())
                            .getDatabase();
                default:
                    throw new IllegalArgumentException("Invalid Environment");
            }
        }
        return database;
    }

    protected DatabaseAdmin getDatabaseAdmin() {
        if (databaseAdmin == null) {
            databaseAdmin = getDatabase().getDatabaseAdmin();
        }
        return databaseAdmin;
    }

    protected void dropCollection(String name) {
        getDatabase().dropCollection(name);
        log.info("Collection {} dropped", name);
    }

    protected void dropTable(String name) {
        getDatabase().dropTable(name);
        log.info("Table {} dropped", name);
    }

    protected void dropAllCollections() {
        Database db = getDatabase();
        db.listCollectionNames().forEach(db::dropCollection);
    }

    protected void dropAllTables() {
        Database db = getDatabase();
        db.listTableNames().forEach(db::dropTable);
    }

}
