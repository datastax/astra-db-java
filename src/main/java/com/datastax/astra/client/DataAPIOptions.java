package com.datastax.astra.client;

import com.datastax.astra.internal.http.HttpClientOptions;
import com.datastax.astra.internal.utils.Assert;
import lombok.Getter;

import java.net.http.HttpClient;

/**
 * Options to set up the client for DataApiClient.
 */
@Getter
public class DataAPIOptions {

    /** Number of documents for a count. */
    static final int MAX_DOCUMENTS_COUNT = 1000;

    /** Maximum number of documents in a page. */
    static final  int MAX_PAGE_SIZE = 20;

    /** Maximum number of documents when you insert. */
    static final int MAX_DOCUMENTS_IN_INSERT = 20;

    /** Default user agent. */
    public static final String DEFAULT_CALLER_NAME = "astra-db-java";

    /** Default user agent. */
    public static final String DEFAULT_CALLER_VERSION =
            DataAPIOptions.class.getPackage().getImplementationVersion() != null ?
            DataAPIOptions.class.getPackage().getImplementationVersion() : "dev";

    /** Default timeout for initiating connection. */
    public static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 20;

    /** Default timeout for initiating connection. */
    public static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 20;

    /** Default retry count. */
    public static final int DEFAULT_RETRY_COUNT = 1;

    /** Default retry delay. */
    public static final int DEFAULT_RETRY_DELAY_MILLIS  = 100;

    /** path for json api. */
    public static final String DEFAULT_VERSION = "v1";

    final HttpClientOptions httpClientOptions;

    final DataAPIDestination destination;

    final String apiVersion;

    public static DataAPIClientOptionsBuilder builder() {
        return new DataAPIClientOptionsBuilder();
    }

    private DataAPIOptions(DataAPIClientOptionsBuilder builder) {
        this.apiVersion  = builder.apiVersion;
        this.destination = builder.destination;

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setHttpVersion(builder.httpVersion);
        httpClientOptions.setHttpRedirect(builder.httpRedirect);
        httpClientOptions.setRetryCount(builder.retryCount);
        httpClientOptions.setRetryDelay(builder.retryDelay);
        httpClientOptions.setUserAgentCallerName(builder.userAgentCallerName);
        httpClientOptions.setUserAgentCallerVersion(builder.userAgentCallerVersion);
        httpClientOptions.setConnectionRequestTimeoutInSeconds(builder.httpRequestTimeout);
        httpClientOptions.setResponseTimeoutInSeconds(builder.httpConnectTimeout);
        httpClientOptions.setProxy(builder.httpProxy);
        this.httpClientOptions = httpClientOptions;
    }

    public static class DataAPIClientOptionsBuilder {

        /** Caller name in User agent. */
        private String apiVersion = DEFAULT_VERSION;

        /** Caller name in User agent. */
        private String userAgentCallerName = DEFAULT_CALLER_NAME;

        /** Caller version in User agent. */
        private String userAgentCallerVersion = DEFAULT_CALLER_VERSION;

        /** Http Connection timeout. */
        private long httpRequestTimeout = DEFAULT_CONNECT_TIMEOUT_SECONDS;

        /** Http Connection timeout. */
        private long httpConnectTimeout = DEFAULT_REQUEST_TIMEOUT_SECONDS;

        /** Enable retry count. */
        private int retryCount = DEFAULT_RETRY_COUNT;

        /** How much to wait in between 2 calls. */
        private int retryDelay = DEFAULT_RETRY_DELAY_MILLIS;

        /** The http client could work through a proxy. */
        private HttpClientOptions.HttpProxy httpProxy;

        /** Moving to HTTP/2. */
        private  HttpClient.Version httpVersion = HttpClient.Version.HTTP_2;

        /** Redirect  */
        private  HttpClient.Redirect httpRedirect = HttpClient.Redirect.NORMAL;

        /** Default is to use Astra in Production. */
        private DataAPIDestination destination = DataAPIDestination.ASTRA;

        public DataAPIClientOptionsBuilder withCaller(String callerName, String callerVersion) {
            Assert.hasLength(callerName, callerVersion);
            this.userAgentCallerName    = callerName;
            this.userAgentCallerVersion = callerVersion;
            return this;
        }

        public DataAPIClientOptionsBuilder withApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public DataAPIClientOptionsBuilder withHttpRedirect(HttpClient.Redirect redirect) {
            httpRedirect = redirect;
            return this;
        }

        public DataAPIClientOptionsBuilder withHtpVersion(HttpClient.Version version) {
            this.httpVersion = version;
            return this;
        }

        public DataAPIClientOptionsBuilder withHttpProxy(HttpClientOptions.HttpProxy httpProxy) {
            this.httpProxy = httpProxy;
            return this;
        }

        public DataAPIClientOptionsBuilder withHttpRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public DataAPIClientOptionsBuilder withHttpConnectTimeout(int connecTimeout) {
            this.httpRequestTimeout = connecTimeout;
            return this;
        }

        public DataAPIClientOptionsBuilder withHttpRequestTimeout(int connecTimeout) {
            this.httpConnectTimeout = connecTimeout;
            return this;
        }

        public DataAPIClientOptionsBuilder withDestination(DataAPIDestination destination) {
            this.destination = destination;
            return this;
        }

        /**
         * Sets the delay between retry attempts when the number of retries is greater than 1.
         * This allows configuring the interval to wait before initiating a subsequent retry
         * after a failed attempt. The delay is specified in milliseconds.
         * <p>
         * If not explicitly set, the default delay is defined by {@code DEFAULT_RETRY_DELAY_MILLIS},
         * which is 100 milliseconds.
         * </p>
         * <p>
         * Usage of this method is crucial in scenarios where repeated requests are made and a
         * gentle back-off strategy is desired to reduce the load on the server or to handle temporary
         * network issues gracefully.
         * </p>
         *
         * @param retryDelay the delay between two retry attempts, expressed in milliseconds.
         *                   Must be a non-negative number.
         * @return a reference to this builder, allowing for method chaining.
         * <p></p>
         * <p>
         * Example usage:
         * <pre>
         * {@code
         * DataAPIClientOptions
         *   .builder()
         *   .withHttpRetryDelayMillis(200); // Sets a 200ms delay between retries.
         * }</pre>
         * </p>
         */
        public DataAPIClientOptionsBuilder withHttpRetryDelayMillis(int retryDelay) {
            if (retryDelay < 0) {
                throw new IllegalArgumentException("Retry delay must be non-negative");
            }
            this.retryDelay = retryDelay;
            return this;
        }

        public DataAPIOptions build() {
            return new DataAPIOptions(this);
        }


    }


    /**
     * Retrieve the maximum number of documents that the count function can return.
     * @return
     *      maximum number of document returned
     */
    public static int getMaxDocumentCount() {
        return MAX_DOCUMENTS_COUNT;
    }

    /**
     * Retrieve the maximum number of documents for a page and also the maximum you can set for a limit.
     *
     * @return
     *      maximum page size.
     */
    public static int getMaxPageSize() {
        return MAX_PAGE_SIZE;
    }

    /**
     * Retrieve the maximum number of documents allows for a insertMany() below this point the list is split and chunked are processed in parallel.
     *
     * @return
     *      maximum page size.
     */
    public static int getMaxDocumentsInInsert() {
        return MAX_DOCUMENTS_IN_INSERT;
    }

}