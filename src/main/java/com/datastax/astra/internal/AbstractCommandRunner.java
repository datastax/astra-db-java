package com.datastax.astra.internal;

import io.stargate.sdk.data.client.CommandRunner;
import io.stargate.sdk.data.client.exception.DataApiResponseException;
import io.stargate.sdk.data.client.model.ApiResponse;
import io.stargate.sdk.data.client.model.Command;
import io.stargate.sdk.data.client.model.ExecutionInfos;
import io.stargate.sdk.data.client.observer.DataApiCommandObserver;
import io.stargate.sdk.http.domain.ApiResponseHttp;
import io.stargate.sdk.utils.CompletableFutures;
import io.stargate.sdk.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Execute the command and parse results throwing DataApiResponseException when needed.
 */
@Slf4j
public abstract class AbstractCommandRunner implements CommandRunner {

    /** Could be usefult to capture the interactions at client side. */
    protected Map<String, DataApiCommandObserver> observers = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void registerListener(String name, DataApiCommandObserver observer) {
        observers.put(name, observer);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteListener(String name) {
        observers.remove(name);
    }

    /** {@inheritDoc} */
    @Override
    public ApiResponse runCommand(Command command) {

        // Initializing the Execution infos (could be pushed to 3rd parties)
        ExecutionInfos.DataApiExecutionInfoBuilder executionInfo =
                ExecutionInfos.builder().withCommand(command);

        try {
            // (Custom) Serialization
            String jsonCommand = JsonUtils.marshallForDataApi(command);

            ApiResponseHttp httpRes = getHttpClient().POST(lookup(), jsonCommand);
            executionInfo.withHttpResponse(httpRes);

            ApiResponse jsonRes = JsonUtils.unmarshallBeanForDataApi(httpRes.getBody(), ApiResponse.class);
            executionInfo.withApiResponse(jsonRes);

            // Encapsulate Errors
            if (jsonRes.getErrors() != null) {
                throw new DataApiResponseException(Collections.singletonList(executionInfo.build()));
            }

            return jsonRes;
        } finally {
            // Notify the observers
            CompletableFuture.runAsync(()-> notifyASync(l -> l.onCommand(executionInfo.build())));
        }
    }

    /**
     * Asynchronously send calls to listener for tracing.
     *
     * @param lambda
     *      operations to execute
     * @return
     *      void
     */
    private CompletionStage<Void> notifyASync(Consumer<DataApiCommandObserver> lambda) {
        return CompletableFutures.allDone(observers.values().stream()
                .map(l -> CompletableFuture.runAsync(() -> lambda.accept(l)))
                .collect(Collectors.toList()));
    }

    /** {@inheritDoc} */
    @Override
    public <DOC> DOC runCommand(Command command, Class<DOC> documentClass) {
        return mapAsDocument(runCommand(command), documentClass);
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
    protected <DOC> DOC mapAsDocument(ApiResponse api, Class<DOC> documentClass) {
        String payload;
        if (api.getData() != null) {
            if (api.getData().getDocument() != null) {
                payload = JsonUtils.marshallForDataApi(api.getData().getDocument());
            } else if (api.getData().getDocuments() != null) {
                payload = JsonUtils.marshallForDataApi(api.getData().getDocuments());
            } else {
                throw new IllegalStateException("Cannot marshall into '" + documentClass + "' no documents returned.");
            }
        } else {
            payload = JsonUtils.marshallForDataApi(api.getStatus());
        }
        return JsonUtils.unmarshallBeanForDataApi(payload, documentClass);
    }

}
