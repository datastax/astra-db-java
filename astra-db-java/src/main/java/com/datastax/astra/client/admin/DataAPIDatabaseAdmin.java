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
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.EmbeddingProvider;
import com.datastax.astra.client.model.FindEmbeddingProvidersResult;
import com.datastax.astra.client.model.KeyspaceOptions;
import com.datastax.astra.client.model.NamespaceOptions;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

import static com.datastax.astra.client.admin.AstraDBAdmin.DEFAULT_NAMESPACE;
import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Implementation of Client.
 */
@Slf4j
@Getter
public class DataAPIDatabaseAdmin extends AbstractCommandRunner implements DatabaseAdmin {

    /** parameters names. */
    private static final String ARG_KEYSPACE = "keyspaceName";

    /** Database if initialized from the DB. */
    protected Database db;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param apiEndpoint
     *      api endpoint.
     * @param options
     *      list of options for the admin
     */
    public DataAPIDatabaseAdmin(String apiEndpoint, String token, DataAPIOptions options) {
        this(new Database(apiEndpoint, token, DEFAULT_NAMESPACE, options));
    }

    /**
     * Initialize a database admin from token and database id.
     *
     * @param db
     *      current database instance
     */
    public DataAPIDatabaseAdmin(Database db) {
        this.db             = db;
        this.commandOptions = new CommandOptions<>()
                .token(db.getToken())
                .embeddingAuthProvider(db.getOptions().getEmbeddingAuthProvider())
                .httpClientOptions(db.getOptions().getHttpClientOptions());
        db.getOptions().getObservers().forEach(this.commandOptions::registerObserver);
    }

    // ------------------------------------------
    // ----      Namespace operations        ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public Set<String> listKeyspaceNames() {
        Command cmd = Command.create("findKeyspaces");
        return runCommand(cmd)
                .getStatusKeyAsStringStream("keyspaces")
                .collect(Collectors.toSet());
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public Set<String> listNamespaceNames() {
        Command cmd = Command.create("findNamespaces");
        ApiResponse response = runCommand(cmd);
        return response
                .getStatusKeyAsStringStream("namespaces")
                .collect(Collectors.toSet());
    }

    /** {@inheritDoc} */
    @Override
    public FindEmbeddingProvidersResult findEmbeddingProviders() {
        ApiResponse res = runCommand(Command.create("findEmbeddingProviders"));
        return new FindEmbeddingProvidersResult(
                res.getStatusKeyAsMap("embeddingProviders",
                EmbeddingProvider.class));
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase() {
        return db;
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase(String keyspace) {
        return db.useNamespace(keyspace);
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase(String keyspace, String userToken) {
        Assert.hasLength(keyspace, ARG_KEYSPACE);
        Assert.hasLength(userToken, "userToken");
        db = new Database(db.getDbApiEndpoint(), userToken, keyspace, db.getOptions());
        return db;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void createNamespace(String keyspace, boolean updateDBKeyspace) {
        createKeyspace(keyspace, updateDBKeyspace);
        Assert.hasLength(keyspace, ARG_KEYSPACE);
        createKeyspace(keyspace, KeyspaceOptions.simpleStrategy(1));
        if (updateDBKeyspace) {
            db.useNamespace(keyspace);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void createKeyspace(String keyspace, boolean updateDBKeyspace) {
        Assert.hasLength(keyspace, ARG_KEYSPACE);
        createKeyspace(keyspace, KeyspaceOptions.simpleStrategy(1));
        if (updateDBKeyspace) {
            db.useNamespace(keyspace);
        }
    }

    /**
     * Allow to create a namespace with full-fledged definition
     *
     * @param namespace
     *      namespace name
     * @param options
     *      options to create a namespace
     * @deprecated use {@link #createKeyspace(String, KeyspaceOptions)} instead
     */
    @Deprecated
    public void createNamespace(String namespace, NamespaceOptions options) {
        hasLength(namespace, ARG_KEYSPACE);
        notNull(options, "options");
        Command createNamespace = Command
                        .create("createKeyspace")
                        .append("name", namespace)
                        .withOptions(options);
        runCommand(createNamespace);
        log.info("Namespace  '" + green("{}") + "' has been created", namespace);
    }

    /**
     * Allow to create a keyspace with full-fledged definition
     *
     * @param keyspace
     *      keyspace name
     * @param options
     *      options to create a namespace
     */
    public void createKeyspace(String keyspace, KeyspaceOptions options) {
        hasLength(keyspace, ARG_KEYSPACE);
        notNull(options, "options");
        Command createKeypace = Command
                .create("createKeyspace")
                .append("name", keyspace)
                .withOptions(options);
        runCommand(createKeypace);
        log.info("Keyspace  '" + green("{}") + "' has been created", keyspace);
    }

    /** {@inheritDoc} */
    @Deprecated
    public void dropNamespace(String namespace) {
        dropKeyspace(namespace);
    }

    @Override
    public void dropKeyspace(String keyspace) {
        hasLength(keyspace, ARG_KEYSPACE);
        Command dropNamespace = Command
                .create("dropKeyspace")
                .append("name", keyspace);
        runCommand(dropNamespace);
        log.info("Keyspace  '" + green("{}") + "' has been deleted", keyspace);
    }

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return db.getDbApiEndpoint() + "/v1";
    }

    /**
     * Register a listener to execute commands on the collection. Please now use {@link CommandOptions}.
     *
     * @param logger
     *      name for the logger
     * @param commandObserver
     *      class for the logger
     */
    public void registerListener(String logger, CommandObserver commandObserver) {
        this.commandOptions.registerObserver(logger, commandObserver);
    }

}
