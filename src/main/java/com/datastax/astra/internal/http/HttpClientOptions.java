package com.datastax.astra.internal.http;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.net.http.HttpClient;

/**
 * Options to set up http Client.
 */
@Getter @Builder
public class HttpClientOptions {

    /** Default user agent. */
    public static final String DEFAULT_USER_AGENT = "stargate-sdk";

    /** Default timeout for initiating connection. */
    public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 20;

    /** Default timeout for initiating connection. */
    public static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 20;

    /** Default retry count. */
    public static final int DEFAULT_RETRY_COUNT = 3;

    /** Default retry delay. */
    public static final int DEFAULT_RETRY_DELAY_MILLIS  = 100;

    /** Caller name in User agent. */
    @Builder.Default
    String userAgentCallerName = DEFAULT_USER_AGENT;

    /** Caller version in User agent. */
    @Builder.Default
    String userAgentCallerVersion = HttpClientOptions.class.getPackage().getImplementationVersion() != null ?
            HttpClientOptions.class.getPackage().getImplementationVersion() : "dev";

    /** Http Connection timeout. */
    @Builder.Default
    long connectionRequestTimeoutInSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;

    /** Http Connection timeout. */
    @Builder.Default
    long responseTimeoutInSeconds = DEFAULT_REQUEST_TIMEOUT_SECONDS;

    /** Enable retry count. */
    @Builder.Default
    int retryCount = DEFAULT_RETRY_COUNT;

    /** How much to wait in between 2 calls. */
    @Builder.Default
    int retryDelay = DEFAULT_RETRY_DELAY_MILLIS;

    /** The http client could work through a proxy. */
    HttpProxy proxy;

    /** Moving to HTTP/2. */
    @Builder.Default
    HttpClient.Version httpVersion = HttpClient.Version.HTTP_2;

    /** Redirect  */
    @Builder.Default
    HttpClient.Redirect httpRedirect = HttpClient.Redirect.NORMAL;

    @Data @AllArgsConstructor
    public static class HttpProxy {
        String hostname;
        int port;
    }

}
