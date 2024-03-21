package com.datastax.astra.internal;

import io.stargate.sdk.ServiceDeployment;
import io.stargate.sdk.data.client.Database;
import io.stargate.sdk.data.client.model.Command;
import io.stargate.sdk.data.client.model.namespaces.CreateNamespaceOptions;
import io.stargate.sdk.data.client.model.namespaces.NamespaceInformation;
import io.stargate.sdk.http.HttpClientOptions;
import io.stargate.sdk.http.LoadBalancedHttpClient;
import io.stargate.sdk.http.ServiceHttp;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.stream.Stream;

import static io.stargate.sdk.utils.AnsiUtils.green;
import static io.stargate.sdk.utils.Assert.hasLength;
import static io.stargate.sdk.utils.Assert.notNull;

/**
 * Implementation of Client.
 */
@Slf4j
@Getter
public class DataAPIDatabaseAdmin extends AbstractCommandRunner implements io.stargate.sdk.data.client.DatabaseAdmin {

    /** Function to compute the root. */
    public final Function<ServiceHttp, String> rootResource;

    /** Get Topology of the nodes. */
    protected final LoadBalancedHttpClient stargateHttpClient;

    /** Version of the API. */
    protected final HttpClientOptions options;

    /**
     * Initialized document API with a URL and a token.
     *
     * @param serviceDeployment
     *      http client topology aware
     * @param httpClientOptions
     *      option for the client
     */
    public DataAPIDatabaseAdmin(ServiceDeployment<ServiceHttp> serviceDeployment, HttpClientOptions httpClientOptions) {
        notNull(serviceDeployment, "service deployment topology");
        this.stargateHttpClient = new LoadBalancedHttpClient(serviceDeployment, httpClientOptions);
        this.options             = httpClientOptions;
        this.rootResource        = (node) -> node.getEndpoint() +  "/" + httpClientOptions.getApiVersion();
    }

    // ------------------------------------------
    // ----           Lookup                 ----
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public Function<ServiceHttp, String> lookup() {
        return rootResource;
    }

    /** {@inheritDoc} */
    @Override
    public LoadBalancedHttpClient getHttpClient() {
        return stargateHttpClient;
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
    public Stream<NamespaceInformation> listNamespaces() {
        return listNamespaceNames().map(NamespaceInformation::new);
    }

    /** {@inheritDoc} */
    @Override
    public Database getNamespace(String namespaceName) {
        return new DatabaseImpl(this, namespaceName);
    }

    /** {@inheritDoc} */
    @Override
    public Database createNamespace(String namespace, CreateNamespaceOptions options) {
        hasLength(namespace, "namespace");
        notNull(options, "options");
        Command createNamespace = Command
                        .create("createNamespace")
                        .append("name", namespace)
                        .withOptions(options);
        runCommand(createNamespace);
        log.info("Namespace  '" + green("{}") + "' has been created", namespace);
        return new DatabaseImpl(this, namespace);
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

}
