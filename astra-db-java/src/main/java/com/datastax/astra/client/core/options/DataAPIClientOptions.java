package com.datastax.astra.client.core.options;

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

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.auth.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.http.Caller;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import com.datastax.astra.internal.serdes.DatabaseSerializer;
import com.dtsx.astra.sdk.utils.Assert;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.datastax.astra.client.DataAPIDestination.ASTRA;
import static com.datastax.astra.client.DataAPIDestination.ASTRA_DEV;
import static com.datastax.astra.client.DataAPIDestination.ASTRA_TEST;

/**
 * Options to set up the client for DataApiClient.
 */
@Setter
@Slf4j
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class DataAPIClientOptions implements Cloneable {

    /**
     * Feature Flag Tables.
     */
    public static final String HEADER_FEATURE_FLAG_TABLES = "Feature-Flag-tables";

    /**
     * path for json api.
     */
    public static final String DEFAULT_VERSION = "v1";

    /**
     * Number of documents for a count.
     */
    public static final int DEFAULT_MAX_COUNT = 1000;

    /**
     * Maximum number of documents when you insert.
     */
    public static final int DEFAULT_MAX_CHUNK_SIZE = 50;

    /**
     * Set the API version like 'v1'
     */
    private String apiVersion = DEFAULT_VERSION;

    /**
     * Encode the destination like Astra or local installation.
     */
    private DataAPIDestination destination = DataAPIDestination.ASTRA;

    /**
     * The embedding service API key can be provided at top level.
     */
    private EmbeddingHeadersProvider embeddingAuthProvider;

    /**
     * Add headers to admin calls.
     */
    private Map<String, String> databaseAdditionalHeaders = new HashMap<>();

    /**
     * Add headers to admin calls.
     */
    private Map<String, String> adminAdditionalHeaders = new HashMap<>();

    /**
     * Observers for the commands (logging or metrics - emitter pattern).
     */
    private Map<String, CommandObserver> observers = new TreeMap<>();

    /**
     * Http Client Options
     */
    private HttpClientOptions httpClientOptions = new HttpClientOptions();

    /**
     * Timeout options.
     */
    private TimeoutOptions timeoutOptions = new TimeoutOptions();

    /**
     * Options for serialization and deserialization.
     */
    private static SerdesOptions serdesOptions = new SerdesOptions();

    // --------------------------------------------
    // ----------------- GETTERS  -----------------
    // --------------------------------------------

    /**
     * Check if the deploying is Astra
     *
     * @return true if the destination is Astra
     */
    public boolean isAstra() {
        return getDestination() == ASTRA ||
                getDestination() == ASTRA_DEV ||
                getDestination() == ASTRA_TEST;
    }

    /**
     * Find the Astra Environment from the destination provided in the initial Optional. It will help
     * shaping the Api endpoint to spawn sub database objects.
     *
     * @return astra environment if found
     */
    public AstraEnvironment getAstraEnvironment() {
        if (getDestination() != null) {
            switch (getDestination()) {
                case ASTRA:
                    return AstraEnvironment.PROD;
                case ASTRA_DEV:
                    return AstraEnvironment.DEV;
                case ASTRA_TEST:
                    return AstraEnvironment.TEST;
            }
        }
        return null;
    }

    /**
     * Gets apiVersion
     *
     * @return value of apiVersion
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets destination
     *
     * @return value of destination
     */
    public DataAPIDestination getDestination() {
        return destination;
    }

    /**
     * Gets embeddingAuthProvider
     *
     * @return value of embeddingAuthProvider
     */
    public EmbeddingHeadersProvider getEmbeddingAuthProvider() {
        return embeddingAuthProvider;
    }

    /**
     * Gets databaseAdditionalHeaders
     *
     * @return value of databaseAdditionalHeaders
     */
    public Map<String, String> getDatabaseAdditionalHeaders() {
        return databaseAdditionalHeaders;
    }

    /**
     * Gets adminAdditionalHeaders
     *
     * @return value of adminAdditionalHeaders
     */
    public Map<String, String> getAdminAdditionalHeaders() {
        return adminAdditionalHeaders;
    }

    /**
     * Gets observers
     *
     * @return value of observers
     */
    public Map<String, CommandObserver> getObservers() {
        return observers;
    }

    /**
     * Gets httpClientOptions
     *
     * @return value of httpClientOptions
     */
    public HttpClientOptions getHttpClientOptions() {
        return httpClientOptions;
    }

    /**
     * Gets timeoutOptions
     *
     * @return value of timeoutOptions
     */
    public TimeoutOptions getTimeoutOptions() {
        return timeoutOptions;
    }

    /**
     * Gets serdesOptions
     *
     * @return value of serdesOptions
     */
    public static SerdesOptions getSerdesOptions() {
        return serdesOptions;
    }

    // --------------------------------------------
    // ------- Special Fluent Setters  ------------
    // --------------------------------------------

    /**
     * Register an observer with its className.
     *
     * @param observer command observer
     * @return instance of the command options
     */
    public DataAPIClientOptions addObserver(String name, CommandObserver observer) {
        this.observers.put(name, observer);
        return this;
    }

    /**
     * Register an observer with its className.
     *
     * @param observer command observer
     * @return instance of the command options
     */
    public DataAPIClientOptions addObserver(CommandObserver observer) {
        return addObserver(observer.getClass().getSimpleName(), observer);
    }

    /**
     * Help to enable loggin requests.
     *
     * @return current reference
     */
    public DataAPIClientOptions logRequests() {
        return addObserver(new LoggingCommandObserver(DataAPIClient.class));
    }

    // --------------------------------------------
    // ----------------- HEADERS  -----------------
    // --------------------------------------------

    /**
     * Builder pattern, update http connection Timeout
     *
     * @param embeddingAPIKey embedding API Key
     * @return self reference
     */
    public DataAPIClientOptions embeddingAPIKey(String embeddingAPIKey) {
        return embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(embeddingAPIKey));
    }

    /**
     * Builder pattern, update caller information.
     * o
     *
     * @param name    caller name in the user agent
     * @param version caller version in the user agent
     * @return self reference
     */
    public DataAPIClientOptions addCaller(String name, String version) {
        this.httpClientOptions.addCaller(new Caller(name, version));
        return this;
    }

    /**
     * Add a header to the db calls.
     *
     * @param key   key
     * @param value value
     * @return self reference
     */
    public DataAPIClientOptions addDatabaseAdditionalHeader(String key, String value) {
        this.databaseAdditionalHeaders.put(key, value);
        return this;
    }


    /**
     * Add a header to the admin calls.
     *
     * @param key   key
     * @param value value
     * @return self reference
     */
    public DataAPIClientOptions addAdminAdditionalHeader(String key, String value) {
        adminAdditionalHeaders.put(key, value);
        return this;
    }

    public DataAPIClientOptions enableFeatureFlagTables() {
        return addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true");
    }

    public DataAPIClientOptions disableFeatureFlagTables() {
        return addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, null);
    }

    // --------------------------------------------
    // ---------- JAVA DEFAULTS   -----------------
    // --------------------------------------------

    @Override
    public String toString() {
        return new DatabaseSerializer().marshall(this);
    }

    public DataAPIClientOptions(DataAPIClientOptions options) {
        Assert.notNull(options, "Options");
        this.apiVersion                 = options.apiVersion;
        this.destination                = options.destination;
        this.embeddingAuthProvider      = options.embeddingAuthProvider;
        // Deep Copy
        this.databaseAdditionalHeaders  = options.databaseAdditionalHeaders != null ?
                new HashMap<>(options.databaseAdditionalHeaders) : null;
        this.adminAdditionalHeaders     = options.adminAdditionalHeaders != null ?
                new HashMap<>(options.adminAdditionalHeaders) : null;
        this.observers                  = options.observers != null ?
                new HashMap<>(options.observers) : null;
        // Clones
        this.httpClientOptions          = options.httpClientOptions != null ?
                options.httpClientOptions.clone() : null;
        this.timeoutOptions             = options.timeoutOptions != null ?
                options.timeoutOptions.clone() : null;
        this.serdesOptions              = options.serdesOptions != null ?
                options.serdesOptions.clone() : null;
    }

    @Override
    public DataAPIClientOptions clone() {
        return new DataAPIClientOptions(this);
    }

}
