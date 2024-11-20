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

import com.datastax.astra.client.core.http.Caller;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.options.TimeoutOptions;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
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

    /** Header param. */
    public static final String HEADER_ACCEPT            = "Accept";

    /** Headers param to insert the token. */
    public static final String HEADER_TOKEN             = "token";

    /** Headers param to insert the conte type. */
    public static final String HEADER_CONTENT_TYPE      = "Content-Type";

    /** Headers param to insert the token for devops API. */
    public static final String HEADER_AUTHORIZATION     = "Authorization";

    /** Headers name to insert the user agent identifying the client. */
    public static final String HEADER_USER_AGENT        = "User-Agent";

    /** Headers param to insert the user agent identifying the client. */
    public static final String HEADER_REQUESTED_WITH    = "X-Requested-With";

    /** JDK11 Http client. */
    protected final HttpClient httpClient;

    /** Http Options. */
    protected final HttpClientOptions httpClientOptions;

    /** Http Options. */
    protected final TimeoutOptions timeoutOptions;

    /** Default retry configuration. */
    protected final RetryConfig retryConfig;

    /**
     * Initialize the instance with all items
     *
     * @param httpClientOptions
     *      http client options
     * @param timeoutOptions
     *     timeout options
     *
     */
    public RetryHttpClient(HttpClientOptions httpClientOptions, TimeoutOptions timeoutOptions) {
        this.httpClientOptions = httpClientOptions;
        this.timeoutOptions    = timeoutOptions;
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(httpClientOptions.getHttpVersion());
        httpClientBuilder.followRedirects(httpClientOptions.getHttpRedirect());
        httpClientBuilder.connectTimeout(Duration.ofMillis(timeoutOptions.getConnectTimeoutMillis()));
        if (httpClientOptions.getHttpProxy() != null) {
            httpClientBuilder.proxy(ProxySelector.of(new InetSocketAddress(
                    httpClientOptions.getHttpProxy().getHostname(),
                    httpClientOptions.getHttpProxy().getPort())));
        }
        httpClient = httpClientBuilder.build();

        retryConfig = new RetryConfigBuilder()
                .retryOnAnyException()
                .withDelayBetweenTries(httpClientOptions.getRetryDelay())
                .withMaxNumberOfTries(httpClientOptions.getRetryCount())
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
