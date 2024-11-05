package com.datastax.astra.internal.command;

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

import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.api.ApiResponseHttp;
import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates detailed information about the execution of a command, including the original request,
 * the raw response, HTTP response details, and timing information. This class serves as a comprehensive
 * record of a command's execution, facilitating analysis, logging, and monitoring of command operations.
 */
@Getter
public class ExecutionInfos implements Serializable {

    /**
     * The original command request that was executed. This field provides access to the details of the
     * command that triggered the execution, allowing observers to understand what operation was performed.
     */
    private final Command command;

    /**
     * A map containing the HTTP headers from the request.
     */
    private final Map<String, List<String>> requestHttpHeaders;

    /**
     * Request URL
     */
    private final String requestUrl;

    /**
     * The raw {@link ApiResponse} received in response to the command execution. This field contains the
     * complete response from the server, including any data, errors, or status information returned.
     */
    private final ApiResponse response;

    /**
     * The HTTP status code returned by the server in response to the command execution. This code provides
     * a standard way to indicate the result of the HTTP request (e.g., success, error, not found).
     */
    private final int responseHttpCode;

    /**
     * A map containing the HTTP headers from the response. These headers can provide additional context about
     * the response, such as content type, caching policies, and other metadata.
     */
    private final Map<String, String> responseHttpHeaders;

    /**
     * The duration of time, in milliseconds, that the command execution took, from sending the request to
     * receiving the response. This timing information can be used for performance monitoring and optimization.
     */
    private final long executionTime;

    /**
     * The timestamp marking when the command execution was initiated. This information is useful for logging
     * and monitoring purposes, allowing for the temporal correlation of command executions within the system.
     */
    private final Instant executionDate;

    /**
     * The serializer used to serialize and deserialize data objects.
     */
    private final DataAPISerializer serializer;

    /**
     * Constructor with the builder.
     *
     * @param builder
     *      current builder.
     */
    private ExecutionInfos(DataApiExecutionInfoBuilder builder) {
        this.command             = builder.command;
        this.requestHttpHeaders  = builder.requestHttpHeaders;
        this.response            = builder.response;
        this.responseHttpHeaders = builder.responseHttpHeaders;
        this.responseHttpCode    = builder.responseHttpCode;
        this.executionTime       = builder.executionTime;
        this.executionDate       = builder.executionDate;
        this.requestUrl          = builder.requestUrl;
        this.serializer          = builder.serializer;
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
     * Builder class for execution information
     */
    public static class DataApiExecutionInfoBuilder {
        private Command command;
        private ApiResponse response;
        private long executionTime;
        private int responseHttpCode;
        private Map<String, List<String>> requestHttpHeaders;
        private Map<String, String> responseHttpHeaders;
        private final Instant executionDate;
        private String requestUrl;
        private DataAPISerializer serializer;

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
         * @param serializer
         *      current serializer
         * @return
         *      current reference
         */
        public DataApiExecutionInfoBuilder withSerializer(DataAPISerializer serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Populate after http call.
         *
         * @param url
         *     target url
         * @return
         *      current reference
         */
        public DataApiExecutionInfoBuilder withRequestUrl(String url) {
            this.requestUrl = url;
            return this;
        }

        /**
         * Populate after http call.
         *
         * @param headers
         *      request headers
         * @return
         *      current reference
         */
        public DataApiExecutionInfoBuilder withRequestHeaders(Map<String, List<String>> headers) {
            this.requestHttpHeaders = headers;
            return this;
        }

        /**
         * Populate after http call.
         *
         * @param response current response
         */
        public void withApiResponse(ApiResponse response) {
            this.response = response;
        }

        /**
         * Populate after http call.
         *
         * @param httpResponse http response
         */
        public void withHttpResponse(ApiResponseHttp httpResponse) {
            Assert.notNull(httpResponse, "httpResponse");
            this.executionTime       = System.currentTimeMillis() - 1000 * executionDate.getEpochSecond();
            this.responseHttpCode    = httpResponse.getCode();
            this.responseHttpHeaders = httpResponse.getHeaders();
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
