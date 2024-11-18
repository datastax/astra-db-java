package com.datastax.astra.client.core.commands;

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

import com.datastax.astra.client.core.auth.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.internal.command.CommandObserver;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.HEADER_FEATURE_FLAG_TABLES;

/**
 * Options that will be provided to all commands for this collection.
 *
 * @param <T>
 *     the sub-class implementing the command options
 */
@Getter
public class CommandOptions<T extends CommandOptions<T>> implements Cloneable {

    /**
     * Default command type is DATA
     */
    protected CommandType commandType = CommandType.DATA;

    /**
     * List of observers to notify.
     */
    protected Map<String, CommandObserver> observers = new LinkedHashMap<>();

    /**
     * Token to use for authentication.
     */
    protected String token;

    /**
     * Will be used to create a client
     */
    protected HttpClientOptions httpClientOptions;

    /**
     * Will be used to create a client
     */
    protected TimeoutOptions timeoutOptions;

    /**
     * Embedding auth provider
     */
    protected EmbeddingHeadersProvider embeddingAuthProvider;

    /** Add headers to db calls. */
    protected Map<String, String> databaseAdditionalHeaders = new HashMap<>();

    /** Add headers to admin calls. */
    protected Map<String, String> adminAdditionalHeaders  = new HashMap<>();

    /**
     * Provide the token.
     *
     * @param token
     *      authentication token
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T token(String token) {
        this.token = token;
        return (T) this;
    }

    /**
     * Provide the token.
     *
     * @param params
     *      additional headers
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T databaseAdditionalHeaders(Map<String, String> params) {
        this.adminAdditionalHeaders = params;
        return (T) this;
    }

    /**
     * Provide the token.
     *
     * @param params
     *      additional headers
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T adminAdditionalHeaders(Map<String, String> params) {
        this.adminAdditionalHeaders = params;
        return (T) this;
    }

    /**
     * Provide the token.
     *
     * @param options
     *      options to initialize the http client
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T httpClientOptions(HttpClientOptions options) {
        this.httpClientOptions = options;
        return (T) this;
    }

    /**
     * Provide the embedding service API key.
     *
     * @param embeddingAuthProvider
     *      authentication provider
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T embeddingAuthProvider(EmbeddingHeadersProvider embeddingAuthProvider) {
        this.embeddingAuthProvider = embeddingAuthProvider;
        return (T) this;
    }

    /**
     * Provide the command type if default is not DATA.
     *
     * @param commandType
     *      command Type
     * @return
     *      service key
     */
    public T commandType(CommandType commandType) {
        this.commandType = commandType;
        return (T) this;
    }

    /**
     * Provide the embedding service API key.
     *
     * @param timeoutOptions
     *      options of timeouts
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T timeoutOptions(TimeoutOptions timeoutOptions) {
        this.timeoutOptions = timeoutOptions;
        return (T) this;
    }

    /**
     * Allow to register a listener for the command.
     * @param name
     *      name of the observer
     * @param observer
     *     observer to register
     * @return
     *    instance of the command options
     */
    @SuppressWarnings("unchecked")
    public T registerObserver(String name, CommandObserver observer) {
        if (observer != null) {
            observers.put(name, observer);
        }
        return (T) this;
    }

    /**
     * Register an observer with its className.
     *
     * @param observer
     *      command observer
     * @return
     *      instance of the command options
     */
    public T registerObserver(CommandObserver observer) {
        return registerObserver(observer.getClass().getSimpleName(), observer);
    }

    /**
     * Remove a listener from the command.
     *
     * @param name
     *      name of the observer
     * @return
     *      instance of the command options
     */
    @SuppressWarnings("unchecked")
    public T unregisterObserver(String name) {
        observers.remove(name);
        return (T) this;
    }

    /**
     * Remove an observer by its class.
     *
     * @param observer
     *      observer to remove
     * @return
     *      instance of the command options
     */
    public T unregisterObserver(Class<CommandObserver> observer) {
        return unregisterObserver(observer.getSimpleName());
    }

    /**
     * Return the HTTP Request Timeout based on the command type
     *
     * @return value of token
     */
    public long selectRequestTimeout() {
        return selectRequestTimeout(commandType, timeoutOptions);
    }

    /**
     * Return the HTTP Request Timeout based on the command type
     *
     * @return value of token
     */
    public static long selectRequestTimeout(CommandType type, TimeoutOptions timeoutOptions) {
        return switch (type) {
            case DATABASE_ADMIN -> timeoutOptions.databaseAdminTimeoutMillis();
            case KEYSPACE_ADMIN -> timeoutOptions.keyspaceAdminTimeoutMillis();
            case SCHEMA -> timeoutOptions.schemaOperationTimeoutMillis();
            default -> timeoutOptions.requestTimeoutMillis();
        };
    }

    /**
     * Add a header to the db calls.
     *
     * @param key
     *      key
     * @param value
     *      value
     * @return
     *      self reference
     */
    @SuppressWarnings("unchecked")
    public T addDatabaseAdditionalHeader(String key, String value) {
        databaseAdditionalHeaders.put(key, value);
        return (T) this;
    }

    public T enableFeatureFlagTables() {
        return addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true");
    }

    public T disableFeatureFlagTables() {
        return addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, null);
    }

    /**
     * Add a header to the admin calls.
     *
     * @param key
     *      key
     * @param value
     *      value
     * @return
     *      self reference
     */
    @SuppressWarnings("unchecked")
    public T addAdminAdditionalHeader(String key, String value) {
        adminAdditionalHeaders.put(key, value);
        return (T) this;
    }

    /**
     * Default Constructor.
     */
    public CommandOptions() {
        // left blank, jackson serialization
    }

    /**
     * Default Constructor.
     */
    public CommandOptions(DataAPIClientOptions options) {
       if (options != null) {
           this.embeddingAuthProvider     = options.getEmbeddingAuthProvider();
           this.adminAdditionalHeaders    = options.getAdminAdditionalHeaders();
           this.databaseAdditionalHeaders = options.getDatabaseAdditionalHeaders();
           this.httpClientOptions         = options.getHttpClientOptions();
           this.timeoutOptions            = options.getTimeoutOptions();
           for(Map.Entry<String, CommandObserver> entry : options.getObservers().entrySet()) {
               // Avoid observers Duplication
               if (!getObservers().containsKey(entry.getKey())) {
                   registerObserver(entry.getKey(), entry.getValue());
               }
           }
       }
    }

    @SuppressWarnings("unchecked")
    public T clone() {
        try {
            CommandOptions<T> cloned = (CommandOptions<T>) super.clone();

            // Deep copy of mutable fields
            cloned.observers = new LinkedHashMap<>(this.observers);
            cloned.databaseAdditionalHeaders = new HashMap<>(this.databaseAdditionalHeaders);
            cloned.adminAdditionalHeaders = new HashMap<>(this.adminAdditionalHeaders);

            // Copying other objects (assuming they have proper copy constructors or are immutable)
            cloned.httpClientOptions = this.httpClientOptions != null ? this.httpClientOptions.clone() : null;
            cloned.timeoutOptions = this.timeoutOptions != null ? this.timeoutOptions.clone() : null;
            cloned.embeddingAuthProvider = this.embeddingAuthProvider != null ? this.embeddingAuthProvider.copy() : null;

            return (T) cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

}
