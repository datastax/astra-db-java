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

import com.datastax.astra.client.exception.DataApiResponseException;
import com.datastax.astra.client.model.Command;
import com.datastax.astra.client.model.CommandOptions;
import com.datastax.astra.client.model.CommandRunner;
import com.datastax.astra.internal.api.ApiResponse;
import com.datastax.astra.internal.api.ApiResponseHttp;
import com.datastax.astra.internal.http.RetryHttpClient;
import com.datastax.astra.internal.utils.CompletableFutures;
import com.datastax.astra.internal.utils.JsonUtils;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.datastax.astra.internal.http.RetryHttpClient.CONTENT_TYPE_JSON;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_ACCEPT;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_AUTHORIZATION;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_CASSANDRA;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_CONTENT_TYPE;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_REQUESTED_WITH;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_REQUEST_ID;
import static com.datastax.astra.internal.http.RetryHttpClient.HEADER_USER_AGENT;

/**
 * Execute the command and parse results throwing DataApiResponseException when needed.
 */
@Slf4j
@Getter
public abstract class AbstractCommandRunner implements CommandRunner {

    /** Http client reused when properties not override. */
    protected RetryHttpClient httpClient;

    /**
     * Default command options when not override
     */
    protected CommandOptions<?> commandOptions;

    /**
     * Default constructor.
     */
    protected AbstractCommandRunner() {
    }

    /** {@inheritDoc} */
    @Override
    public ApiResponse runCommand(Command command, CommandOptions<?> overridingOptions) {

        // === HTTP CLIENT ===
        if (httpClient == null && (overridingOptions == null || overridingOptions.getHttpClientOptions().isEmpty())) {
            if (commandOptions.getHttpClientOptions().isEmpty()) {
                throw new IllegalStateException("HttpClientOptions is required in collection level options");
            }
            httpClient = new RetryHttpClient(this.commandOptions.getHttpClientOptions().get());
        }
        RetryHttpClient requestHttpClient = this.httpClient;
        if (overridingOptions != null && overridingOptions.getHttpClientOptions().isPresent()) {
            requestHttpClient = new RetryHttpClient(overridingOptions.getHttpClientOptions().get());
        }

        // === OBSERVERS ===
        List<CommandObserver> observers = new ArrayList<>(commandOptions.getObservers().values());
        if (overridingOptions != null && overridingOptions.getObservers() != null) {
            for (Map.Entry<String, CommandObserver> observer : overridingOptions.getObservers().entrySet()) {
                if (!commandOptions.getObservers().containsKey(observer.getKey())) {
                    observers.add(observer.getValue());
                }
            }
        }

        // === TOKEN ===
        String token = null;
        if (commandOptions.getToken().isPresent()) {
            token = commandOptions.getToken().get();
        }
        if (overridingOptions != null && overridingOptions.getToken().isPresent()) {
            token = overridingOptions.getToken().get();
        }

        // Initializing the Execution infos (could be pushed to 3rd parties)
        ExecutionInfos.DataApiExecutionInfoBuilder executionInfo =
                ExecutionInfos.builder().withCommand(command);

        try {
            // (Custom) Serialization
            String jsonCommand = JsonUtils.marshall(command);
            HttpRequest.Builder builder=  HttpRequest.newBuilder()
                        .uri(new URI(getApiEndpoint()))
                        .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                        .header(HEADER_USER_AGENT, requestHttpClient.getUserAgentHeader())
                        .header(HEADER_REQUESTED_WITH, requestHttpClient.getUserAgentHeader())
                        .header(HEADER_REQUEST_ID, UUID.randomUUID().toString())
                        .header(HEADER_CASSANDRA, token)
                        .header(HEADER_AUTHORIZATION, "Bearer " + token)
                        .method("POST", HttpRequest.BodyPublishers.ofString(jsonCommand));
            builder.timeout(Duration.ofSeconds(requestHttpClient.getResponseTimeoutInSeconds()));

            // === Embedding KEY ===
            // FIX ME EMBEDDING AUTHENTICATION PROVIDER
            if (commandOptions.getEmbeddingAuthProvider().isPresent()) {
                commandOptions.getEmbeddingAuthProvider()
                        .get().getHeaders()
                        .forEach(builder::header);
            }
            if (overridingOptions != null && overridingOptions.getEmbeddingAuthProvider().isPresent()) {
                overridingOptions.getEmbeddingAuthProvider()
                        .get().getHeaders()
                        .forEach(builder::header);
            }

            HttpRequest request = builder.build();
            executionInfo.withRequestHeaders(request.headers().map());
            executionInfo.withRequestUrl(getApiEndpoint());

            Status<HttpResponse<String>> status = requestHttpClient.executeHttpRequest(request);
            ApiResponseHttp httpRes = requestHttpClient.parseHttpResponse(status.getResult());
            executionInfo.withHttpResponse(httpRes);
            ApiResponse jsonRes = JsonUtils.unMarshallBean(httpRes.getBody(), ApiResponse.class);
            executionInfo.withApiResponse(jsonRes);
            // Encapsulate Errors
            if (jsonRes.getErrors() != null) {
                throw new DataApiResponseException(Collections.singletonList(executionInfo.build()));
            }
            // Trace All Warning
            if (jsonRes.getStatus().containsKey("warnings")) {
                jsonRes.getStatusKeyAsStringStream("warnings").forEach(log::warn);
            }
            return jsonRes;
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL '" + getApiEndpoint() + "'", e);
        } finally {
            // Notify the observers
            CompletableFuture.runAsync(()-> notifyASync(l -> l.onCommand(executionInfo.build()), observers));
        }
    }

    /**
     * Asynchronously send calls to listener for tracing.
     *
     * @param lambda
     *      operations to execute
     * @param observers
     *      list of observers to check
     *
     */
    private void notifyASync(Consumer<CommandObserver> lambda, List<CommandObserver> observers) {
        if (observers != null) {
            CompletableFutures.allDone(observers.stream()
                    .map(l -> CompletableFuture.runAsync(() -> lambda.accept(l)))
                    .collect(Collectors.toList()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> T runCommand(Command command, CommandOptions<?> options, Class<T> documentClass) {
        return mapAsDocument(runCommand(command, options), documentClass);
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
     * @param <T>
     *     document type
     */
    protected <T> T mapAsDocument(ApiResponse api, Class<T> documentClass) {
        String payload;
        if (api.getData() != null) {
            if (api.getData().getDocument() != null) {
                payload = JsonUtils.marshall(api.getData().getDocument());
            } else if (api.getData().getDocuments() != null) {
                payload = JsonUtils.marshall(api.getData().getDocuments());
            } else {
                throw new IllegalStateException("Cannot marshall into '" + documentClass + "' no documents returned.");
            }
        } else {
            payload = JsonUtils.marshall(api.getStatus());
        }
        return JsonUtils.unMarshallBean(payload, documentClass);
    }

    /**
     * The subclass should provide the endpoint, url to post request.
     *
     * @return
     *      url on which to post the request
     */
    protected abstract String getApiEndpoint();


}
