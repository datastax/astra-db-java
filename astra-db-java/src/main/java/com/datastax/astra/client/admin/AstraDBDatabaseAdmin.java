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
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.commands.options.CreateKeyspaceOptions;
import com.datastax.astra.client.databases.commands.options.DropKeyspaceOptions;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.datastax.astra.client.databases.commands.results.FindRerankingProvidersResult;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceDefinition;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.DbKeyspacesClient;
import com.dtsx.astra.sdk.db.domain.Database;
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
        log.debug("getDatabaseInformations");
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
                getDatabaseInformations().getInfo().getRegion(), options.getDataAPIClientOptions().getAstraEnvironment())
                .getApiEndPoint();
    }

    /**
     * Access teh database with the default token.
     *
     * @param keyspace The name of the namespace (or keyspace) to retrieve. This parameter should match the
     *                      exact name of the namespace as it exists in the database.
     * @return
     *      client to interact with database DML.
     */
    public com.datastax.astra.client.databases.Database getDatabase(String keyspace) {
        return db.useKeyspace(keyspace);
    }

    /**
     * Access teh database with the specialized token.
     *
     * @param keyspace The name of the namespace (or keyspace) to retrieve. This parameter should match the
     *                      exact name of the namespace as it exists in the database.
     * @param tokenUser token with reduce privileges compared to admin token in order to do dml options (CRUD).
     * @return
     *      client to interact with database DML.
     */
    public com.datastax.astra.client.databases.Database getDatabase(String keyspace, String tokenUser) {
        return new com.datastax.astra.client.databases.Database(getApiEndpoint(), db.getOptions());
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
    public FindEmbeddingProvidersResult findEmbeddingProviders() {
        log.debug("findEmbeddingProviders");
        DataAPIDatabaseAdmin admin = new DataAPIDatabaseAdmin(db, this.options);
        return new FindEmbeddingProvidersResult(admin.findEmbeddingProviders().getEmbeddingProviders());
    }

    /** {@inheritDoc} */
    @Override
    public FindRerankingProvidersResult findRerankingProviders() {
        log.debug("findRerankingProviders");
        DataAPIDatabaseAdmin admin = new DataAPIDatabaseAdmin(db, this.options);
        return new FindRerankingProvidersResult(admin.findRerankingProviders().getRerankingProviders());
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
        log.warn("CommandOptions are not supported for dropKeyspace in Astra MODE");
        DbKeyspacesClient ks = devopsDbClient.database(databaseId.toString()).keyspaces();
        if (ks.exist(keyspace) && options.isIfExists()) {
            ks.delete(keyspace);
        }
    }

    /** {@inheritDoc} */
    @Override
    public com.datastax.astra.client.databases.Database getDatabase() {
        return db;
    }

}
