package com.datastax.astra;

import com.datastax.astra.devops.db.AstraDBDevopsClient;
import com.datastax.astra.devops.db.domain.Database;
import com.datastax.astra.devops.db.exception.DatabaseNotFoundException;
import com.datastax.astra.devops.utils.AstraEnvironment;
import io.stargate.sdk.data.client.DataApiClient;
import io.stargate.sdk.data.client.DataApiClients;
import io.stargate.sdk.data.client.model.namespaces.NamespaceInformation;
import io.stargate.sdk.data.internal.AbstractApiClient;
import io.stargate.sdk.http.LoadBalancedHttpClient;
import io.stargate.sdk.http.ServiceHttp;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class AstraDBDatabaseAdmin extends AbstractApiClient {

    /**
     * Top level resource for json api.
     */
    private final DataApiClient dataApiClient;

    /**
     * Token used for the credentials
     */
    final String token;

    /**
     * Token used for the credentials
     */
    final UUID databaseId;

    final AstraEnvironment env;

    /** Client for Astra Devops Api. */
    final AstraDBDevopsClient devopsDbClient;

    /** Options to personalized http client other client options. */
    final AstraClientOptions astraClientOptions;

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param databaseId
     *      database identifier
     */
    public AstraDBDatabaseAdmin(String token, UUID databaseId) {
        this(token, databaseId, AstraEnvironment.PROD, AstraClientOptions.builder().build());
    }

    /**
     * Initialize a database admin from token and database id.
     *
     * @param token
     *      token value
     * @param databaseId
     *      database identifier
     */
    public AstraDBDatabaseAdmin(String token, UUID databaseId, AstraEnvironment env, AstraClientOptions options) {
        this.env                = env;
        this.token              = token;
        this.databaseId         = databaseId;
        this.devopsDbClient     = new AstraDBDevopsClient(token, env);
        this.dataApiClient      = DataApiClients.create(getApiEndpoint(), token, options.asHttpClientOptions());
        this.astraClientOptions = options;
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

    /** {@inheritDoc} */
    public AstraDatabase getNamespace(String namespace) {
        return new AstraDatabase(getApiEndpoint(), token, namespace);
    }

    /** {@inheritDoc} */
    public Stream<String> listNamespaceNames() {
        return dataApiClient.listNamespaceNames();
    }

    /** {@inheritDoc} */
    public Stream<NamespaceInformation> listNamespaces() {
        return dataApiClient.listNamespaces();
    }

    /** {@inheritDoc} */
    public boolean namespaceExists(String namespace) {
        return dataApiClient.isNamespaceExists(namespace);
    }

    /** {@inheritDoc} */
    public AstraDatabase createNamespace(String namespace) {
        devopsDbClient.database(databaseId.toString()).keyspaces().create(namespace);
        return new AstraDatabase(getApiEndpoint(), token, namespace, astraClientOptions);
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

    @Override
    public Function<ServiceHttp, String> lookup() {
        return null;
    }

    @Override
    public LoadBalancedHttpClient getHttpClient() {
        return null;
    }
}
