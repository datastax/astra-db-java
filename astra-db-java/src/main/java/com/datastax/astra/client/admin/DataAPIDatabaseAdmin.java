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

import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.databases.definition.keyspaces.KeyspaceOptions;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.command.AbstractCommandRunner;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.serdes.collections.DocumentSerializer;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Implementation of Client.
 */
@Slf4j
@Getter
public class DataAPIDatabaseAdmin extends AbstractCommandRunner<AdminOptions> implements DatabaseAdmin {

    /** parameters names. */
    private static final String ARG_KEYSPACE = "keyspaceName";

    /** Serializer. */
    protected static final DataAPISerializer SERIALIZER = new DocumentSerializer();

    /** Database if initialized from the DB. */
    protected Database db;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param db
     *      current database used to initialized the admin
     * @param options
     *      list of options for the admin
     */
    public DataAPIDatabaseAdmin(Database db, AdminOptions options) {
        super(db.getRootEndpoint(), options);
        this.db = db;
        this.options.commandType(CommandType.KEYSPACE_ADMIN);
        String apiVersion = options.getDataAPIClientOptions().getApiVersion();
        switch(options.getDataAPIClientOptions().getDestination()) {
            case ASTRA:
            case ASTRA_TEST:
            case ASTRA_DEV:
                if (db.getRootEndpoint().endsWith(".com")) {
                    this.apiEndpoint += "/api/json";
                }
                break;
            default:
                break;
        }
        if (!db.getRootEndpoint().endsWith(apiVersion)) {
          this.apiEndpoint += "/" + apiVersion;
        }
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
    public FindEmbeddingProvidersResult findEmbeddingProviders() {
        DataAPIResponse res = runCommand(Command.create("findEmbeddingProviders"));
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
        return db.useKeyspace(keyspace);
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase(String keyspace, String userToken) {
        return new Database(db.getRootEndpoint(), db.getOptions().clone()
                .token(userToken)
                .keyspace(keyspace));
    }

    /** {@inheritDoc} */
    @Override
    public void createKeyspace(String keyspace, boolean updateDBKeyspace) {
        Assert.hasLength(keyspace, ARG_KEYSPACE);
        createKeyspace(keyspace, KeyspaceOptions.simpleStrategy(1));
        if (updateDBKeyspace) {
            db.useKeyspace(keyspace);
        }
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

    @Override
    public void dropKeyspace(String keyspace, BaseOptions<?> options) {
        hasLength(keyspace, ARG_KEYSPACE);
        Command dropNamespace = Command
                .create("dropKeyspace")
                .append("name", keyspace);
        runCommand(dropNamespace, options);
        log.info("Keyspace  '" + green("{}") + "' has been deleted", keyspace);
    }

}
