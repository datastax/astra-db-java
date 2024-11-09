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

import com.datastax.astra.client.core.options.DataAPIOptions;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Options to set up http Client.
 */
@Getter
public class HttpClientOptions implements Cloneable {

    // --------------------------------------------
    // ----------------- HEADERS  -----------------
    // --------------------------------------------

    /** Default caller name. */
    public static final Caller DEFAULT_CALLER = new Caller("astra-db-java",
            DataAPIOptions.class.getPackage().getImplementationVersion() != null ?
            DataAPIOptions.class.getPackage().getImplementationVersion() : "dev");

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

    // --------------------------------------------
    // ----------------- RETRIES  -----------------
    // --------------------------------------------

    /** Default retry count. */
    public static final int DEFAULT_RETRY_COUNT = 3;

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
     * Add a caller.
     *
     * @param count
     *      retry count
     * @param delay
     *      retry delay
     */
    public HttpClientOptions withRetries(int count, Duration delay) {
        this.retryCount = count;
        this.retryDelay = delay;
        return this;
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
    HttpProxy proxy;


    /**
     * Provide a custom redirect policy.
     *
     * @param redirect
     *      redirect policy
     * @return
     *      self reference
     */
    public HttpClientOptions withHttpRedirect(HttpClient.Redirect redirect) {
        Assert.notNull(redirect, "redirect");
        this.httpRedirect = redirect;
        return this;
    }

    /**
     * Provide a custom http version.
     *
     * @param version
     *      http version
     * @return
     *      self reference
     */
    public HttpClientOptions withHttpVersion(HttpClient.Version version) {
        Assert.notNull(version, "version");
        this.httpVersion = version;
        return this;
    }

    /**
     * Provide a proxy for HTTP connection.
     *
     * @param proxy
     *      http proxy
     * @return
     *      self reference
     */
    public HttpClientOptions withHttpProxy(HttpProxy proxy) {
        this.proxy = proxy;
        return this;
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

    @Override
    public HttpClientOptions clone() {
        try {
            HttpClientOptions cloned = (HttpClientOptions) super.clone();
            // Deep copy of mutable fields
            cloned.callers = new ArrayList<>(this.callers);
            cloned.retryDelay = this.retryDelay != null ? Duration.ofMillis(this.retryDelay.toMillis()) : null;
            cloned.proxy = this.proxy != null ? this.proxy.clone() : null;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

}
