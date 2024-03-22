package com.datastax.astra.internal;

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
import com.datastax.astra.client.DatabaseAdmin;
import com.datastax.astra.internal.astra.AstraApiEndpoint;
import com.dtsx.astra.sdk.db.AstraDBOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.UUID;
import java.util.stream.Stream;

public class AstraDBDatabaseAdmin implements DatabaseAdmin {

    /**
     * Token used for the credentials
     */
    final String token;

    /**
     * Token used for the credentials
     */
    final UUID databaseId;

    final AstraEnvironment env;

    /** Options to personalized http client other client options. */
    final DataAPIOptions options;

    /** Client for Astra Devops Api. */
    final AstraDBOpsClient devopsDbClient;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param databaseId
     *      database identifier
     */
    public AstraDBDatabaseAdmin(String token, UUID databaseId, AstraEnvironment env, DataAPIOptions options) {
        this.env            = env;
        this.token          = token;
        this.databaseId     = databaseId;
        this.options        = options;
        this.devopsDbClient = new AstraDBOpsClient(token, this.env);
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

    // ------------------------------------------
    // ----  Crud on Namespaces (dataApi)    ----
    // ------------------------------------------

    private String getApiEndpoint() {
        return new AstraApiEndpoint(databaseId,
                getDatabaseInformations().getInfo().getRegion(), env)
                .getApiEndPoint();
    }

    public com.datastax.astra.client.Database getDatabase(String nameSpaceName) {
        return getDatabase(nameSpaceName, this.token);
    }

    public com.datastax.astra.client.Database getDatabase(String namespace, String tokenUser) {
        return new com.datastax.astra.client.Database(getApiEndpoint(), tokenUser, namespace);
    }

    /** {@inheritDoc} */
    public Stream<String> listNamespaceNames() {
        return devopsDbClient
                .database(databaseId.toString())
                .keyspaces().findAll().stream();
    }


    public void createNamespace(String namespace) {
        devopsDbClient.database(databaseId.toString()).keyspaces().create(namespace);
    }

    /**
     * Delete a namespace from current database. If the namespace does not exist not errors will be thrown.
     *
     * <pre>{@code
     *  // Initialize a db
     *  AstraDBDatabase db = new AstraDBDatabase("API_ENDPOINT", "TOKEN");
     *  // Drop a Namespace
     *  db.dropNamespace("<namespace_name>");
     * }</pre>
     * @param namespace
     *      current namespace
     */
    public void dropNamespace(String namespace) {
        devopsDbClient.database(databaseId.toString()).keyspaces().delete(namespace);
    }
}
