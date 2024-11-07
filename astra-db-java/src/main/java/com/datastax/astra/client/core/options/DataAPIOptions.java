package com.datastax.astra.client.core.options;

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

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.core.auth.EmbeddingAPIKeyHeaderProvider;
import com.datastax.astra.client.core.auth.EmbeddingHeadersProvider;
import com.datastax.astra.client.core.http.Caller;
import com.datastax.astra.client.core.http.HttpClientOptions;
import com.datastax.astra.client.core.http.HttpProxy;
import com.datastax.astra.internal.command.CommandObserver;
import com.datastax.astra.internal.command.LoggingCommandObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Options to set up the client for DataApiClient.
 */
@Slf4j
@Getter
public class DataAPIOptions {

    /** Feature Flag Tables. */
    public static final String HEADER_FEATURE_FLAG_TABLES = "Feature-Flag-tables";

    /** path for json api. */
    public static final String DEFAULT_VERSION = "v1";

    /** Number of documents for a count. */
    public static final int DEFAULT_MAX_COUNT = 1000;

    /** Maximum number of documents in a page. */
    public static final  int DEFAULT_MAX_PAGE_SIZE = 20;

    /** Maximum number of documents when you insert. */
    public static final int DEFAULT_MAX_CHUNK_SIZE = 100;

    /** When operating a count operation, the maximum number of documents that can be returned. */
    final int maxRecordCount;

    /** The maximum number of documents that can be returned in a single page. */
    final int maxPageSize;

    /** The maximum number of documents that can be inserted in a single operation. */
    final int maxRecordsInInsert;

    /** Set the API version like 'v1' */
    final String apiVersion;

    final TimeoutOptions timeoutOptions;

    /** Group options and parameters for http client. */
    final HttpClientOptions httpClientOptions;

    /** Encode the destination like Astra or local installation. */
    final DataAPIDestination destination;

    /** Embedding auth provider. */
    final EmbeddingHeadersProvider embeddingAuthProvider;

    /** Add headers to db calls. */
    final Map<String, String> databaseAdditionalHeaders;

    /** Add headers to admin calls. */
    final Map<String, String> adminAdditionalHeaders;

    /** Observers for the commands. */
    final Map<String, CommandObserver> observers;

    /**
     * Initializer for the builder.
     *
     * @return
     *      a new instance of builder
     */
    public static DataAPIClientOptionsBuilder builder() {
        return new DataAPIClientOptionsBuilder();
    }

    /**
     * Hidden constructor with the builder to build immutable class.
     *
     * @param builder
     *      current builder
     */
    private DataAPIOptions(DataAPIClientOptionsBuilder builder) {
        this.apiVersion                = builder.apiVersion;
        this.destination               = builder.destination;
        this.maxRecordCount            = builder.maxDocumentCount;
        this.maxPageSize               = builder.maxPageSize;
        this.maxRecordsInInsert        = builder.maxDocumentsInInsert;
        this.embeddingAuthProvider     = builder.embeddingAuthProvider;
        this.httpClientOptions         = builder.httpClientOptions;
        this.observers                 = builder.observers;
        this.databaseAdditionalHeaders = builder.databaseAdditionalHeaders;
        this.adminAdditionalHeaders    = builder.adminAdditionalHeaders;
        this.timeoutOptions = new TimeoutOptions();
    }

    /**
     * Builder for the DataAPIClientOptions.
     */
    public static class DataAPIClientOptionsBuilder {

        /** Caller name in User agent. */
        private String apiVersion = DEFAULT_VERSION;

        /** When operating a count operation, the maximum number of documents that can be returned. */
        private int maxDocumentCount = DEFAULT_MAX_COUNT;

        /** The maximum number of documents that can be returned in a single page. */
        private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;

        /** The maximum number of documents that can be inserted in a single operation. */
        private int maxDocumentsInInsert = DEFAULT_MAX_CHUNK_SIZE;

        /** Encode the destination like Astra or local installation. */
        private DataAPIDestination destination = DataAPIDestination.ASTRA;

        /** The embedding service API key can be provided at top level. */
        private EmbeddingHeadersProvider embeddingAuthProvider;

        /** Add headers to admin calls. */
        final Map<String, String> databaseAdditionalHeaders = new HashMap<>();

        /** Add headers to admin calls. */
        final Map<String, String> adminAdditionalHeaders = new HashMap<>();

        /** Observers for the commands. */
        private final Map<String, CommandObserver> observers = new TreeMap<>();

        /** client options. */
        private HttpClientOptions httpClientOptions = new HttpClientOptions();

        /**
         * Default constructor.
         */
        public DataAPIClientOptionsBuilder() {
        }

        /**
         * Builder pattern, update api version.
         *
         * @param apiVersion
         *      api version
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }


        /**
         * Sets the maximum number of documents that can be returned by the count function.
         *
         * <p>
         * If not explicitly set, the default value is defined by {@code MAX_DOCUMENTS_COUNT},
         * which is 1000 documents.
         * </p>
         *
         * @param maxDocumentCount the maximum number of documents that can be returned by the count function.
         *                         Must be a positive number.
         * @return a reference to this builder, allowing for method chaining.
         *
         * Example usage:
         * <pre>
         * {@code
         * DataAPIClientOptions
         *   .builder()
         *   .withMaxDocumentCount(2000); // Sets the maximum number of documents to 2000.
         * }</pre>
         */
        public DataAPIClientOptionsBuilder withMaxDocumentCount(int maxDocumentCount) {
            if (maxDocumentCount <= 0) {
                throw new IllegalArgumentException("Max document count must be a positive number");
            }
            if (maxDocumentCount > DEFAULT_MAX_COUNT) {
                log.warn("Setting the maximum document count to a value greater than the default value of {} may impact performance.", DEFAULT_MAX_COUNT);
            }
            this.maxDocumentCount = maxDocumentCount;
            return this;
        }

        /**
         * Sets the maximum number of documents that can be returned in a single page.
         * <p>
         * If not explicitly set, the default value is defined by {@code MAX_PAGE_SIZE},
         * which is 20 documents.
         * </p>
         *
         * @param maxPageSize the maximum number of documents that can be returned in a single page.
         *                    Must be a positive number.
         * @return a reference to this builder, allowing for method chaining.
         *
         * Example usage:
         * <pre>
         * {@code
         * DataAPIClientOptions
         *   .builder()
         *   .withMaxPageSize(50); // Sets the maximum page size to 50 documents.
         * }</pre>
         */
        public DataAPIClientOptionsBuilder withMaxPageSize(int maxPageSize) {
            if (maxPageSize <= 0) {
                throw new IllegalArgumentException("Max page size must be a positive number");
            }
            if (maxPageSize > DEFAULT_MAX_PAGE_SIZE) {
                log.warn("Setting the maximum page size to a value greater than the " +
                        "default value of {} may impact performance or result in error at server level", DEFAULT_MAX_PAGE_SIZE);
            }
            this.maxPageSize = maxPageSize;
            return this;
        }

        /**
         * Sets the maximum number of documents that can be inserted in a single operation.
         * <p>
         * If not explicitly set, the default value is defined by {@code MAX_DOCUMENTS_IN_INSERT},
         * which is 20 documents.
         * </p>
         *
         * @param maxDocumentsInInsert the maximum number of documents that can be inserted in a single operation.
         *                             Must be a positive number.
         * @return a reference to this builder, allowing for method chaining.
         *
         * Example usage:
         * <pre>
         * {@code
         * DataAPIClientOptions
         *   .builder()
         *   .withMaxDocumentsInInsert(50); // Sets the maximum number of documents to insert to 50.
         * }</pre>
         */
        public DataAPIClientOptionsBuilder withMaxDocumentsInInsert(int maxDocumentsInInsert) {
            if (maxDocumentsInInsert <= 0) {
                throw new IllegalArgumentException("Max documents in insert must be a positive number");
            }
            if (maxDocumentsInInsert > DEFAULT_MAX_CHUNK_SIZE) {
                log.warn("Setting the maximum number of documents in insert to a value greater than the " +
                        "default value of {} may impact performance or result in error at server level", DEFAULT_MAX_CHUNK_SIZE);
            }
            this.maxDocumentsInInsert = maxDocumentsInInsert;
            return this;
        }

        /**
         * Allow to register a listener for the command.
         * @param name
         *      name of the observer
         * @param observer
         *     observer to register
         * @return
         *    instance of the command options
         */
        public DataAPIClientOptionsBuilder withObserver(String name, CommandObserver observer) {
            observers.put(name, observer);
            return this;
        }

        /**
         * Register an observer with its className.
         *
         * @param observer
         *      command observer
         * @return
         *      instance of the command options
         */
        public DataAPIClientOptionsBuilder withObserver(CommandObserver observer) {
            return withObserver(observer.getClass().getSimpleName(), observer);
        }

        /**
         * Help to enable loggin requests.
         *
         * @return      current reference
         *
         */
        public DataAPIClientOptionsBuilder logRequests() {
            return withObserver(new LoggingCommandObserver(DataAPIClient.class));
        }

        /**
         * Builder pattern, update http connection Timeout
         *
         * @param destination
         *      data api destination
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withDestination(DataAPIDestination destination) {
            this.destination = destination;
            return this;
        }

        // --------------------------------------------
        // ----------------- HEADERS  -----------------
        // --------------------------------------------

        /**
         * Builder pattern, update http connection Timeout
         *
         * @param embeddingAPIKey
         *      embedding API Key
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withEmbeddingAPIKey(String embeddingAPIKey) {
            return withEmbeddingAuthProvider(new EmbeddingAPIKeyHeaderProvider(embeddingAPIKey));
        }

        /**
         * Builder pattern, update authentication provider for vectorize.
         *
         * @param embeddingAuthProvider
         *      embedding authentication provider
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withEmbeddingAuthProvider(EmbeddingHeadersProvider embeddingAuthProvider) {
            this.embeddingAuthProvider = embeddingAuthProvider;
            return this;
        }

        /**
         * Builder pattern, update caller information.
         *o
         * @param name
         *      caller name in the user agent
         * @param version
         *      caller version in the user agent
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder addCaller(String name, String version) {
            httpClientOptions.addCaller(new Caller(name, version));
            return this;
        }

        /**
         * Add a header to the db calls.
         *
         * @param key
         *      key
         * @param value
         *      value
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder addDatabaseAdditionalHeader(String key, String value) {
            databaseAdditionalHeaders.put(key, value);
            return this;
        }

        public DataAPIClientOptionsBuilder enableFeatureFlagTables() {
            return addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, "true");
        }

        public DataAPIClientOptionsBuilder disableFeatureFlagTables() {
            return addDatabaseAdditionalHeader(HEADER_FEATURE_FLAG_TABLES, null);
        }

        /**
         * Add a header to the admin calls.
         *
         * @param key
         *      key
         * @param value
         *      value
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder addAdminAdditionalHeader(String key, String value) {
            adminAdditionalHeaders.put(key, value);
            return this;
        }

        /**
         * Set the connection timeout.
         *
         */
        public DataAPIClientOptionsBuilder withHttpRequestTimeout(Duration requestTimeout) {
            httpClientOptions.withRequestTimeout(requestTimeout);
            return this;
        }

        /**
         * Set the connection timeout.
         *
         */
        public DataAPIClientOptionsBuilder withHttConnectTimeout(Duration connectionTimeout) {
            httpClientOptions.withConnectTimeout(connectionTimeout);
            return this;
        }

        public DataAPIClientOptionsBuilder withHttpRetries(int count, Duration delay) {
            httpClientOptions.withRetries(count, delay);
            return this;
        }

        /**
         * Builder pattern, update http redirect
         *
         * @param redirect
         *      http redirect
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withHttpRedirect(HttpClient.Redirect redirect) {
            httpClientOptions.withHttpRedirect(redirect);
            return this;
        }

        /**
         * Builder pattern, update http redirect
         *
         * @param httpClientOptions
         *      all options for http client
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withHttpClientOptions(HttpClientOptions httpClientOptions) {
            this.httpClientOptions = httpClientOptions;
            return this;
        }

        /**
         * Builder pattern, update http version
         *
         * @param version
         *      http version
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withHttpVersion(HttpClient.Version version) {
            httpClientOptions.withHttpVersion(version);
            return this;
        }

        /**
         * Builder pattern, update http httpProxy
         *
         * @param httpProxy
         *      http proxy
         * @return
         *      self reference
         */
        public DataAPIClientOptionsBuilder withHttpProxy(HttpProxy httpProxy) {
            httpClientOptions.withHttpProxy(httpProxy);
            return this;
        }

        /**
         * Build the options.
         *
         * @return
         *      options
         */
        public DataAPIOptions build() {
            return new DataAPIOptions(this);
        }
    }

}
