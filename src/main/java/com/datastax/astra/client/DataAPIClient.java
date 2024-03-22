package com.datastax.astra.client;

import com.datastax.astra.internal.astra.AstraApiEndpoint;
import com.datastax.astra.internal.utils.Assert;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.AstraDBAdmin.DEFAULT_NAMESPACE;

public class DataAPIClient {

    /** Token in use with the solution. */
    private final String token;

    /** Options to setup the client. */
    private final DataAPIOptions options;

    public DataAPIClient(String token) {
        this(token, DataAPIOptions.builder().build());
    }

    public DataAPIClient(String token, DataAPIOptions options) {
        Assert.hasLength(token, "token");
        Assert.notNull(options, "options");
        this.token   = token;
        this.options = options;
    }

    // --------------------------------------------------
    // ---       Access AstraDBAdmin                  ---
    // --------------------------------------------------

    public AstraDBAdmin getAdmin() {
        return getAdmin(this.token);
    }

    public AstraDBAdmin getAdmin(String superUserToken) {
       return new AstraDBAdmin(superUserToken, getAstraEnvironment(), options);
    }

    private Optional<AstraEnvironment> findAstraEnvironment() {
        if (options.getDestination() != null) {
            switch (options.getDestination()) {
                case ASTRA:
                    return Optional.of(AstraEnvironment.PROD);
                case ASTRA_DEV:
                    return Optional.of(AstraEnvironment.DEV);
                case ASTRA_TEST:
                    return Optional.of(AstraEnvironment.TEST);
            }
        }
        return Optional.empty();
    }

    private AstraEnvironment getAstraEnvironment() {
        return findAstraEnvironment()
                .orElseThrow(() -> new IllegalArgumentException("'destination' should be ASTRA* to use the AstraDBAdmin"));
    }

    // --------------------------------------------------
    // ---       Access Database                      ---
    // --------------------------------------------------

    public Database getDatabase(UUID databaseId, String namespace) {
        Assert.notNull(databaseId, "databaseId");
        Assert.hasLength(namespace, "namespace");
        return new Database(new AstraApiEndpoint(databaseId,
                getAdmin().getDatabaseInformations(databaseId).getInfo().getRegion(),
                getAstraEnvironment()).getApiEndPoint(),
                namespace);
    }

    public Database getDatabase(UUID databaseId) {
        return getDatabase(databaseId, DEFAULT_NAMESPACE);
    }

    public Database getDatabase(String apiEndpoint, String namespace) {
        return new Database(apiEndpoint, token, namespace, options);
    }

    public Database getDatabase(String apiEndpoint) {
        return getDatabase(apiEndpoint, DEFAULT_NAMESPACE);
    }




}
