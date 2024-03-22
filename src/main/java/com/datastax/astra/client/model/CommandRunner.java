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

import com.datastax.astra.client.exception.DataApiResponseException;
import com.datastax.astra.client.internal.ApiResponse;
import com.datastax.astra.client.internal.observer.CommandObserver;

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
