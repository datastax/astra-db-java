package com.datastax.astra.client.admin;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.admin.options.AdminOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.commands.options.CreateKeyspaceOptions;
import com.datastax.astra.client.databases.commands.options.DropKeyspaceOptions;
import com.datastax.astra.client.databases.commands.options.FindEmbeddingProvidersOptions;
import com.datastax.astra.client.databases.commands.options.FindRerankingProvidersOptions;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.DbKeyspacesClient;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;


/**
 * Implementation of the DatabaseAdmin interface for Astra. To create the namespace the devops APi is leverage. To use this class a higher token permission is required.
 */
@Slf4j
public class AstraDBDatabaseAdmin extends AbstractCommandRunner<AdminOptions> implements DatabaseAdmin {

    /** Token used for the credentials. */
    final UUID databaseId;

    /** Client for Astra Devops Api. */
    final AstraDBOpsClient devopsDbClient;

    /** Database if initialized from the DB. */
    final com.datastax.astra.client.databases.Database db;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param db
     *      target database
     * @param adminOptions
     *     options to use for the admin operations like timeouts
     */
    public AstraDBDatabaseAdmin(com.datastax.astra.client.databases.Database db, AdminOptions adminOptions) {
        Assert.notNull(db, "database");
        this.db          = db;
        this.databaseId  = db.getId(); // UUID.fromString(db.getApiEndpoint().substring(8, 44));
        this.options = adminOptions;
        if (adminOptions == null) {
            this.options = new AdminOptions();
        }
        // Using database options if no extra parameters provided
        if (this.options.getToken() == null) {
            this.options.token(db.getOptions().getToken());
        }
        if (this.options.getDataAPIClientOptions() == null) {
            this.options.dataAPIClientOptions(db.getOptions().getDataAPIClientOptions());
        }
        this.devopsDbClient = new AstraDBOpsClient(
                options.getToken(),
                options.getDataAPIClientOptions().getAstraEnvironment());
    }

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param databaseId
     *      database identifier
     * @param clientOptions
     *      options used to initialize the http client
     */
    public AstraDBDatabaseAdmin(String token, UUID databaseId, DataAPIClientOptions clientOptions) {
        this.databaseId     = databaseId;
        this.options        = new AdminOptions(token, clientOptions);
        this.devopsDbClient = new AstraDBOpsClient(token, options.getDataAPIClientOptions().getAstraEnvironment());

        this.db = new com.datastax.astra.client.databases.Database(getApiEndpoint(),
                new DatabaseOptions(token,  options.getDataAPIClientOptions()));
    }

    /**
     * Find a database from its id.
     *
     * @return
     *     list of db matching the criteria
     */
    public Database getDatabaseInformations() {
        return devopsDbClient
                .findById(databaseId.toString())
                .orElseThrow(() -> new DatabaseNotFoundException(databaseId.toString()));
    }

    /**
     * Extract environment from the destination.
     *
     * @param destination
     *      destination to extract the environment from
     * @return
     *      the environment
     */
    private static AstraEnvironment getEnvironment(DataAPIDestination destination) {
        switch (destination) {
            case ASTRA:
                return AstraEnvironment.PROD;
            case ASTRA_DEV:
                return AstraEnvironment.DEV;
            case ASTRA_TEST:
                return AstraEnvironment.TEST;
            default:
                throw new IllegalArgumentException("Unknown destination: " + destination);
        }
    }

    // ------------------------------------------
    // ----  Crud on Namespaces (dataApi)    ----
    // ------------------------------------------

    /**
     * Get the API endpoint for the current database.
     *
     * @return
     *      the endpoint as an url.
     */
    public String getApiEndpoint() {
        return new AstraApiEndpoint(databaseId,
                getDatabaseInformations().getInfo().getRegion(),
                options.getDataAPIClientOptions().getAstraEnvironment())
                .getApiEndPoint();
    }

    /** {@inheritDoc} */
    @Override
    public com.datastax.astra.client.databases.Database getDatabase() {
        return db;
    }

    /** {@inheritDoc} */
    @Override
    public com.datastax.astra.client.databases.Database getDatabase(String keyspace) {
        return db.useKeyspace(keyspace);
    }

    @Override
    public com.datastax.astra.client.databases.Database getDatabase(String keyspace, String userToken, DatabaseOptions options) {
        String          targetToken    = (userToken != null) ? userToken : db.getOptions().getToken();
        DatabaseOptions targetOptions  = (options != null) ?   options :   db.getOptions();
        String          targetKeyspace = (keyspace != null) ?  keyspace :  db.getKeyspace();
        return new com.datastax.astra.client.databases.Database(db.getRootEndpoint(), targetOptions
                .clone()
                .token(targetToken)
                .keyspace(targetKeyspace));
    }

    /** {@inheritDoc} */
    @Override
    public com.datastax.astra.client.databases.Database getDatabase(String keyspace, String userToken) {
        String          targetToken    = (userToken != null) ? userToken : db.getOptions().getToken();
        String          targetKeyspace = (keyspace != null) ?  keyspace :  db.getKeyspace();
        return new com.datastax.astra.client.databases.Database(db.getRootEndpoint(), db.getOptions().clone()
                .token(targetToken)
                .keyspace(targetKeyspace));
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> listKeyspaceNames() {
        log.debug("listKeyspaceNames");
        return devopsDbClient
                .database(databaseId.toString())
                .keyspaces().findAll();
    }

    /** {@inheritDoc} */
    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders(FindEmbeddingProvidersOptions options) {
        log.debug("findEmbeddingProviders");
        DataAPIDatabaseAdmin admin = new DataAPIDatabaseAdmin(db, this.options);
        return new FindEmbeddingProvidersResult(admin.findEmbeddingProviders(options).getEmbeddingProviders());
    }

    /** {@inheritDoc} */
    @Override
    public FindRerankingProvidersResult findRerankingProviders(FindRerankingProvidersOptions options) {
        log.debug("findRerankingProviders");
        DataAPIDatabaseAdmin admin = new DataAPIDatabaseAdmin(db, this.options);
        return new FindRerankingProvidersResult(admin.findRerankingProviders(options).getRerankingProviders());
    }

    /** {@inheritDoc} */
    @Override
    public void createKeyspace(String keyspace) {
        createKeyspace(
                new KeyspaceDefinition().name(keyspace),
                new CreateKeyspaceOptions().ifNotExists(true));
    }

    @Override
    public void createKeyspace(KeyspaceDefinition keyspace, CreateKeyspaceOptions options) {
        waitForDatabaseActive();
        String keyspaceName  = keyspace.getName();
        DbKeyspacesClient ks = devopsDbClient.database(databaseId.toString()).keyspaces();
        if (!ks.exist(keyspaceName) && options.isIfNotExists()) {
            ks.create(keyspaceName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dropKeyspace(String keyspace) {
        log.debug("dropKeyspace");
        dropKeyspace(keyspace, new DropKeyspaceOptions().ifExists(true));
    }

    /** {@inheritDoc} */
    @Override
    public void dropKeyspace(String keyspace, DropKeyspaceOptions options) {
        waitForDatabaseActive();
        DbKeyspacesClient ks = devopsDbClient.database(databaseId.toString()).keyspaces();
        if (ks.exist(keyspace) && options.isIfExists()) {
            ks.delete(keyspace);
        }
    }

    /**
     * Wait for the database to be in ACTIVE status before proceeding.
     * This is required before keyspace operations as the database may be
     * in MAINTENANCE state from a previous operation.
     */
    @SuppressWarnings("java:S2925")
    private void waitForDatabaseActive() {
        DbOpsClient dbc = devopsDbClient.database(databaseId.toString());
        long top = System.currentTimeMillis();
        long timeout = 1000L * AstraDBAdmin.WAIT_IN_SECONDS;
        while (DatabaseStatusType.ACTIVE != dbc.get().getStatus()
                && ((System.currentTimeMillis() - top) < timeout)) {
            try {
                Thread.sleep(5000);
                log.info("...waiting for database '{}' to become active...", databaseId);
            } catch (InterruptedException e) {
                log.warn("Interrupted {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        if (DatabaseStatusType.ACTIVE != dbc.get().getStatus()) {
            throw new IllegalStateException("Database " + databaseId
                    + " is not active after timeout of "
                    + AstraDBAdmin.WAIT_IN_SECONDS + " seconds.");
        }
    }

}
