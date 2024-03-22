package com.datastax.astra.client;

import com.datastax.astra.client.exception.DataApiResponseException;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.api.ApiResponse;
import com.datastax.astra.client.observer.CommandObserver;
import com.datastax.astra.internal.http.RetryHttpClient;

/**
 * Use to initialize the HTTPClient.
 */
public interface CommandRunner {

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
    void registerListener(String name, CommandObserver observer);

    /**
     * Delete a listener of it exists.
     *
     * @param name
     *      name of the listener.
     */
    void deleteListener(String name);

}
