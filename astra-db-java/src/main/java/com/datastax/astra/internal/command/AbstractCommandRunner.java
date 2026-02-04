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

import com.datastax.astra.client.core.commands.Command;
import com.datastax.astra.client.core.commands.CommandRunner;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.BaseOptions;
import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
import com.datastax.astra.client.exceptions.DataAPIResponseException;
import com.datastax.astra.client.exceptions.DataAPITimeoutException;
import com.datastax.astra.internal.api.ApiResponseHttp;
import com.datastax.astra.internal.api.DataAPIResponse;
import com.datastax.astra.internal.http.RetryHttpClient;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.utils.Assert;
import com.datastax.astra.internal.utils.CompletableFutures;
import com.datastax.astra.internal.utils.EscapeUtils;
import com.dtsx.astra.sdk.utils.JsonUtils;
import com.evanlennick.retry4j.Status;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.datastax.astra.client.exceptions.InvalidEnvironmentException.throwErrorRestrictedAstra;
import static com.datastax.astra.internal.http.RetryHttpClient.CONTENT_TYPE_JSON;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_ACCEPT;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_AUTHORIZATION;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_CONTENT_TYPE;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_REQUESTED_WITH;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_TOKEN;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_USER_AGENT;

/**
 * Abstract base class for executing commands and handling their results.
 * <p>
 * This class provides a template for implementing a command runner that executes commands
 * with specific options and parses their results. It ensures consistent error handling by
 * throwing a {@link DataAPIResponseException} when necessary.
 * </p>
 *
 * <p>Subclasses must implement the command execution logic as required by the specific context.</p>
 *
 * @param <OPTIONS> the type of options used by the command runner, extending {@link BaseOptions}
 *
 * Example usage:
 * <pre>
 * {@code
 * public class MyCommandRunner extends AbstractCommandRunner<MyOptions> {
 *
 *     @Override
 *     protected void runCommand(MyOptions options) {
 *         // Implement the command execution logic here
 *     }
 *
 *     @Override
 *     protected void parseResults() {
 *         // Implement result parsing logic here
 *     }
 * }
 *
 * MyCommandRunner runner = new MyCommandRunner();
 * runner.execute(new MyOptions());
 * }
 * </pre>
 */
@Slf4j
@Getter
public abstract class AbstractCommandRunner<OPTIONS extends BaseOptions<?>> implements CommandRunner {

    // --- Arguments ---

    /** parameters names. */
    protected static final String ARG_DATABASE = "database";
    /** parameters names. */
    protected static final String ARG_OPTIONS = "options";
    /** parameters names. */
    protected static final String ARG_UPDATE = "update";
    /** parameters names. */
    protected static final String ARG_CLAZZ = "working class 'clazz'";

    // --- Read Outputs ---

    /** parameters names. */
    protected static final String RESULT_INSERTED_IDS = "insertedIds";
    /** parsing output json */
    protected static final String RESULT_DELETED_COUNT = "deletedCount";
    /** parsing output json */
    protected static final String RESULT_MATCHED_COUNT = "matchedCount";
    /** parsing output json */
    protected static final String RESULT_MODIFIED_COUNT = "modifiedCount";
    /** parsing output json */
    protected static final String RESULT_UPSERTED_ID = "upsertedId";
    /** parsing output json */
    protected static final String RESULT_MORE_DATA = "moreData";
    /** parsing output json */
    protected static final String RESULT_COUNT = "count";

    // --- Build Requests --

    /** json inputs */
    protected static final String OPTIONS_UPSERT = "upsert";
    /** json inputs */
    protected static final String OPTIONS_RETURN_DOCUMENT = "returnDocument";
    /** json inputs */
    protected static final String OPTIONS_ORDERED = "ordered";
    /** json inputs */
    protected static final String OPTIONS_RETURN_DOCUMENT_RESPONSES = "returnDocumentResponses";
    /** json inputs */
    protected static final String OPTIONS_PAGE_STATE = "pageState";
    /** json inputs */
    protected static final String OPTIONS_LIMIT = "limit";
    /** json inputs */
    protected static final String OPTIONS_HYBRID_LIMITS = "hybridLimits";
    /** json inputs */
    protected static final String OPTIONS_RERANK_QUERY = "rerankQuery";
    /** json inputs */
    protected static final String OPTIONS_RERANK_ON = "rerankOn";
    /** json inputs */
    protected static final String OPTIONS_INCLUDE_SORT_VECTOR = "includeSortVector";
    /** json inputs */
    protected static final String OPTIONS_INCLUDE_SCORES = "includeScores";
    /** json inputs */
    protected static final String OPTIONS_INCLUDE_SIMILARITY = "includeSimilarity";

    /** Http client reused when properties not override. */
    protected RetryHttpClient httpClient;

    /** Api Endpoint for the API. */
    protected String apiEndpoint;

    /**  Default command options when not override. */
    protected OPTIONS options;

    /**
     * Default constructor.
     */
    protected AbstractCommandRunner() {
    }

    /**
     * Constructor with the API endpoint and default options.
     *
     * @param apiEndpoint
     *      the API endpoint
     * @param options
     *     the default options
     */
    public AbstractCommandRunner(String apiEndpoint, OPTIONS options) {
        Assert.hasLength(apiEndpoint, "apiEndpoint");
        Assert.notNull(options, "options");
        this.apiEndpoint = apiEndpoint;
        this.options = options;
    }

    /** {@inheritDoc} */
    @Override
    public DataAPIResponse runCommand(Command command, BaseOptions<?> overridingOptions) {
        DataAPIClientOptions options = this.options.getDataAPIClientOptions();

        // ==================
        // === HTTPCLIENT ===
        // ==================
        if (httpClient == null) {
            httpClient = new RetryHttpClient(options.getHttpClientOptions(), options.getTimeoutOptions());
        }
        RetryHttpClient requestHttpClient = httpClient;

        // Should we override the client to use a different one
        long requestTimeout = this.options.getRequestTimeout();
        if (overridingOptions != null && overridingOptions.getDataAPIClientOptions() != null) {
            DataAPIClientOptions overClientOptions  = overridingOptions.getDataAPIClientOptions();
            HttpClientOptions overHttpClientOptions = overClientOptions.getHttpClientOptions();
            TimeoutOptions    overTimeoutOptions    = overClientOptions.getTimeoutOptions();
            // User provided specific parameters for the client
            if (overHttpClientOptions != null || overTimeoutOptions != null) {
                // overTimeoutOptions used only for connection timeout
                requestHttpClient = new RetryHttpClient(
                        overHttpClientOptions != null ? overHttpClientOptions : options.getHttpClientOptions(),
                        overTimeoutOptions != null ? overTimeoutOptions : options.getTimeoutOptions());
            }

            // =======================
            // ===   Timeouts      ===
            // =======================
            if (overTimeoutOptions != null) {
                requestTimeout = overridingOptions.getRequestTimeout();
            }
        }

        // ==================
        // === OBSERVERS ===
        // ==================

        List<CommandObserver> observers = new ArrayList<>(options.getObservers().values());
        if (overridingOptions != null
                && overridingOptions.getDataAPIClientOptions() != null
                && overridingOptions.getDataAPIClientOptions().getObservers() != null) {
            // Specialization has been found
            for (Map.Entry<String, CommandObserver> observer : overridingOptions
                    .getDataAPIClientOptions()
                    .getObservers()
                    .entrySet()) {
                // Add only if not already present
                if (!options.getObservers().containsKey(observer.getKey())) {
                    observers.add(observer.getValue());
                }
            }
        }

        // ==================
        // ===   TOKEN    ===
        // ==================

        String token = this.options.getToken();
        if (overridingOptions != null && overridingOptions.getToken() != null) {
            token = overridingOptions.getToken();
        }
        if (token == null) {
            throw new IllegalArgumentException("No token provided for the command");
        }

        // =======================
        // ===   SERIALIZER    ===
        // =======================

        DataAPISerializer serializer = this.options.getSerializer();
        if (overridingOptions != null && overridingOptions.getSerializer() != null) {
            serializer = overridingOptions.getSerializer();
        }

        // Initializing the Execution infos (could be pushed to 3rd parties)
        ExecutionInfos.DataApiExecutionInfoBuilder executionInfo =
                ExecutionInfos.builder()
                        .withCommand(command)
                        .withCommandOptions(this.options)
                        .withOverrideCommandOptions(overridingOptions);

        try {

            // (Custom) Serialization different for Tables and Documents
            String jsonCommand = serializer.marshall(command);
            // LOG REQUEST CONSOLE
            //System.out.println(jsonCommand);

            URI targetUri;
            try {
                targetUri = new URI(getApiEndpoint());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid Endpoints '" + getApiEndpoint() + "'", e);
            }
            // Build the request
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(targetUri)
                        .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                        .header(HEADER_USER_AGENT, httpClient.getUserAgentHeader())
                        .header(HEADER_REQUESTED_WITH, httpClient.getUserAgentHeader())
                        .header(HEADER_TOKEN, token)
                        .header(HEADER_AUTHORIZATION, "Bearer " + token)
                        .method("POST", HttpRequest.BodyPublishers.ofString(jsonCommand));
            if (requestTimeout > 0) {
                builder.timeout(Duration.ofMillis(requestTimeout));
            }

            // =======================
            // ===   HEADERS       ===
            // =======================

            if (options.getEmbeddingHeadersProvider() != null) {
                options.getEmbeddingHeadersProvider().getHeaders().forEach(builder::header);
            }
            if (options.getDatabaseAdditionalHeaders() != null) {
                options.getDatabaseAdditionalHeaders().forEach(builder::header);
            }
            if (options.getAdminAdditionalHeaders() != null) {
                options.getAdminAdditionalHeaders().forEach(builder::header);
            }

            if (overridingOptions!= null && overridingOptions.getDataAPIClientOptions() != null) {
                DataAPIClientOptions overClientOptions = overridingOptions.getDataAPIClientOptions();
                if (overClientOptions.getEmbeddingHeadersProvider() != null) {
                    overClientOptions.getEmbeddingHeadersProvider().getHeaders().forEach(builder::header);
                }
                if (overClientOptions.getRerankingHeadersProvider() != null) {
                    overClientOptions.getRerankingHeadersProvider().getHeaders().forEach(builder::header);
                }
                if (overClientOptions.getDatabaseAdditionalHeaders() != null) {
                    overClientOptions.getDatabaseAdditionalHeaders().forEach(builder::header);
                }
                if (overClientOptions.getAdminAdditionalHeaders() != null) {
                    overClientOptions.getAdminAdditionalHeaders().forEach(builder::header);
                }
            }

            HttpRequest request = builder.build();
            executionInfo.withSerializer(serializer);
            executionInfo.withRequestHeaders(request.headers().map());
            executionInfo.withRequestUrl(getApiEndpoint());
            Status<HttpResponse<String>> status = requestHttpClient.executeHttpRequest(request);
            // LOG RESPONSES CONSOLE
            //System.out.println(status.getResult().body());
            ApiResponseHttp httpRes = requestHttpClient.parseHttpResponse(status.getResult());
            executionInfo.withHttpResponse(httpRes);

            if (httpRes == null) {
                throw new DataAPITimeoutException("Timeout while executing command '" +
                        command.getName() + "' timeout: " + requestTimeout +
                        " but was " + executionInfo.getExecutionTime());
            }

            //String dataAPIRawBody = httpRes.getBody();
            //String escapedDataAPIRawBody = EscapeUtils.escapeRawJsonNames(dataAPIRawBody);
            //System.out.println("escaped:" + escapedDataAPIRawBody);
            DataAPIResponse apiResponse = serializer.unMarshallBean(httpRes.getBody(), DataAPIResponse.class);
            apiResponse.setSerializer(serializer);
            if (apiResponse.getStatus() != null) {
                apiResponse.getStatus().setSerializer(serializer);
            }
            executionInfo.withApiResponse(apiResponse);
            // Encapsulate Errors
            if (apiResponse.getErrors() != null) {
                throw new DataAPIResponseException(Collections.singletonList(executionInfo.build()));
            }
            // Trace All Warnings
            if (apiResponse.getStatus()!= null && apiResponse.getStatus().getWarnings() != null) {
                try {
                    apiResponse.getStatus().getWarnings().stream()
                            .map(this.options.getSerializer()::marshall).forEach(log::warn);
                } catch(Exception e) {
                    apiResponse.getStatusKeyAsList("warnings", Object.class)
                           .forEach(error -> log.warn(this.options.getSerializer().marshall(error)));
                }
            }
            return apiResponse;
        } finally {
            // Notify the observers
            CompletableFuture.runAsync(()-> notifyASync(l -> l.onCommand(executionInfo.build()), observers));
        }
    }

    /** {@inheritDoc} */
    @Override
    public <DOC> DOC runCommand(Command command, BaseOptions<?> options, Class<DOC> documentClass) {
        return unmarshall(runCommand(command, options), documentClass);
    }

    /**
     * Asynchronously send calls to listener for tracing.
     *
     * @param lambda
     *      operations to execute
     * @param observers
     *      list of observers to check
     */
    private void notifyASync(Consumer<CommandObserver> lambda, List<CommandObserver> observers) {
        if (observers != null) {
            CompletableFutures.allDone(observers.stream()
                    .map(l -> CompletableFuture.runAsync(() -> lambda.accept(l)))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Validates that the current options are configured for Astra.
     *
     * <p>
     * This method ensures that the operation is being performed in an Astra environment.
     * If the options are not set for Astra, it throws an exception with details about the restriction.
     * </p>
     *
     * @throws IllegalStateException if the configuration is not set for Astra
     */
    protected void assertIsAstra() {
        if (!options.getDataAPIClientOptions().isAstra()) {
            throwErrorRestrictedAstra("getRegion", options.getDataAPIClientOptions().getDestination());
        }
    }

    /**
     * Gets the serializer currently in place to parse inputs and outputs.
     *
     * @return
     *      the serializer
     */
    protected DataAPISerializer getSerializer() {
        return this.options.getSerializer();
    }

    /**
     * Document Mapping.
     *
     * @param api
     *      api response
     * @param documentClass
     *      document class
     * @return
     *      document
     * @param <DOC>
     *     document type
     */
    protected <DOC> DOC unmarshall(DataAPIResponse api, Class<DOC> documentClass) {
        String payload;
        if (api.getData() != null) {
            if (api.getData().getDocument() != null) {
                payload = getSerializer().marshall(api.getData().getDocument());
            } else if (api.getData().getDocuments() != null) {
                payload = getSerializer().marshall(api.getData().getDocuments());
            } else {
                throw new IllegalStateException("Cannot marshall into '" + documentClass + "' no documents returned.");
            }
        } else {
            payload = getSerializer().marshall(api.getStatus());
        }
        return getSerializer().unMarshallBean(payload, documentClass);
    }

    /**
     * Gets apiEndpoint
     *
     * @return value of apiEndpoint
     */
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    /**
     * Gets commandOptions
     *
     * @return value of commandOptions
     */
    public OPTIONS getOptions() {
        return options;
    }
}
