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
import com.datastax.astra.internal.serdes.DataAPISerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@Setter
@NoArgsConstructor
@Accessors(fluent = true, chain = true)
public class CommandOptions<T extends CommandOptions<T>> implements Cloneable {

    /**
     * Token to use for authentication.
     */
    protected String token;

    /**
     * The command type will drive the timeout in used.
     */
    protected CommandType commandType = CommandType.GENERAL_METHOD;

    /**
     * Token used
     */
    protected DataAPIClientOptions dataAPIClientOptions;

    /**
     * Serializer for the command.
     */
    protected DataAPISerializer serializer;

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
     * @param serializer
     *      serializer
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T serializer(DataAPISerializer serializer) {
        this.serializer = serializer;
        return (T) this;
    }

    /**
     * Provide the command type. The nature of the command will determine the timeout.
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

    // --------------------------------------------
    // ----- OVERRIDE DATA API CLIENT OPTIONS -----
    // --------------------------------------------

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
        this.dataAPIClientOptions.embeddingAuthProvider(embeddingAuthProvider);
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
        this.dataAPIClientOptions.databaseAdditionalHeaders(params);
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
        this.dataAPIClientOptions.adminAdditionalHeaders(params);
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
        this.dataAPIClientOptions.httpClientOptions(options);
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
        this.dataAPIClientOptions.timeoutOptions(timeoutOptions);
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
            this.dataAPIClientOptions.addObserver(name, observer);
        }
        return (T) this;
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
        this.dataAPIClientOptions.getObservers().remove(name);
        return (T) this;
    }

    /**
     * Return the HTTP Request Timeout based on the command type
     *
     * @return value of token
     */
    public long getTimeout() {
        return getTimeout(dataAPIClientOptions.getTimeoutOptions(), commandType);
    }

    /**
     * Request timeout based on the command type.
     *
     * @param timeoutOptions
     *      options of timeouts
     * @param type
     *      command type
     * @return
     *      timeout
     */
    public static long getTimeout(TimeoutOptions timeoutOptions, CommandType type) {
        return switch (type) {
            case DATABASE_ADMIN -> timeoutOptions.getDatabaseAdminTimeoutMillis();
            case KEYSPACE_ADMIN -> timeoutOptions.getKeyspaceAdminTimeoutMillis();
            case TABLE_ADMIN -> timeoutOptions.getTableAdminTimeoutMillis();
            case COLLECTION_ADMIN -> timeoutOptions.getCollectionAdminTimeoutMillis();
            default -> timeoutOptions.getGeneralMethodTimeoutMillis();
        };
    }

    /**
     * Provide the command type. The nature of the command will determine the timeout.
     *
     * @param timeoutMillis
     *      timeout for the request
     * @return
     *      service key
     */
    public T timeout(long timeoutMillis) {
        TimeoutOptions timeoutOptions = dataAPIClientOptions.getTimeoutOptions();
        switch (commandType) {
            case DATABASE_ADMIN -> timeoutOptions.databaseAdminTimeoutMillis(timeoutMillis);
            case KEYSPACE_ADMIN -> timeoutOptions.keyspaceAdminTimeoutMillis(timeoutMillis);
            case TABLE_ADMIN -> timeoutOptions.tableAdminTimeoutMillis(timeoutMillis);
            case COLLECTION_ADMIN -> timeoutOptions.collectionAdminTimeoutMillis(timeoutMillis);
            default -> timeoutOptions.generalMethodTimeoutMillis(timeoutMillis);
        };
        return (T) this;
    }

    /**
     * Gets token
     *
     * @return value of token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets commandType
     *
     * @return value of commandType
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Gets dataAPIClientOptions
     *
     * @return value of dataAPIClientOptions
     */
    public DataAPIClientOptions getDataAPIClientOptions() {
        return dataAPIClientOptions;
    }

    /**
     * Gets serializer
     *
     * @return value of serializer
     */
    public DataAPISerializer getSerializer() {
        return serializer;
    }

    /**
     * Return the HTTP Request Timeout based on the command type.
     *
     * @param token
     *      authentication token
     * @param type
     *      command type
     * @param options
     *      data api options
     */
    public CommandOptions(String token, CommandType type, DataAPIClientOptions options) {
       this.token = token;
       this.commandType = type;
       if (options != null) {
           this.dataAPIClientOptions = options.clone();
       }
    }

    @SuppressWarnings("unchecked")
    public T clone() {
        try {
            CommandOptions<T> cloned = (CommandOptions<T>) super.clone();
            cloned.token = token;
            cloned.commandType = commandType;
            cloned.dataAPIClientOptions = dataAPIClientOptions.clone();
            return (T) cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

}
