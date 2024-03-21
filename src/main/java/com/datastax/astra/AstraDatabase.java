package com.datastax.astra;

import io.stargate.sdk.data.client.DataApiClient;
import io.stargate.sdk.data.client.DataApiClients;
import io.stargate.sdk.data.client.DataApiNamespace;
import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.collections.CollectionDefinition;
import io.stargate.sdk.data.client.model.collections.CollectionOptions;
import io.stargate.sdk.data.internal.AbstractApiClient;
import io.stargate.sdk.http.LoadBalancedHttpClient;
import io.stargate.sdk.http.ServiceHttp;
import io.stargate.sdk.utils.Assert;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Client for AstraDB at database level (crud for collections).
 */
@Slf4j
public class AstraDatabase extends AbstractApiClient {

    /**
     * Url to access the API
     */
    private final AstraApiEndpoint astraApiEndpoint;

    /**
     * Top level resource for json api.
     */
    private final DataApiClient dataApiClient;

    /**
     * Namespace client
     */
    private final DataApiNamespace dataApiNamespace;

    /**
     * Token used for the credentials
     */
    private final String token;

    /**
     * Name of the namespace in use.
     */
    @Getter
    private final String namespaceName;

    /**
     * Configuration of the
     */
    private final AstraClientOptions astraClientOptions;

    /**
     * Initialization with endpoint and apikey.
     *
     * @param token
     *      api token
     * @param apiEndpoint
     *      api endpoint
     */
    public AstraDatabase(String apiEndpoint, String token) {
        this(apiEndpoint, token, AstraDBAdmin.DEFAULT_KEYSPACE);
    }

    /**
     * Initialization with endpoint and apikey.
     *
     * @param token
     *      api token
     * @param apiEndpoint
     *      api endpoint
     * @param namespace
     *      namespace
     */
    public AstraDatabase(String apiEndpoint, String token, String namespace) {
        this(apiEndpoint, token, namespace, AstraDBAdmin.DEFAULT_OPTIONS);
    }

    /**
     * Initialization with endpoint and apikey.
     *
     * @param token
     *      api token
     * @param apiEndpoint
     *      api endpoint
     * @param options
     *      setup of the clients with options
     */
    public AstraDatabase(String apiEndpoint, String token, AstraClientOptions options) {
        this(apiEndpoint, token, AstraDBAdmin.DEFAULT_KEYSPACE, options);
    }

    /**
     * Initialization with endpoint and apikey.
     *
     * @param token
     *      api token
     * @param apiEndpoint
     *      api endpoint
     * @param namespace
     *      namespace
     * @param options
     *      setup of the clients with options
     */
    public AstraDatabase(String apiEndpoint, String token, String namespace, AstraClientOptions options) {
        this(AstraApiEndpoint.parse(apiEndpoint), token, namespace, options);
    }

    /**
     * Initialization with endpoint and apikey.
     *
     * @param apiEndpoint
     *      api endpoint
     *  @param token
     *      api token
     * @param namespace
     *      namespace
     * @param astraClientOptions
     *      setup of the clients with options
     */
    public AstraDatabase(AstraApiEndpoint apiEndpoint, String token, String namespace, AstraClientOptions astraClientOptions) {
        Assert.notNull(apiEndpoint, "endpoint");
        Assert.hasLength(token, "token");
        Assert.hasLength(namespace, "namespace");
        this.token              = token;
        this.astraApiEndpoint   = apiEndpoint;
        this.astraClientOptions = astraClientOptions;
        this.namespaceName      = namespace;
        this.dataApiClient      = DataApiClients.create(astraApiEndpoint.getApiEndPoint(), token, astraClientOptions.asHttpClientOptions());
        this.dataApiNamespace   = dataApiClient.getNamespace(namespace);
    }

    // --------------------------------------------
    // ---- Information on current Namespace   ----
    // --------------------------------------------

    /**
     * Access to Admin.
     * @return
     */
    public AstraDBDatabaseAdmin getDatabaseAdmin() {
        return new AstraDBDatabaseAdmin(token, astraApiEndpoint.getDatabaseId(), astraApiEndpoint.getEnv(), astraClientOptions);
    }

    // --------------------------------------------
    // ----  Crud on Collection (namespace)    ----
    // --------------------------------------------

    /** {@inheritDoc} */
    public Stream<String> listCollectionNames() {
        return dataApiNamespace.listCollectionNames();
    }

    /** {@inheritDoc} */
    public Stream<CollectionDefinition> listCollections() {
        return dataApiNamespace.listCollections();
    }

    /** {@inheritDoc} */
    public AstraCollection<Document> getCollection(String collectionName) {
        return getCollection(collectionName, Document.class);
    }

    /** {@inheritDoc} */
    public <DOC> AstraCollection<DOC> getCollection(String collectionName, Class<DOC> documentClass) {
        return new AstraCollection<>(this, collectionName, documentClass);
    }

    /** {@inheritDoc} */
    public void drop() {
        ((AstraDBAdmin) dataApiClient).dropDatabase(astraApiEndpoint.getDatabaseId());
    }

    /** {@inheritDoc} */
    public AstraCollection<Document> createCollection(String collectionName) {
        return createCollection(collectionName, null, Document.class);
    }

    /** {@inheritDoc} */
    public <DOC> AstraCollection<DOC> createCollection(String collectionName, Class<DOC> documentClass) {
        return createCollection(collectionName, null, documentClass);
    }

    /** {@inheritDoc} */
    public AstraCollection<Document> createCollection(String collectionName, CollectionOptions createCollectionOptions) {
        return createCollection(collectionName, createCollectionOptions, Document.class);
    }

    /** {@inheritDoc} */
    public <DOC> AstraCollection<DOC> createCollection(String collectionName, CollectionOptions createCollectionOptions, Class<DOC> documentClass) {
        dataApiNamespace.createCollection(collectionName, createCollectionOptions, documentClass);
        return new AstraCollection<>(this, collectionName, documentClass);
    }

    /** {@inheritDoc} */
    public void dropCollection(String collectionName) {
        dataApiNamespace.dropCollection(collectionName);
    }

    /** {@inheritDoc} */
    @Override
    public Function<ServiceHttp, String> lookup() {
        return dataApiNamespace.lookup();
    }

    /** {@inheritDoc} */
    @Override
    public LoadBalancedHttpClient getHttpClient() {
        return dataApiNamespace.getHttpClient();
    }

}
