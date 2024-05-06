package com.datastax.astra.client.model;

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
import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpClient;

/**
 * Options to set up http Client.
 */
@Getter
@Setter
public class HttpClientOptions {

    /**
     * Default user agent.
     */
    public static final String DEFAULT_USER_AGENT = "stargate-sdk";

    /**
     * Default timeout for initiating connection.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 20;

    /**
     * Default timeout for initiating connection.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT_MILLISECONDS = 20000;

    /**
     * Default retry count.
     */
    public static final int DEFAULT_RETRY_COUNT = 3;

    /**
     * Default retry delay.
     */
    public static final int DEFAULT_RETRY_DELAY_MILLIS = 100;

    /**
     * Caller name in User agent.
     */
    String userAgentCallerName = DEFAULT_USER_AGENT;

    /**
     * Caller version in User agent.
     */
    String userAgentCallerVersion = HttpClientOptions.class.getPackage().getImplementationVersion() != null ?
            HttpClientOptions.class.getPackage().getImplementationVersion() : "dev";

    /**
     * Http Connection timeout.
     */
    long connectionRequestTimeoutInSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;

    /**
     * Http Connection timeout.
     */
    long maxTimeMS = DEFAULT_REQUEST_TIMEOUT_MILLISECONDS;

    /**
     * Enable retry count.
     */
    int retryCount = DEFAULT_RETRY_COUNT;

    /**
     * How much to wait in between 2 calls.
     */
    int retryDelay = DEFAULT_RETRY_DELAY_MILLIS;

    /**
     * The http client could work through a proxy.
     */
    DataAPIOptions.HttpProxy proxy;

    /**
     * Moving to HTTP/2.
     */
    HttpClient.Version httpVersion = HttpClient.Version.HTTP_2;

    /**
     * Redirect
     */
    HttpClient.Redirect httpRedirect = HttpClient.Redirect.NORMAL;

    /**
     * Default constructor.
     */
    public HttpClientOptions() {
        // left blanks as default values are set
    }

}
