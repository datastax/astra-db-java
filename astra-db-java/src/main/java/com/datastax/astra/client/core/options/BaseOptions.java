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

import com.datastax.astra.client.core.headers.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.commands.CommandType;
import com.datastax.astra.client.core.headers.RerankingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.headers.RerankingHeadersProvider;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.utils.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

/**
 * Options that will be provided to all commands for this collection.
 *
 * @param <T>
 *     the sub-class implementing the command options
 */
@NoArgsConstructor
public class BaseOptions<T extends BaseOptions<T>> implements Cloneable {

    /**
     * Token used
     */
    protected DataAPIClientOptions dataAPIClientOptions;

    /**
     * Serializer for the command.
     */
    protected DataAPISerializer serializer;

    /**
     * Token to use for authentication.
     */
    protected String token;

    /**
     * The command type will drive the timeout in used.
     */
    protected CommandType commandType = CommandType.GENERAL_METHOD;

    // --------------------------------------------
    // ----- Setters (Fluent)                 -----
    // --------------------------------------------

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

    /**
     * Provide a fluent setter for the data API Client.
     *
     * @param options
     *      command Type
     * @return
     *      service key
     */
    public T dataAPIClientOptions(DataAPIClientOptions options) {
        this.dataAPIClientOptions = options;
        return (T) this;
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
        return timeout(timeoutMillis, getCommandType());
    }

    /**
     * Provide the command type. The nature of the command will determine the timeout.
     *
     * @param duration
     *      timeout for the request
     * @return
     *      service key
     */
    public T timeout(Duration duration) {
        Assert.notNull(duration, "duration");
        return timeout(duration.toMillis(), getCommandType());
    }

    // --------------------------------------------
    // ----- Setters                          -----
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
        Assert.notNull(embeddingAuthProvider, "embeddingAuthProvider");
        getDataAPIClientOptions().embeddingHeadersProvider(embeddingAuthProvider);
        return (T) this;
    }

    /**
     * Provide the reranking service API key.
     *
     * @param rerankingHeadersProvider
     *      authentication provider
     * @return
     *      service key
     */
    public T rerankingAuthProvider(RerankingHeadersProvider rerankingHeadersProvider) {
        Assert.notNull(rerankingHeadersProvider, "rerankHeadersProvider");
        getDataAPIClientOptions().rerankingHeadersProvider(rerankingHeadersProvider);
        return (T) this;
    }

    /**
     * Provide the embedding service API key.
     *
     * @param apiKey
     *      target api key
     * @return
     *      service key
     */
    public T embeddingApiKey(String apiKey) {
        Assert.hasLength(apiKey, "apiKey");
        return embeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(apiKey));
    }

    /**
     * Provide the reranking service API key.
     *
     * @param apiKey
     *      target api key
     * @return
     *      service key
     */
    public T rerankingApiKey(String apiKey) {
        Assert.hasLength(apiKey, "apiKey");
        return rerankingAuthProvider(new RerankingAPIKeyHeaderProvider(apiKey));
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
        if (params!=null && !params.isEmpty()) {
            getDataAPIClientOptions().databaseAdditionalHeaders(params);
        }
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
        if (params!=null && !params.isEmpty()) {
            getDataAPIClientOptions().adminAdditionalHeaders(params);
        }
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
        getDataAPIClientOptions().httpClientOptions(options);
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
        Assert.notNull(timeoutOptions, "timeoutOptions");
        getDataAPIClientOptions().timeoutOptions(timeoutOptions);
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
        Assert.hasLength(name, "name");
        Assert.notNull(observer, "observer");
        getDataAPIClientOptions().addObserver(name, observer);
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
        getDataAPIClientOptions().getObservers().remove(name);
        return (T) this;
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
    public long getTimeout(TimeoutOptions timeoutOptions, CommandType type) {
        return switch (type) {
            case DATABASE_ADMIN -> timeoutOptions.getDatabaseAdminTimeoutMillis();
            case KEYSPACE_ADMIN -> timeoutOptions.getKeyspaceAdminTimeoutMillis();
            case TABLE_ADMIN -> timeoutOptions.getTableAdminTimeoutMillis();
            case COLLECTION_ADMIN -> timeoutOptions.getCollectionAdminTimeoutMillis();
            default -> timeoutOptions.getGeneralMethodTimeoutMillis();
        };
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
    public long getRequestTimeout(TimeoutOptions timeoutOptions, CommandType type) {
        return switch (type) {
            case DATABASE_ADMIN ->   timeoutOptions.getDatabaseAdminTimeoutMillis();
            case KEYSPACE_ADMIN ->   timeoutOptions.getKeyspaceAdminTimeoutMillis();
            case TABLE_ADMIN ->      timeoutOptions.getTableAdminTimeoutMillis();
            case COLLECTION_ADMIN -> timeoutOptions.getCollectionAdminTimeoutMillis();
            case GENERAL_METHOD ->   timeoutOptions.getGeneralMethodTimeoutMillis();
        };
    }

    /**
     * Provide the command type. The nature of the command will determine the timeout.
     *
     * @param timeoutMillis
     *      timeout for the request
     * @param commandType
     *      command type
     * @return
     *      service key
     */
    public T timeout(long timeoutMillis, CommandType commandType) {
        if (getDataAPIClientOptions().getTimeoutOptions() == null) {
            getDataAPIClientOptions().timeoutOptions(new TimeoutOptions());
        }
        TimeoutOptions timeoutOptions = getDataAPIClientOptions().getTimeoutOptions();
        switch (commandType) {
            case DATABASE_ADMIN -> timeoutOptions.databaseAdminTimeoutMillis(timeoutMillis);
            case KEYSPACE_ADMIN -> timeoutOptions.keyspaceAdminTimeoutMillis(timeoutMillis);
            case TABLE_ADMIN -> timeoutOptions.tableAdminTimeoutMillis(timeoutMillis);
            case COLLECTION_ADMIN -> timeoutOptions.collectionAdminTimeoutMillis(timeoutMillis);
            default -> timeoutOptions.generalMethodTimeoutMillis(timeoutMillis);
        };
        return (T) this;
    }

    // --------------------------------------------
    // ----- Getters                          -----
    // --------------------------------------------

    /**
     * Gets dataAPIClientOptions
     *
     * @return value of dataAPIClientOptions
     */
    @JsonIgnore
    public DataAPIClientOptions getDataAPIClientOptions() {
        if (this.dataAPIClientOptions == null) {
            this.dataAPIClientOptions = new DataAPIClientOptions();
        }
        return dataAPIClientOptions;
    }

    /**
     * Return the HTTP Request Timeout based on the command type
     *
     * @return value of token
     */
    @JsonIgnore
    public long getTimeout() {
        if (getDataAPIClientOptions() != null && getDataAPIClientOptions().getTimeoutOptions() != null) {
            return getTimeout(getDataAPIClientOptions().getTimeoutOptions(), getCommandType());
        }
        return -1;
    }

    /**
     * Return the HTTP Request Timeout based on the command type
     *
     * @return value of token
     */
    @JsonIgnore
    public long getRequestTimeout() {
        if (getDataAPIClientOptions() != null && getDataAPIClientOptions().getTimeoutOptions() != null) {
            return getRequestTimeout(getDataAPIClientOptions().getTimeoutOptions(), getCommandType());
        }
        return -1;
    }

    /**
     * Gets token
     *
     * @return value of token
     */
    @JsonIgnore
    public String getToken() {
        return token;
    }

    /**
     * Gets commandType
     *
     * @return value of commandType
     */
    @JsonIgnore
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Gets serializer
     *
     * @return value of serializer
     */
    @JsonIgnore
    public DataAPISerializer getSerializer() {
        return serializer;
    }

    // --------------------------------------------
    // ----- Java Core                        -----
    // --------------------------------------------

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
    public BaseOptions(String token, CommandType type, DataAPIClientOptions options) {
       this.token = token;
       this.commandType = type;
       if (options != null) {
           this.dataAPIClientOptions = options.clone();
       }
    }

    /**
     * Return the HTTP Request Timeout based on the command type.
     *
     * @param token
     *      authentication token
     * @param type
     *      command type
     * @param serializer
     *      serializer
     * @param options
     *      data api options
     */
    public BaseOptions(String token, CommandType type, DataAPISerializer serializer, DataAPIClientOptions options) {
        this.token       = token;
        this.commandType = type;
        this.serializer  = serializer;
        if (options != null) {
            this.dataAPIClientOptions = options.clone();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getSerializer().marshall(this);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public T clone() {
        try {
            BaseOptions<T> cloned = (BaseOptions<T>) super.clone();
            cloned.token = token;
            cloned.commandType = commandType;
            cloned.dataAPIClientOptions = dataAPIClientOptions.clone();
            return (T) cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

}
