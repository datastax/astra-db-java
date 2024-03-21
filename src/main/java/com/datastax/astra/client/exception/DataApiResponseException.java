package com.datastax.astra.client.exception;

import io.stargate.sdk.data.client.model.ApiError;
import io.stargate.sdk.data.client.model.ApiResponse;
import io.stargate.sdk.data.client.model.ExecutionInfos;
import io.stargate.sdk.utils.Assert;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class to wrap error from the Data API.
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

