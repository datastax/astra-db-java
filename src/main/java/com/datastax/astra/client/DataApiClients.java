package com.datastax.astra.client;

import com.datastax.astra.internal.DataAPIDatabaseAdmin;
import com.datastax.astra.internal.auth.FixedTokenAuthenticationService;
import com.datastax.astra.internal.auth.StargateAuthenticationService;
import com.datastax.astra.internal.auth.TokenProvider;
import com.datastax.astra.internal.http.HttpClientOptions;

import com.datastax.astra.internal.utils.Assert;
import lombok.NonNull;

import java.util.Collections;

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
    public static DataAPIClient localStargate() {
        return new DataAPIClient(
                new StargateAuthenticationService().getToken(),
                DataAPIClientOptions.builder().withDestination(DataAPIDestination.CASSANDRA).build());
    }

    public static DataAPIClient astra(String token) {
        return new DataAPIClient(token, DataAPIClientOptions
                .builder()
                .withDestination(DataAPIDestination.ASTRA)
                .build());
    }

    public static DataAPIClient astraDev(String token) {
        return new DataAPIClient(token, DataAPIClientOptions
                .builder()
                .withDestination(DataAPIDestination.ASTRA_DEV)
                .build());
    }

    public static DataAPIClient astraTest(String token) {
        return new DataAPIClient(token, DataAPIClientOptions
                .builder()
                .withDestination(DataAPIDestination.ASTRA_TEST)
                .build());
    }


}
