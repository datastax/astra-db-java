package com.datastax.astra.client;


import io.stargate.sdk.ServiceDatacenter;
import io.stargate.sdk.ServiceDeployment;
import io.stargate.sdk.auth.FixedTokenAuthenticationService;
import io.stargate.sdk.auth.StargateAuthenticationService;
import io.stargate.sdk.auth.TokenProvider;
import io.stargate.sdk.data.internal.DataAPIDatabaseAdmin;
import io.stargate.sdk.http.HttpClientOptions;
import io.stargate.sdk.http.ServiceHttp;
import io.stargate.sdk.utils.Assert;
import lombok.NonNull;

import java.util.Collections;

import static io.stargate.sdk.utils.Assert.hasLength;
import static io.stargate.sdk.utils.Assert.notNull;

/**
 * Initialization of the client in a Static way.
 */
public class DataApiClients {

    /** Default endpoint. */
    public static final String DEFAULT_ENDPOINT = "http://localhost:8181";

    /** Default endpoint. */
    public static final String PATH_HEALTH_CHECK = "/stargate/health";

    /** Default service id. */
    public static final String DEFAULT_SERVICE_ID = "sgv2-json";

    /** Default datacenter id. */
    private static final String DEFAULT_DATACENTER = "dc1";

    /**
     * Utility class, should not be instanced.
     */
    private DataApiClients() {}

    /**
     * Create from an Endpoint only
     */
    public static io.stargate.sdk.data.client.DatabaseAdmin create() {
        return create(
                buildServiceDeployment(DEFAULT_ENDPOINT, new StargateAuthenticationService()),
                HttpClientOptions.builder().build());
    }

    /**
     * Create from an Endpoint only
     *
     * @param endpoint
     *      service endpoint
     * @param token
     *      token
     */
    public static io.stargate.sdk.data.client.DatabaseAdmin create(@NonNull String endpoint, @NonNull String token) {
        return create(endpoint, token, HttpClientOptions.builder().build());
    }

    /**
     * Create from an Endpoint only
     *
     * @param endpoint
     *      service endpoint
     * @param token
     *      token
     */
    public static io.stargate.sdk.data.client.DatabaseAdmin create(@NonNull String endpoint, @NonNull String token, HttpClientOptions httpClientOptions) {
        Assert.notNull(endpoint, "endpoint");
        Assert.notNull(token, "token");
        Assert.notNull(httpClientOptions, "httpClientOptions");
        return new DataAPIDatabaseAdmin(buildServiceDeployment(endpoint, new FixedTokenAuthenticationService(token)),  httpClientOptions);
    }

    /**
     * Build the Stargate Service Deployment (DC / Services).
     * @param endpoint
     *      http endpoint
     * @param tokenProvider
     *      token provider (fixed or stargate)
     * @return
     *      service deployment
     */
    private static ServiceDeployment<ServiceHttp> buildServiceDeployment(String endpoint, TokenProvider tokenProvider) {
        hasLength(endpoint, "stargate endpoint");
        notNull(tokenProvider, "tokenProvider");
        // Single instance running
        ServiceHttp rest =
                new ServiceHttp(DEFAULT_SERVICE_ID, endpoint, endpoint + PATH_HEALTH_CHECK);
        // DC with default auth and single node
        ServiceDatacenter<ServiceHttp> sDc =
                new ServiceDatacenter<>(DEFAULT_DATACENTER, tokenProvider, Collections.singletonList(rest));
        // Deployment with a single dc
        return new ServiceDeployment<ServiceHttp>().addDatacenter(sDc);
    }

    /**
     * Initialized document API with a URL and a token.
     *
     * @param serviceDeployment
     *      http client topology aware
     */
    public static io.stargate.sdk.data.client.DatabaseAdmin create(ServiceDeployment<ServiceHttp> serviceDeployment, HttpClientOptions clientOptions) {
        return new DataAPIDatabaseAdmin(serviceDeployment, clientOptions);
    }

}
