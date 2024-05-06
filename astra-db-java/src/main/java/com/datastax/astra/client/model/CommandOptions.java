package com.datastax.astra.client.model;

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

import com.datastax.astra.internal.command.CommandObserver;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Options that will be provided to all commands for this collection.
 *
 * @param <T>
 *     the sub-class implementing the command options
 */
public class CommandOptions<T extends CommandOptions<T>>{

    /**
     * List of observers to notify.
     */
    @Getter
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
     * Header for embeddings when that make sense
     */
    protected String embeddingAPIKey;

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
     * @param embeddingServiceApiKey
     *      embedding service key
     * @return
     *      service key
     */
    @SuppressWarnings("unchecked")
    public T embeddingAPIKey(String embeddingServiceApiKey) {
        this.embeddingAPIKey = embeddingServiceApiKey;
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
     * Gets embeddinAPIKey
     *
     * @return value of embeddingAPIKey
     */
    public Optional<String> getEmbeddingAPIKey() {
        return Optional.ofNullable(embeddingAPIKey);
    }

    /**
     * Gets token
     *
     * @return value of token
     */
    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }
    /**
     * Gets httpClientOptions
     *
     * @return value of httpClientOptions
     */
    public Optional<HttpClientOptions> getHttpClientOptions() {
        return Optional.ofNullable(httpClientOptions);
    }

    /**
     * Default Constructor.
     */
    public CommandOptions() {
        // left blank, jackson serialization
    }


}
