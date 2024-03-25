package com.datastax.astra.client.exception;

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

import com.datastax.astra.internal.ApiError;
import com.datastax.astra.internal.ApiResponse;
import com.datastax.astra.internal.ExecutionInfos;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Extends {@link DataApiException} to specifically address errors encountered during the
 * processing of responses from the Data API. This exception is particularly useful when
 * a command issued to the API is internally divided into multiple sub-operations, each of
 * which might succeed or fail independently. It aggregates detailed information about each
 * sub-operation, including execution times and any errors encountered, facilitating comprehensive
 * error tracking and debugging.
 * <p>
 * The {@code commandsList} attribute of this exception holds a collection of {@link ExecutionInfos}
 * objects, each representing the outcome of a sub-operation. This detailed breakdown helps in
 * identifying specific points of failure within a complex command execution sequence, making it
 * easier for developers to diagnose issues and implement more robust error handling and recovery
 * strategies.
 * </p>
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * try {
 *     List<Document> longListOfDocuments;
 *     collection.insertMany(longListOfDocuments);
 * } catch (DataApiResponseException e) {
 *     e.getCommandsList().forEach(executionInfo -> {
 *         if (executionInfo.hasErrors()) {
 *             log.error("Error in sub-operation: " + executionInfo.getErrorDetails());
 *         }
 *     });
 *     // Handle the exception or notify the user
 * }
 * }
 * </pre>
 *
 * @see DataApiException
 */
@Getter
public class DataApiResponseException extends DataApiException {

    /**
     * Trace the execution results information.
     */
    List<ExecutionInfos> commandsList;

    /**
     * Constructor with list of constructors.
     *
     * @param cmdList
     *      command execution list
     */
    public DataApiResponseException(List<ExecutionInfos> cmdList) {
        super(getErrorCode(cmdList), getErrorMessage(cmdList));
        this.commandsList = cmdList;
    }

    /**
     * Flattening errors as a list.
     *
     * @return
     *      list of errors
     */
    public List<ApiError> getApiErrors() {
        if (commandsList != null) {
            return commandsList.stream()
                    .map(ExecutionInfos::getResponse)
                    .filter(Objects::nonNull)
                    .map(ApiResponse::getErrors)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Parse the command list to get first error of first command.
     *
     * @param commands
     *      input command list
     * @return
     *      error message from the API
     */
    public static String getErrorMessage(List<ExecutionInfos> commands) {
        Assert.notNull(commands, "commandList");
        return findFirstError(commands).map(ApiError::getErrorMessage).orElse(DEFAULT_ERROR_MESSAGE);
    }

    /**
     * Parse the command list to get first error of first command.
     *
     * @param commands
     *      input command list
     * @return
     *      error code from the API
     */
    public static String getErrorCode(List<ExecutionInfos> commands) {
        Assert.notNull(commands, "commandList");
        return findFirstError(commands).map(ApiError::getErrorCode).orElse(DEFAULT_ERROR_CODE);
    }

    /**
     * Scan the command execution list to return first Error in appearance.
     *
     * @param commands
     *      command list
     * @return
     *      first error if exists
     */
    private static Optional<ApiError> findFirstError(List<ExecutionInfos> commands) {
        for (ExecutionInfos command :commands) {
            if (command.getResponse() != null
                    && command.getResponse().getErrors()!= null
                    && !command.getResponse().getErrors().isEmpty()) {
                return Optional.ofNullable(command.getResponse().getErrors().get(0));
            }
        }
        return Optional.empty();
    }


}

