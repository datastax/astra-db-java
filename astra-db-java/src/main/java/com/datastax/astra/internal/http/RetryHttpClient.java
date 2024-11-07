package com.datastax.astra.internal.http;

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

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.core.http.Caller;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.exception.DataAPIException;
import com.datastax.astra.client.exception.DataAPIHttpException;
import com.datastax.astra.internal.api.ApiResponseHttp;
import com.evanlennick.retry4j.CallExecutorBuilder;
import com.evanlennick.retry4j.Status;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.datastax.astra.client.exception.DataAPIException.ERROR_CODE_HTTP;

/**
 * Http Client using JDK11 client with a retry mechanism.
 */
@Slf4j
public class RetryHttpClient {

    /** Headers, Api is usig JSON */
    public static final String CONTENT_TYPE_JSON        = "application/json";

    /** Headers, Api is usig JSON */
    public static final String CONTENT_TYPE_GRAPHQL     = "application/graphql";

    /** Header param. */
    public static final String HEADER_ACCEPT            = "Accept";

    /** Headers param to insert the token. */
    public static final String HEADER_CASSANDRA         = "X-Cassandra-Token";

    /** Headers param to insert the unique identifier for the request. */
    public static final String HEADER_REQUEST_ID        = "X-Cassandra-Request-Id";

    /** Headers param to insert the conte type. */
    public static final String HEADER_CONTENT_TYPE      = "Content-Type";

    /** Headers param to insert the token for devops API. */
    public static final String HEADER_AUTHORIZATION     = "Authorization";

    /** Headers name to insert the user agent identifying the client. */
    public static final String HEADER_USER_AGENT        = "User-Agent";

    /** Headers param to insert the user agent identifying the client. */
    public static final String HEADER_REQUESTED_WITH    = "X-Requested-With";

    /** Value for the requested with. */
    public static final String REQUEST_WITH = "data-api-client-java";

    /** JDK11 Http client. */
    protected final HttpClient httpClient;

    /** Http Options. */
    protected final HttpClientOptions httpClientOptions;

    /** Default retry configuration. */
    protected final RetryConfig retryConfig;

    /**
     * Default initialization of http client.
     */
    public RetryHttpClient() {
        this(DataAPIOptions.builder().build().getHttpClientOptions());
    }

    /**
     * Initialize the instance with all items
     *
     * @param config
     *      configuration of the HTTP CLIENT.
     */
    public RetryHttpClient(HttpClientOptions config) {
        this.httpClientOptions = config;

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(config.getHttpVersion());
        httpClientBuilder.followRedirects(config.getHttpRedirect());
        httpClientBuilder.connectTimeout(config.getConnectionTimeout());
        if (config.getProxy() != null) {
            httpClientBuilder.proxy(ProxySelector.of(new InetSocketAddress(
                    config.getProxy().getHostname(),
                    config.getProxy().getPort())));
        }
        httpClient = httpClientBuilder.build();

        retryConfig = new RetryConfigBuilder()
                .retryOnAnyException()
                .withDelayBetweenTries(config.getRetryDelay())
                .withMaxNumberOfTries(config.getRetryCount())
                .withExponentialBackoff()
                .build();
    }

    /**
     * Give access to the user agent header.
     *
     * @return
     *      user agent header
     */
    public String getUserAgentHeader() {
        List<Caller> callers = httpClientOptions.getCallers();
        StringJoiner sj = new StringJoiner(" ");
        for (int i = callers.size() - 1; i >= 0; i--) {
            Caller entry = callers.get(i);
            sj.add(entry.getName() + "/" + entry.getVersion());
        }
        return sj.toString();
    }

    /**
     * Return value for default response time.
     *
     * @return
     *      default response time
     */
    public long getResponseTimeoutInSeconds() {
        return httpClientOptions.getRequestTimeout().getSeconds();
    }

    // -------------------------------------------
    // ---------- Working with HTTP --------------
    // -------------------------------------------

    /**
     * Helper to build the HTTP request.
     *
     * @param url
     *      target url
     * @param token
     *      authentication token
     * @param body
     *      request body
     * @return
     *      http request
     */
    public ApiResponseHttp post(String url, String token, String body) {
        return executeHttp("POST", url, token, body, CONTENT_TYPE_JSON);
    }

    /**
     * Help to build the HTTP request.
     *
     * @param method
     *      http method name
     * @param url
     *      http url
     * @param token
     *      token for authorization header
     * @param body
     *     body of the request
     * @param contentType
     *      content type for the request
     * @return
     *      the http request.
     */
    public HttpRequest builtHttpRequest(final String method,
                                         final String url,
                                         final String token,
                                         String body,
                                         String contentType) {
        try {
            return HttpRequest.newBuilder()
                .uri(new URI(url))
                .header(HEADER_CONTENT_TYPE, contentType)
                .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                .header(HEADER_USER_AGENT, getUserAgentHeader())
                .header(HEADER_REQUESTED_WITH, getUserAgentHeader())
                .header(HEADER_REQUEST_ID, UUID.randomUUID().toString())
                .header(HEADER_CASSANDRA, token)
                .header(HEADER_AUTHORIZATION, "Bearer " + token)
                .timeout(httpClientOptions.getRequestTimeout())
                .method(method, HttpRequest.BodyPublishers.ofString(body))
                .build();
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL '" + url + "'", e);
        }
    }

    /**
     * Parse HTTP response as a ApiResponseHttp.
     *
     * @param response
     *      http response from the JDK11 client
     * @return
     *      the response as an ApiResponseHttp
     */
    public ApiResponseHttp parseHttpResponse(HttpResponse<String> response) {
        ApiResponseHttp res = new ApiResponseHttp(response.body(), response.statusCode(),
                    response.headers().map().entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> entry.getValue().toString()))
        );
        if (res.getCode() >= 300) {
            log.error("Error for request url={}, method={}, code={}, body={}",
                    response.request().uri().toString(), response.request().method(),
                     res.getCode(), res.getBody());
            processErrors(res);
        }
        return res;
    }

    /**
     * Main Method executing HTTP Request.
     *
     * @param method
     *      http method
     * @param url
     *      url
     * @param token
     *      authentication token
     * @param contentType
     *      request content type
     * @param body
     *      request body
     * @return
     *      basic request
     */
    public ApiResponseHttp executeHttp(final String method,
                                       final String url,
                                       final String token,
                                       String body,
                                       String contentType) {
        HttpRequest httpRequest = builtHttpRequest(method, url, token, body, contentType);
        Status<HttpResponse<String>> status = executeHttpRequest(httpRequest);
        return parseHttpResponse(status.getResult());
    }

    /**
     * Implementing retries.
     *
     * @param req
     *      current request
     * @return
     *      the closeable response
     */
    @SuppressWarnings("unchecked")
    public Status<HttpResponse<String>> executeHttpRequest(HttpRequest req) {
        Callable<HttpResponse<String>> executeRequest = () ->
                httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new CallExecutorBuilder<String>()
                .config(retryConfig)
                .onFailureListener(s -> log.error("Calls failed after {} retries", s.getTotalTries()))
                .afterFailedTryListener(s -> {
                    log.error("Failure on attempt {}/{} ", s.getTotalTries(), retryConfig.getMaxNumberOfTries());
                    log.error("Failed request {} on {}", req.method() , req.uri().toString() );
                    log.error("+ Exception was ", s.getLastExceptionThatCausedRetry());
                })
                .build()
                .execute(executeRequest);
    }

    /**
     * Process ERRORS.Anything above code 300 can be marked as an error Still something
     * 404 is expected and should not result in throwing exception (=not find)
     * @param res HttpResponse
     */
    private void processErrors(ApiResponseHttp res) {
        switch(res.getCode()) {
            // 401
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new DataAPIHttpException("Error Code=" + res.getCode() +
                        ", (HTTP_UNAUTHORIZED) Invalid Credentials Check your token: " +
                        res.getBody());
            case 422:
                throw new DataAPIHttpException("Error Code=" + res.getCode() +
                        "(422) Invalid information provided to create DB: "
                        + res.getBody());
            default:
                if (res.getCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
                    throw new IllegalStateException(res.getBody() + " (http:" + res.getCode() + ")");
                }
                throw new DataAPIException(ERROR_CODE_HTTP, res.getBody() + " (http:" + res.getCode() + ")");
        }
    }


}
