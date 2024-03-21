package com.datastax.astra.internal;

import io.stargate.sdk.data.client.Collection;
import io.stargate.sdk.data.client.Database;
import io.stargate.sdk.data.client.DatabaseAdmin;
import io.stargate.sdk.data.client.model.Command;
import io.stargate.sdk.data.client.model.Document;
import io.stargate.sdk.data.client.model.collections.CollectionDefinition;
import io.stargate.sdk.data.client.model.collections.CollectionOptions;
import io.stargate.sdk.http.LoadBalancedHttpClient;
import io.stargate.sdk.http.ServiceHttp;
import io.stargate.sdk.utils.JsonUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.stream.Stream;

import static io.stargate.sdk.utils.AnsiUtils.green;
import static io.stargate.sdk.utils.Assert.hasLength;
import static io.stargate.sdk.utils.Assert.notNull;

/**
 * Default implementation of the {@link Database}.
 */
@Slf4j @Getter
public class DatabaseImpl extends AbstractCommandRunner implements Database {

    /** Reference to the Data APi client. */
    private final DatabaseAdmin apiDataAPIClient;

    /** Current Namespace information. */
    private final String namespaceName;

    /** Resource for namespace. */
    public final Function<ServiceHttp, String> namespaceResource;

    /**
     * Constructor with api and namespace.
     *
     * @param apiDataAPIClient
     *      reference to api client
     * @param namespace
     *      current namespace
     */
    public DatabaseImpl(DatabaseAdmin apiDataAPIClient, String namespace) {
        notNull(apiDataAPIClient, "apiClient");
        hasLength(namespace, "namespace");
        this.apiDataAPIClient = apiDataAPIClient;
        this.namespaceName = namespace;
        this.namespaceResource = (node) -> apiDataAPIClient.lookup().apply(node) + "/" + namespace;
    }

    // ------------------------------------------
    // ----   General Informations           ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public String getNamespaceName() {
        return namespaceName;
    }

    /** {@inheritDoc} */
    @Override
    public DatabaseAdmin getClient() {
        return apiDataAPIClient;
    }

    /** {@inheritDoc} */
    @Override
    public void drop() {
        apiDataAPIClient.dropNamespace(this.getNamespaceName());
    }

    // ------------------------------------------
    // ----     Collection CRUD              ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public Stream<String> listCollectionNames() {

        Command findCollections = Command
                .create("findCollections");

        return runCommand(findCollections)
                .getStatusKeyAsList("collections", String.class)
                .stream();
    }

    /** {@inheritDoc} */
    @Override
    public <DOC> Collection<DOC> getCollection(String collectionName, @NonNull Class<DOC> documentClass) {
        hasLength(collectionName, "collectionName");
        notNull(documentClass, "documentClass");
        return new CollectionImpl<>(this, collectionName, documentClass);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<CollectionDefinition> listCollections() {
        Command findCollections = Command
                .create("findCollections")
                .withOptions(new Document().append("explain", true));

        return runCommand(findCollections)
                .getStatusKeyAsList("collections", CollectionDefinition.class)
                .stream();
    }

    /** {@inheritDoc} */
    @Override
    public <DOC> Collection<DOC> createCollection(String collectionName, CollectionOptions collectionOptions, Class<DOC> documentClass) {
        hasLength(collectionName, "collectionName");
        notNull(documentClass, "documentClass");
        Command createCollection = Command
                .create("createCollection")
                .append("name", collectionName)
                .withOptions(JsonUtils.convertValueForDataApi(collectionOptions, Document.class));
        runCommand(createCollection);
        log.info("Collection  '" + green("{}") + "' has been created", collectionName);
        return getCollection(collectionName, documentClass);
    }

    /** {@inheritDoc} */
    @Override
    public void dropCollection(String collectionName) {
        runCommand(Command
                .create("deleteCollection")
                .append("name", collectionName));
        log.info("Collection  '" + green("{}") + "' has been deleted", collectionName);
    }

    // ------------------------------------------
    // ----           Lookup                 ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public Function<ServiceHttp, String> lookup() {
        return namespaceResource;
    }

    /** {@inheritDoc} */
    @Override
    public LoadBalancedHttpClient getHttpClient() {
        return getClient().getHttpClient();
    }

}
