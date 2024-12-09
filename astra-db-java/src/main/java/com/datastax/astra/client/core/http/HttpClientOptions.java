package com.datastax.astra.client.core.http;

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

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.internal.utils.Assert;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Options to set up http Client.
 */
@Setter
@Accessors(fluent = true, chain = true)
public class HttpClientOptions implements Cloneable {

    // --------------------------------------------
    // ----------------- HEADERS  -----------------
    // --------------------------------------------

    /** Default caller name. */
    public static final Caller DEFAULT_CALLER = new Caller("astra-db-java",
            DataAPIClientOptions.class.getPackage().getImplementationVersion() != null ?
            DataAPIClientOptions.class.getPackage().getImplementationVersion() : "dev");

    /** Add headers to db calls. */
    List<Caller> callers = new ArrayList<>();

    /**
     * Add a caller.
     *
     * @param caller
     *      caller to add.
     */
    public void addCaller(Caller caller) {
        Assert.notNull(caller, "caller");
        Assert.hasLength(caller.getName(), caller.getVersion());
        callers.add(caller);
    }

    /**
     * Gets callers
     *
     * @return value of callers
     */
    public List<Caller> getCallers() {
        return callers;
    }

    // --------------------------------------------
    // ----------------- RETRIES  -----------------
    // --------------------------------------------

    /** Default retry count. */
    public static final int DEFAULT_RETRY_COUNT = 1;

    /** Default retry delay. */
    public static final int DEFAULT_RETRY_DELAY_MILLIS = 100;

    /**
     * Enable retry count.
     */
    int retryCount = DEFAULT_RETRY_COUNT;

    /**
     * How much to wait in between 2 calls.
     */
    Duration retryDelay = Duration.ofMillis(DEFAULT_RETRY_DELAY_MILLIS);

    /**
     * Set the number of retries and the delay between each retry.
     *
     * @param i
     *      number of retries
     * @param duration
     *      delay between each retry
     * @return
     *      this
     */
    public HttpClientOptions httpRetries(int i, Duration duration) {
        this.retryCount = i;
        this.retryDelay = duration;
        return this;
    }

    /**
     * Gets retryCount
     *
     * @return value of retryCount
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Gets retryDelay
     *
     * @return value of retryDelay
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    // --------------------------------------------
    // ------------- HTTP PROTOCOL ----------------
    // --------------------------------------------

    /**
     * Moving to HTTP/2.
     */
    HttpClient.Version httpVersion = HttpClient.Version.HTTP_2;

    /**
     * Redirect
     */
    HttpClient.Redirect httpRedirect = HttpClient.Redirect.NORMAL;

    /**
     * The http client could work through a proxy.
     */
    HttpProxy httpProxy;

    /**
     * Gets httpVersion
     *
     * @return value of httpVersion
     */
    public HttpClient.Version getHttpVersion() {
        return httpVersion;
    }

    /**
     * Gets httpRedirect
     *
     * @return value of httpRedirect
     */
    public HttpClient.Redirect getHttpRedirect() {
        return httpRedirect;
    }

    /**
     * Gets proxy
     *
     * @return value of proxy
     */
    public HttpProxy getHttpProxy() {
        return httpProxy;
    }

    // --------------------------------------------
    // ------------- INITIALIZATION ---------------
    // --------------------------------------------

    /**
     * Default constructor.
     */
    public HttpClientOptions() {
        callers.add(DEFAULT_CALLER);
    }

    /** {@inheritDoc} */
    @Override
    public HttpClientOptions clone() {
        try {
            HttpClientOptions cloned = (HttpClientOptions) super.clone();
            // Deep copy of mutable fields
            cloned.callers = new ArrayList<>(this.callers);
            cloned.retryDelay = this.retryDelay != null ? Duration.ofMillis(this.retryDelay.toMillis()) : null;
            cloned.httpProxy = this.httpProxy != null ? this.httpProxy.clone() : null;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }


}
