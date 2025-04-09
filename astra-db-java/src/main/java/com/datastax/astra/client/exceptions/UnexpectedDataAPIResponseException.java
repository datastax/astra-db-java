package com.datastax.astra.client.exceptions;

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

import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.internal.api.DataAPIResponse;
import lombok.Getter;

/**
 * Represents a specific kind of {@link DataAPIException} that is thrown when the response
 * received from the Data API does not match the expected format or content. This exception
 * is typically thrown in situations where the API response is either malformed, incomplete,
 * or contains an error code indicating a failure that needs to be addressed by the client application.
 * <p>
 * This exception encapsulates details about the command that triggered the erroneous response
 * and the actual response received from the Data API, allowing for more informed error handling
 * and debugging. It is advisable to catch this exception specifically when performing operations
 * that are critical and have known potential for response inconsistencies.
 * </p>
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * try {
 *     ApiResponse response = dataApiClient.executeCommand(someCommand);
 *     if (!response.isSuccessful()) {
 *         throw new DataApiFaultyResponseException(someCommand, response, "The response indicates a failure.");
 *     }
 *     // Process the successful response
 * } catch (DataApiFaultyResponseException e) {
 *     // Handle scenarios where the API response was not as expected
 *     log.error("Faulty response received for command: " + e.getCommand() + " with message: " + e.getMessage(), e);
 * }
 * }
 * </pre>
 *
 * @see DataAPIException
 */
@Getter
public class UnexpectedDataAPIResponseException extends DataAPIException {

    /** Command which triggered the error. */
    private final Command command;

    /** The Data API response associated with the error. */
    private final DataAPIResponse response;

    /**
     * Constructs a new exception with the specified command that triggered the error,
     * the API response received, and a custom error message.
     *
     * @param cmd The command object that was executed and led to the faulty response.
     * @param res The actual response received from the Data API which was not as expected.
     * @param msg The detailed error message explaining the nature of the fault.
     */
    public UnexpectedDataAPIResponseException(Command cmd, DataAPIResponse res, String msg) {
        super(DEFAULT_ERROR_CODE, msg);
        this.command = cmd;
        this.response = res;
    }

    /**
     * Constructs a new exception with the specified command that triggered the error,
     * the API response received, and a custom error message.
     *
     * @param msg The detailed error message explaining the nature of the fault.
     */
    public UnexpectedDataAPIResponseException(String msg) {
        super(DEFAULT_ERROR_CODE, msg);
        this.command = null;
        this.response = null;
    }
}

