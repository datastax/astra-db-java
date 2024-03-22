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
import com.datastax.astra.client.Database;
import com.datastax.astra.client.DatabaseAdmin;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.namespaces.CreateNamespaceOptions;
import com.datastax.astra.internal.http.HttpClientOptions;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import static com.datastax.astra.internal.utils.AnsiUtils.green;
import static com.datastax.astra.internal.utils.Assert.hasLength;
import static com.datastax.astra.internal.utils.Assert.notNull;

/**
 * Implementation of Client.
 */
@Slf4j
@Getter
public class DataAPIDatabaseAdmin extends AbstractCommandRunner implements DatabaseAdmin {

    /** Version of the API. */
    protected final DataAPIOptions options;

    /** Version of the API. */
    protected final String apiEndPoint;

    /** Version of the API. */
    protected final String token;

    /** Endpoint for a Database in particular. */
    private final String apiEndpointDatabase;

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
        this.apiEndPoint = apiEndpoint;
        this.token       = token;
        this.options     = options;
        this.apiEndpointDatabase = apiEndpoint + "/" + options.getApiVersion();
    }

    // ------------------------------------------
    // ----      Namespace operations        ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public Stream<String> listNamespaceNames() {
        Command cmd = Command.create("findNamespaces");
        return runCommand(cmd).getStatusKeyAsStringStream("namespaces");
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase(String namespaceName) {
        Assert.hasLength(namespaceName, "namespaceName");
        return new Database(apiEndPoint, token, namespaceName, options);
    }

    /** {@inheritDoc} */
    @Override
    public Database getDatabase(String namespaceName, String userToken) {
        Assert.hasLength(namespaceName, "namespaceName");
        Assert.hasLength(userToken, "userToken");
        return new Database(apiEndPoint, userToken, namespaceName, options);
    }

    /** {@inheritDoc} */
    public void createNamespace(String namespace) {
        Assert.hasLength(namespace, "namespace");
        createNamespace(namespace, CreateNamespaceOptions.simpleStrategy(1));
    }

    /**
     * Allow to create a namespace with full-fledged definition
     *
     * @param namespace
     *      namespace name
     * @param options
     *      options to create a namespace
     */
    public void createNamespace(String namespace, CreateNamespaceOptions options) {
        hasLength(namespace, "namespace");
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
        hasLength(namespace, "namespace");
        Command dropNamespace = Command
                .create("dropNamespace")
                .append("name", namespace);
        runCommand(dropNamespace);
        log.info("Namespace  '" + green("{}") + "' has been deleted", namespace);
    }

    /** {@inheritDoc} */
    @Override
    protected String getApiEndpoint() {
        return apiEndpointDatabase;
    }

    /** {@inheritDoc} */
    @Override
    protected HttpClientOptions getHttpClientOptions() {
        return options.getHttpClientOptions();
    }
}
