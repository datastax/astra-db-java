package com.datastax.astra.internal;

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.DatabaseAdmin;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.namespaces.CreateNamespaceOptions;
import com.datastax.astra.internal.http.HttpClientOptions;
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

    private final String apiEndpointDatabase;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param apiEndpoint
     *      api endpoint.
     */
    public DataAPIDatabaseAdmin(String apiEndpoint, String token, DataAPIOptions options) {
        this.apiEndPoint = apiEndpoint;
        this.token       = token;
        this.options     = options;
        StringBuilder dbApiEndPointBuilder = new StringBuilder(apiEndpoint);
        dbApiEndPointBuilder
                .append("/")
                .append(options.getApiVersion());
        this.apiEndpointDatabase = dbApiEndPointBuilder.toString();
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

    @Override
    public Database getDatabase(String namespaceName) {
        return null;
    }

    @Override
    public Database getDatabase(String namespaceName, String userToken) {
        return null;
    }

    /** {@inheritDoc} */
    public void createNamespace(String namespace) {
        createNamespace(namespace, CreateNamespaceOptions.simpleStrategy(1));
    }

    /** {@inheritDoc} */
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

    @Override
    protected String getApiEndpoint() {
        return apiEndPoint;
    }

    @Override
    protected HttpClientOptions getHttpClientOptions() {
        return null;
    }
}
