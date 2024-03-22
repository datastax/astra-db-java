package com.datastax.astra.client.observer;

import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.api.ApiResponse;
import com.datastax.astra.internal.http.ApiResponseHttp;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Wrapper for the Command execution.
 */
@Getter
public class ExecutionInfos {

    /** Original request. */
    private final Command command;

    /** Raw Response. */
    private final ApiResponse response;

    private final int responseHttpCode;

    private final Map<String, String> responseHttpHeaders;

    /** How much time for the response. */
    private final long executionTime;

    /** When the call was issued. */
    private final Instant executionDate;

    /**
     * Constructor with the builder.
     *
     * @param builder
     *      current builder.
     */
    private ExecutionInfos(DataApiExecutionInfoBuilder builder) {
        this.command             = builder.command;
        this.response            = builder.response;
        this.responseHttpHeaders = builder.responseHttpHeaders;
        this.responseHttpCode    = builder.responseHttpCode;
        this.executionTime       = builder.executionTime;
        this.executionDate       = builder.executionDate;
    }

    /**
     * Initialize our custom builder.
     *
     * @return
     *      builder
     */
    public static DataApiExecutionInfoBuilder builder() {
        return new DataApiExecutionInfoBuilder();
    }

    /**
     * Builder class for execution informations
     */
    public static class DataApiExecutionInfoBuilder {
        private Command command;
        private ApiResponse response;
        private long executionTime;
        private int responseHttpCode;
        private Map<String, String> responseHttpHeaders;
        private Instant executionDate;

        /**
         * Default constructor.
         */
        public DataApiExecutionInfoBuilder() {
            this.executionDate = Instant.now();
        }

        /**
         * Populate after http call.
         *
         * @param command
         *      current command
         * @return
         *      current reference
         */
        public DataApiExecutionInfoBuilder withCommand(Command command) {
            this.command = command;
            return this;
        }

        /**
         * Populate after http call.
         *
         * @param response
         *      current response
         * @return
         *      current reference
         */
        public DataApiExecutionInfoBuilder withApiResponse(ApiResponse response) {
            this.response = response;
            return this;
        }

        /**
         * Populate after http call.
         *
         * @param httpResponse
         *      http response
         * @return
         *      current reference
         */
        public DataApiExecutionInfoBuilder withHttpResponse(ApiResponseHttp httpResponse) {
            if (httpResponse != null) {
                this.executionTime       = System.currentTimeMillis() - 1000 * executionDate.getEpochSecond();
                this.responseHttpCode    = httpResponse.getCode();
                this.responseHttpHeaders = httpResponse.getHeaders();
            }
            return this;
        }

        /**
         * Invoke constructor with the builder.
         *
         * @return
         *      immutable instance of execution infos.
         */
        public ExecutionInfos build() {
            return new ExecutionInfos(this);
        }


    }

}
