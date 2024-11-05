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

import com.datastax.astra.client.exception.DataAPIResponseException;
import com.datastax.astra.internal.api.ApiResponse;

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
     * @throws DataAPIResponseException
     *     if the returned object contains error response is encapsulated in a DataApiResponseException.
     * @return
     *     result as a document map
     */
    default ApiResponse runCommand(Command command) {
        return runCommand(command, new CommandOptions<>());
    }

    /**
     * Command to return the payload as a Map.
     *
     * @param command
     *     command to execute
     * @param options
     *      options when running the command
     * @throws DataAPIResponseException
     *     if the returned object contains error response is encapsulated in a DataApiResponseException.
     * @return
     *     result as a document map
     */
    ApiResponse runCommand(Command command, CommandOptions<?> options)
    throws DataAPIResponseException;

    /**
     * Extension point to run any command with typing constraints.
     * @param command
     *      command as a json Payload
     * @param documentClass
     *      document class to use for marshalling
     * @throws DataAPIResponseException
     *     if the returned object contains error response is encapsulated in a DataApiResponseException.
     * @return
     *      instance of expecting type.
     * @param <T>
     *      document type to use
     */
    default <T> T runCommand(Command command, Class<T> documentClass) {
        return runCommand(command, new CommandOptions<>(), documentClass);
    }

    /**
     * Extension point to run any command with typing constraints.
     * @param command
     *      command as a json Payload
     * @param documentClass
     *      document class to use for marshalling
     * @param options
     *      options when running the command
     * @throws DataAPIResponseException
     *     if the returned object contains error response is encapsulated in a DataApiResponseException.
     * @return
     *      instance of expecting type.
     * @param <T>
     *      document type to use
     */
    <T> T runCommand(Command command, CommandOptions<?> options, Class<T> documentClass)
    throws DataAPIResponseException;
}
