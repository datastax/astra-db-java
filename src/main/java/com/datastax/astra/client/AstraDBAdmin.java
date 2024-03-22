package com.datastax.astra.client;

import com.datastax.astra.internal.astra.AstraApiEndpoint;
import com.datastax.astra.internal.astra.AstraDBDatabaseAdmin;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.utils.ApiLocator;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.dtsx.astra.sdk.utils.AstraRc;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.dtsx.astra.sdk.utils.Utils.readEnvVariable;

/**
 * Main Client for AstraDB, it implements administration and Data Api Operations.

 */
@Slf4j
public class AstraDBAdmin {

    /** Default cloud provider. (free-tier) */
    public static final CloudProviderType FREE_TIER_CLOUD = CloudProviderType.GCP;

    /** Default region. (free-tier) */
    public static final String FREE_TIER_CLOUD_REGION = "us-east1";

    /** Header name used to hold the Astra Token. */
    public static final String TOKEN_HEADER_PARAM = "X-Token";

    /** Default keyspace (same created by the ui). */
    public static final String DEFAULT_NAMESPACE = "default_keyspace";

    /** Client for Astra Devops Api. */
    final AstraDBOpsClient devopsDbClient;

    /** Options to personalized http client other client options. */
    final DataAPIOptions dataAPIOptions;

    /** Astra Environment. */
    final AstraEnvironment env;

    /** Astra Token (credentials). */
    final String token;

    /** Side Http Client (use only to resume a db). */
    final HttpClient httpClient;

    /** Token read for environment variable ASTRA_DB_APPLICATION_TOKEN (if any). */
    static String astraConfigToken;

    /*
     * Load token values from environment variables and ~/.astrarc.
     */
    static {
        new AstraRc().getSectionKey(
                AstraRc.ASTRARC_DEFAULT,
                AstraRc.ASTRA_DB_APPLICATION_TOKEN).ifPresent(s -> astraConfigToken = s);
        // lookup env variable
        readEnvVariable(AstraRc.ASTRA_DB_APPLICATION_TOKEN).ifPresent(s -> astraConfigToken = s);
    }

    /**
     * Initialization with an authentification token and target environment, Use this constructor for testing purpose.
     *
     * @param token
     *      authentication token
     * @param options
     *      options for client
     */
    AstraDBAdmin(String token, AstraEnvironment env, DataAPIOptions options) {
        Assert.hasLength(token, "token");
        Assert.notNull(env, "environment");
        Assert.notNull(options, "options");
        this.token = token;
        this.env = env;
        this.dataAPIOptions = options;
        this.devopsDbClient = new AstraDBOpsClient(token, this.env);

        // Local Agent for Resume
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(options.getHttpClientOptions().getHttpVersion());
        httpClientBuilder.connectTimeout(Duration.ofSeconds(options.getHttpClientOptions().getConnectionRequestTimeoutInSeconds()));
        this.httpClient = httpClientBuilder.build();
    }

    // --------------------
    // -- Databases     ---
    // --------------------

    /**
     * List available database names.
     *
     * @return
     *      list of database names
     */
    public List<String> listDatabaseNames() {
        return listDatabases().map(db -> db.getInfo().getName()).collect(Collectors.toList());
    }

    /**
     * Return true if the database exists.
     * @param name
     *      database identifiers
     * @return
     *      if the database exits or not
     */
    public boolean databaseExists(String name) {
        Assert.hasLength(name, "name");
        return listDatabaseNames().contains(name);
    }

    /**
     * Return true if the database exists.
     *
     * @param id
     *      database identifiers
     * @return
     *      if the database exits or not
     */
    public boolean databaseExists(UUID id) {
        Assert.notNull(id, "id");
        return devopsDbClient.findById(id.toString()).isPresent();
    }

    /**
     * List active databases with vector enabled in current organization.
     *
     * @return
     *      active databases list
     */
    public Stream<Database> listDatabases() {
        return devopsDbClient
                .findAllNonTerminated()
                .filter(db -> db.getInfo().getDbType() != null);
    }

    /**
     * Create new database with a name on free tier. The database name should not exist in the tenant.
     *
     * @param name
     *    database name
     * @return
     *    database identifier
     */
    public UUID createDatabase(String name) {
        Assert.hasLength(name, "name");
        return createDatabase(name, FREE_TIER_CLOUD, FREE_TIER_CLOUD_REGION);
    }

    /**
     * Create new database with a name on the specified cloud provider and region.
     * If the database with same name already exists it will be resumed if not active.
     * The method will wait for the database to be active.
     *
     * @param name
     *      database name
     * @param cloud
     *      cloud provider
     * @param cloudRegion
     *      cloud region
     * @return
     *      database identifier
     */
    public UUID createDatabase(String name, CloudProviderType cloud, String cloudRegion, boolean waitForDb) {
        Assert.hasLength(name, "name");
        Assert.notNull(cloud, "cloud");
        Assert.hasLength(cloudRegion, "cloudRegion");
        Optional<Database> optDb = listDatabases().filter(db->name.equals(db.getInfo().getName())).findFirst();
        // Handling all cases for the user
        if (optDb.isPresent()) {
            Database db = optDb.get();
            switch(db.getStatus()) {
                case ACTIVE:
                    log.info("Database " + green("{}") + " already exists and is ACTIVE.", name);
                    return UUID.fromString(db.getId());
                case MAINTENANCE:
                case INITIALIZING:
                case PENDING:
                case RESUMING:
                    log.info("Database {} already exists and is in {} state, waiting for it to be ACTIVE", name, db.getStatus());
                    if (waitForDb) {
                        waitForDatabase(devopsDbClient.database(db.getId()));
                    }
                    return UUID.fromString(db.getId());
                case HIBERNATED:
                    log.info("Database {} is in {} state, resuming...", name, db.getStatus());
                    resumeDb(db);
                    if (waitForDb) {
                        waitForDatabase(devopsDbClient.database(db.getId()));
                    }
                    return UUID.fromString(db.getId());
                default:
                    throw new IllegalStateException("Database already exist but cannot be activate");
            }
        }
        // Database is not present, creating and waiting for it to be active.
        UUID newDbId = UUID.fromString(devopsDbClient.create(DatabaseCreationRequest.builder()
                .name(name)
                .cloudProvider(cloud)
                .cloudRegion(cloudRegion)
                .keyspace(DEFAULT_NAMESPACE)
                .withVector().build()));
        log.info("Database {} is starting (id={}): it will take about a minute please wait...", name, newDbId);
        if (waitForDb) {
            waitForDatabase(devopsDbClient.database(newDbId.toString()));
        }
        return newDbId;
    }

    /**
     * Create new database with a name on the specified cloud provider and region.
     * If the database with same name already exists it will be resumed if not active.
     * The method will wait for the database to be active.
     *
     * @param name
     *      database name
     * @param cloud
     *      cloud provider
     * @param cloudRegion
     *      cloud region
     * @return
     *      database identifier
     */
    public UUID createDatabase(String name, CloudProviderType cloud, String cloudRegion) {
        return createDatabase(name, cloud, cloudRegion, true);
    }

    /**
     * Delete a Database if exists from its name
     *
     * @param databaseId
     *    database identifier
     * @return
     *      if the db has been deleted
     */
    public boolean dropDatabase(@NonNull UUID databaseId) {
        Assert.notNull(databaseId, "Database identifier");
        boolean exists = databaseExists(databaseId);
        getDatabaseInformations(databaseId);
        devopsDbClient.database(databaseId.toString()).delete();
        return exists;
    }

    public boolean dropDatabase(@NonNull String databaseName) {
        Assert.hasLength(databaseName, "database");
        com.datastax.astra.internal.utils.Assert.hasLength(databaseName, "Database ");
        Optional<Database> db = listDatabases().filter(d -> d.getInfo().getName().equals(databaseName)).findFirst();
        if (db.isPresent()) {
            devopsDbClient.database(db.get().getId()).delete();
            return true;
        }
        return false;
    }

    /**
     * Find a database from its id.
     *
     * @param id
     *          a database name
     * @return
     *          list of db matching the criteria
     */
    public Database getDatabaseInformations(@NonNull UUID id) {
        Assert.notNull(id, "Database identifier should not be null");
        return devopsDbClient.findById(id.toString())
                .orElseThrow(() -> new DatabaseNotFoundException(id.toString()));
    }

    // --------------------
    // == Sub resources  ==
    // --------------------

    /**
     * Access the database functions.
     *
     * @param databaseId
     *      database identifier
     * @return
     *      database client
     */
    public com.datastax.astra.client.Database getDatabase(UUID databaseId, String namespace) {
        Assert.notNull(databaseId, "databaseId");
        Assert.hasLength(namespace, "namespace");
        String databaseRegion = devopsDbClient
                .findById(databaseId.toString())
                .map(db -> db.getInfo().getRegion())
                .orElseThrow(() -> new DatabaseNotFoundException(databaseId.toString()));
        return new com.datastax.astra.client.Database(
            new AstraApiEndpoint(databaseId, databaseRegion, env).getApiEndPoint(),
            token,namespace, dataAPIOptions) {
        };
    }

    /**
     * Access the database functions.
     *
     * @param databaseId
     *      database identifier
     * @return
     *      database client
     */
    public com.datastax.astra.client.Database getDatabase(UUID databaseId) {
        return getDatabase(databaseId, DEFAULT_NAMESPACE);
    }

    /**
     * Access the database functions.
     *
     * @param databaseId
     *      database identifier
     * @return
     *      database client
     */
    public AstraDBDatabaseAdmin getDatabaseAdmin(UUID databaseId) {
        Assert.notNull(databaseId, "databaseId");
        return new AstraDBDatabaseAdmin(token, databaseId, env, dataAPIOptions);
    }

    /**
     * Wait for db to have proper status.
     *
     * @param dbc
     *      database client
     */
    @SuppressWarnings("java:S2925")
    private void waitForDatabase(DbOpsClient dbc) {
        long top = System.currentTimeMillis();
        while(DatabaseStatusType.ACTIVE != getStatus(dbc) && ((System.currentTimeMillis()-top) < 1000L*180)) {
            try {
                Thread.sleep( 5000);
                System.out.print("■");
                System.out.flush();
            } catch (InterruptedException e) {
                log.warn("Interrupted {}",e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        if (getStatus(dbc) != DatabaseStatusType.ACTIVE) {
            throw new IllegalStateException("Database is not in expected state after timeouts");
        }
    }

    /**
     * Retrieve the status of a database.
     * @param dbc
     *      database client
     * @return
     *      database status
     */
    private DatabaseStatusType getStatus(DbOpsClient dbc) {
        return dbc.find().orElseThrow(() -> new DatabaseNotFoundException(dbc.getDatabaseId())).getStatus();
    }

    /**
     * Database name.
     *
     * @param db
     *      database name
     */
    private void resumeDb(Database db) {
        try {
            // Compute Endpoint for the Keyspace
            String endpoint = ApiLocator.getApiRestEndpoint(db.getId(), db.getInfo().getRegion()) + "/v2/schemas/keyspace";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header(TOKEN_HEADER_PARAM, token)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 500) {
                throw new IllegalStateException("Cannot resume database, please check your account");
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted {}",e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Resuming request might have failed, please check {}}",e.getMessage());
        }
    }

}
