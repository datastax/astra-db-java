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

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;
import com.datastax.astra.internal.api.AstraApiEndpoint;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.astra.client.admin.AstraDBAdmin.DEFAULT_NAMESPACE;

/**
 * Implementation of the DatabaseAdmin interface for Astra. To create the namespace the devops APi is leverage. To use this class a higher token permission is required.
 */
public class AstraDBDatabaseAdmin implements DatabaseAdmin {

    /** Token used for the credentials. */
    final String token;

    /** Token used for the credentials. */
    final UUID databaseId;

    /** Working environment. */
    final AstraEnvironment env;

    /** Client for Astra Devops Api. */
    final AstraDBOpsClient devopsDbClient;

    /** Database if initialized from the DB. */
    protected com.datastax.astra.client.Database db;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param db
     *      target database
     */
    public AstraDBDatabaseAdmin(com.datastax.astra.client.Database db) {
        this.databaseId = UUID.fromString(db.getDbApiEndpoint().substring(8, 44));
        this.env        = getEnvironment(db.getOptions().getDestination());
        this.token      = db.getToken();
        this.db         = db;
        this.devopsDbClient = new AstraDBOpsClient(token, this.env);
    }

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param databaseId
     *      database identifier
     * @param env
     *      working environment
     * @param options
     *      options used to initialize the http client
     */
    public AstraDBDatabaseAdmin(String token, UUID databaseId, AstraEnvironment env, DataAPIOptions options) {
        this.env            = env;
        this.token          = token;
        this.databaseId     = databaseId;
        this.devopsDbClient = new AstraDBOpsClient(token, this.env);
        this.db = new com.datastax.astra.client.Database(getApiEndpoint(), token, DEFAULT_NAMESPACE, options);
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
    private static AstraEnvironment getEnvironment(DataAPIOptions.DataAPIDestination destination) {
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
    private String getApiEndpoint() {
        return new AstraApiEndpoint(databaseId,
                getDatabaseInformations().getInfo().getRegion(), env)
                .getApiEndPoint();
    }

    /**
     * Access teh database with the default token.
     *
     * @param namespaceName The name of the namespace (or keyspace) to retrieve. This parameter should match the
     *                      exact name of the namespace as it exists in the database.
     * @return
     *      client to interact with database DML.
     */
    public com.datastax.astra.client.Database getDatabase(String namespaceName) {
        return db.useNamespace(namespaceName);
    }

    /**
     * Access teh database with the specialized token.
     *
     * @param namespaceName The name of the namespace (or keyspace) to retrieve. This parameter should match the
     *                      exact name of the namespace as it exists in the database.
     * @param tokenUser token with reduce privileges compared to admin token in order to do dml options (CRUD).
     * @return
     *      client to interact with database DML.
     */
    public com.datastax.astra.client.Database getDatabase(String namespaceName, String tokenUser) {
        return new com.datastax.astra.client.Database(getApiEndpoint(), tokenUser, namespaceName, db.getOptions());
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> listNamespaceNames() {
        return devopsDbClient
                .database(databaseId.toString())
                .keyspaces().findAll();
    }

    /** {@inheritDoc} */
    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders() {
        DataAPIDatabaseAdmin admin =
                new DataAPIDatabaseAdmin(getApiEndpoint() + "/" + db.getOptions().getApiVersion(), token, db.getOptions());
        return new FindEmbeddingProvidersResult(admin.findEmbeddingProviders().getEmbeddingProviders());
    }

    /** {@inheritDoc} */
    @Override
    public void createNamespace(String namespace) {
        devopsDbClient.database(databaseId.toString()).keyspaces().create(namespace);
    }

    /** {@inheritDoc} */
    @Override
    public void createNamespace(String namespace, boolean updateDbNamespace) {
        createNamespace(namespace);
        if (updateDbNamespace) {
            db.useNamespace(namespace);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dropNamespace(String namespace) {
        try {
            devopsDbClient.database(databaseId.toString()).keyspaces().delete(namespace);
        } catch(NullPointerException e) {
            // Left blank to parse output from a delete
        }
    }

    /** {@inheritDoc} */
    @Override
    public com.datastax.astra.client.Database getDatabase() {
        return db;
    }

}
