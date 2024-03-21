package com.datastax.astra.client;

import io.stargate.sdk.data.client.exception.DataApiResponseException;
import io.stargate.sdk.data.client.model.ApiResponse;
import io.stargate.sdk.data.client.model.Command;
import io.stargate.sdk.data.client.observer.DataApiCommandObserver;
import io.stargate.sdk.http.LoadBalancedHttpClient;
import io.stargate.sdk.http.ServiceHttp;

import java.util.function.Function;

/**
 * Use to initialize the HTTPClient.
 */
public interface CommandRunner {

    /**
     * Lookup for an endpoint (Load balancing)
     *
     * @return
     *      endpoint in a distributed
     */
    Function<ServiceHttp, String> lookup();

    /**
     * Access the HTTP client singleton.
     *
     * @return
     *      endpoint in a distributed
     */
    LoadBalancedHttpClient getHttpClient();

    // ------------------------------------------
    // ----           Command                ----
    // ------------------------------------------

    /**
     * Command to return the payload as a Map.
     *
     * @param command
     *     command to execute
     * @throws DataApiResponseException
     *     if the returned object contains error response is encapsulated in a DataApiResponseException.
     * @return
     *     result as a document map
     */
    ApiResponse runCommand(Command command)
    throws DataApiResponseException;

    /**
     * Extension point to run any command with typing constraints.
     * @param command
     *      command as a json Payload
     * @param documentClass
     *      document class to use for marshalling
     * @throws DataApiResponseException
     *     if the returned object contains error response is encapsulated in a DataApiResponseException.
     * @return
     *      instance of expecting type.
     * @param <DOC>
     *      document type to use
     */
    <DOC> DOC runCommand(Command command, Class<DOC> documentClass)
    throws DataApiResponseException;

    /**
     * Register an observer to execute code.
     *
     * @param name
     *      identifier for the listener
     * @param observer
     *      observer
     */
    void registerListener(String name, DataApiCommandObserver observer);

    /**
     * Delete a listener of it exists.
     *
     * @param name
     *      name of the listener.
     */
    void deleteListener(String name);

}
