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
import com.datastax.astra.client.model.NamespaceOptions;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
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
    private static final String ARG_NAMESPACE = "namespaceName";

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
    public Set<String> listNamespaceNames() {
        Command cmd = Command.create("findNamespaces");
        return runCommand(cmd)
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
    public Database getDatabase(String namespaceName) {
        return db.useNamespace(namespaceName);
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase(String namespaceName, String userToken) {
        Assert.hasLength(namespaceName, ARG_NAMESPACE);
        Assert.hasLength(userToken, "userToken");
        db = new Database(db.getDbApiEndpoint(), userToken, namespaceName, db.getOptions());
        return db;
    }

    /** {@inheritDoc} */
    @Override
    public void createNamespace(String namespace, boolean updateDbNamespace) {
        Assert.hasLength(namespace, ARG_NAMESPACE);
        createNamespace(namespace, NamespaceOptions.simpleStrategy(1));
        if (updateDbNamespace) {
            db.useNamespace(namespace);
        }
    }

    /**
     * Allow to create a namespace with full-fledged definition
     *
     * @param namespace
     *      namespace name
     * @param options
     *      options to create a namespace
     */
    public void createNamespace(String namespace, NamespaceOptions options) {
        hasLength(namespace, ARG_NAMESPACE);
        notNull(options, "options");
        Command createNamespace = Command
                        .create("createNamespace")
                        .append("name", namespace)
                        .withOptions(options);
        runCommand(createNamespace);
        log.info("Namespace  '" + green("{}") + "' has been created", namespace);
    }

    /** {@inheritDoc} */
    public void dropNamespace(String namespace) {
        hasLength(namespace, ARG_NAMESPACE);
        Command dropNamespace = Command
                .create("dropNamespace")
                .append("name", namespace);
        runCommand(dropNamespace);
        log.info("Namespace  '" + green("{}") + "' has been deleted", namespace);
    }

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return db.getDbApiEndpoint();
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
