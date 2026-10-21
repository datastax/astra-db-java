package com.datastax.astra.test.integration.utils;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal OpenSearch REST client used by integration tests to check (and clean) what
 * HCD mirrors into OpenSearch. Plain HTTP only, no authentication.
 */
@Slf4j
public class OpenSearchTestClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    /**
     * @param url OpenSearch base url, e.g. {@code http://localhost:9200}
     */
    public OpenSearchTestClient(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("OpenSearch url is required");
        }
        this.baseUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** @return the OpenSearch base url (no trailing slash). */
    public String getUrl() {
        return baseUrl;
    }

    // ------------------------------------------------------------------
    //  Cluster
    // ------------------------------------------------------------------

    /** @return true if the server answers {@code GET /} with a 2xx status. */
    public boolean isAvailable() {
        try {
            return send(builder("/").GET()).statusCode() / 100 == 2;
        } catch (Exception e) {
            return false;
        }
    }

    /** @return cluster health ({@code GET /_cluster/health}). */
    public JsonNode clusterHealth() {
        return get("/_cluster/health");
    }

    // ------------------------------------------------------------------
    //  Indices
    // ------------------------------------------------------------------

    /** @return HTTP status of {@code HEAD /{index}} (200 = exists, 404 = missing). */
    public int indexStatus(String index) {
        return statusOf(builder("/" + index).method("HEAD", HttpRequest.BodyPublishers.noBody()), "HEAD " + index);
    }

    /** @return true if the index exists. */
    public boolean indexExists(String index) {
        return indexStatus(index) == 200;
    }

    /**
     * Delete an index. Failures are logged and swallowed so it is safe in setup/teardown.
     *
     * @return true if the index was deleted, false if it did not exist or the call failed
     */
    public boolean deleteIndex(String index) {
        try {
            return send(builder("/" + index).DELETE()).statusCode() / 100 == 2;
        } catch (Exception e) {
            log.warn("Could not delete OpenSearch index {}: {}", index, e.getMessage());
            return false;
        }
    }

    /** @return {@code _count} of documents in the index. */
    public int count(String index) {
        return get("/" + index + "/_count").path("count").asInt();
    }

    /** @return the {@code index} settings node (number_of_shards, number_of_replicas, ...). */
    public JsonNode settings(String index) {
        return get("/" + index + "/_settings").path(index).path("settings").path("index");
    }

    /** @return the mapping {@code properties} node of the index. */
    public JsonNode mappings(String index) {
        return get("/" + index + "/_mapping").path(index).path("mappings").path("properties");
    }

    /** @return the {@code _cat/indices} row of the index (health, pri, rep, docs.count, ...). */
    public JsonNode catIndex(String index) {
        return get("/_cat/indices/" + index + "?format=json").get(0);
    }

    /** @return health of the index: green, yellow or red. */
    public String health(String index) {
        return catIndex(index).path("health").asText();
    }

    /** @return the {@code hits} node of {@code GET /{index}/_search?size={size}}. */
    public JsonNode search(String index, int size) {
        return get("/" + index + "/_search?size=" + size).path("hits");
    }

    /**
     * Force a refresh of the index so that all documents indexed so far become immediately
     * searchable. Equivalent to {@code POST /{index}/_refresh}.
     */
    public void refresh(String index) {
        try {
            send(builder("/" + index + "/_refresh").POST(HttpRequest.BodyPublishers.noBody()));
        } catch (Exception e) {
            log.warn("Could not refresh OpenSearch index {}: {}", index, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Raw access
    // ------------------------------------------------------------------

    /** GET a path (starting with '/') and parse the JSON response. */
    public JsonNode get(String path) {
        try {
            return JSON.readTree(send(builder(path).GET()).body());
        } catch (Exception e) {
            throw new IllegalStateException("OpenSearch GET " + path + " failed", e);
        }
    }

    // ------------------------------------------------------------------
    //  Internals
    // ------------------------------------------------------------------

    private HttpRequest.Builder builder(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(REQUEST_TIMEOUT);
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private int statusOf(HttpRequest.Builder request, String label) {
        try {
            return send(request).statusCode();
        } catch (Exception e) {
            throw new IllegalStateException("OpenSearch " + label + " failed", e);
        }
    }
}
