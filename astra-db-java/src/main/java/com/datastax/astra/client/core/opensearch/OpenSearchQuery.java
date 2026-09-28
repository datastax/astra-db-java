package com.datastax.astra.client.core.opensearch;

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

import com.datastax.astra.internal.utils.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for OpenSearch DSL query objects passed to the {@code $search} filter.
 *
 * <p>The resulting object is serialised verbatim as the value of {@code $search} in the
 * Data API {@code filter} block, e.g.:
 * <pre>{@code
 * "filter": {
 *   "$search": { "match_all": {} }
 * }
 * }</pre>
 *
 * <p>Usage examples:
 * <pre>{@code
 * // match_all
 * Filters.search(OpenSearchQuery.matchAll())
 *
 * // full-text match on a field
 * Filters.search(OpenSearchQuery.match("firstName", "Alice"))
 *
 * // term query
 * Filters.search(OpenSearchQuery.term("city", "London"))
 *
 * // bool query
 * Filters.search(OpenSearchQuery.bool()
 *     .must(OpenSearchQuery.match("firstName", "Alice"))
 *     .filter(OpenSearchQuery.term("city", "London")))
 *
 * // pass a raw Map
 * Filters.search(Map.of("match_all", Map.of()))
 * }</pre>
 *
 * @see com.datastax.astra.client.core.query.Filters#search(OpenSearchQuery)
 */
public class OpenSearchQuery {

    /**
     * Internal map that holds the OpenSearch DSL structure.
     * Exposed via {@link #toMap()} for serialisation.
     */
    private final Map<String, Object> queryMap;

    // ------------------------------------------------------------------
    //  Constructors / internal helpers
    // ------------------------------------------------------------------

    /**
     * Private constructor — use the static factory methods.
     *
     * @param queryMap internal DSL map
     */
    private OpenSearchQuery(Map<String, Object> queryMap) {
        this.queryMap = queryMap;
    }

    /**
     * Returns the raw map representation of this OpenSearch query.
     * This is what gets placed as the value of {@code $search}.
     *
     * @return OpenSearch DSL as a {@code Map}
     */
    public Map<String, Object> toMap() {
        return queryMap;
    }

    // ------------------------------------------------------------------
    //  Leaf queries
    // ------------------------------------------------------------------

    /**
     * Creates a {@code match_all} query that matches every document.
     *
     * <p>Wire representation: {@code {"match_all": {}}}
     *
     * @return an {@link OpenSearchQuery} representing {@code match_all}
     */
    public static OpenSearchQuery matchAll() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("match_all", new LinkedHashMap<>());
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code match} query for full-text search on a specific field.
     *
     * <p>Wire representation: {@code {"match": {"<field>": "<query>"}}}
     *
     * @param field the field name to search in
     * @param query the text to search for
     * @return an {@link OpenSearchQuery} representing the {@code match} clause
     */
    public static OpenSearchQuery match(String field, String query) {
        Assert.hasLength(field, "field");
        Assert.hasLength(query, "query");
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put(field, query);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("match", fieldMap);
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code match} query with additional options.
     *
     * <p>Wire representation:
     * {@code {"match": {"<field>": {"query": "<query>", ...options}}}}
     *
     * @param field   the field name to search in
     * @param query   the text to search for
     * @param options additional options (e.g. {@code operator}, {@code fuzziness})
     * @return an {@link OpenSearchQuery} representing the {@code match} clause
     */
    public static OpenSearchQuery match(String field, String query, Map<String, Object> options) {
        Assert.hasLength(field, "field");
        Assert.hasLength(query, "query");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("query", query);
        if (options != null) {
            params.putAll(options);
        }
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put(field, params);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("match", fieldMap);
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code term} query for exact-value matching on a keyword field.
     *
     * <p>Wire representation: {@code {"term": {"<field>": "<value>"}}}
     *
     * @param field the field name
     * @param value the exact value to match
     * @return an {@link OpenSearchQuery} representing the {@code term} clause
     */
    public static OpenSearchQuery term(String field, Object value) {
        Assert.hasLength(field, "field");
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put(field, value);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("term", fieldMap);
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code terms} query matching any of the provided values.
     *
     * <p>Wire representation: {@code {"terms": {"<field>": [<values>]}}}
     *
     * @param field  the field name
     * @param values the values to match against
     * @return an {@link OpenSearchQuery} representing the {@code terms} clause
     */
    public static OpenSearchQuery terms(String field, Object... values) {
        Assert.hasLength(field, "field");
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put(field, values);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("terms", fieldMap);
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code range} query for numerical or date range filtering.
     *
     * <p>Wire representation: {@code {"range": {"<field>": {"gte": <from>, "lte": <to>}}}}
     *
     * @param field the field name
     * @param from  lower bound (inclusive, {@code null} to omit)
     * @param to    upper bound (inclusive, {@code null} to omit)
     * @return an {@link OpenSearchQuery} representing the {@code range} clause
     */
    public static OpenSearchQuery range(String field, Object from, Object to) {
        Assert.hasLength(field, "field");
        Map<String, Object> bounds = new LinkedHashMap<>();
        if (from != null) bounds.put("gte", from);
        if (to   != null) bounds.put("lte", to);
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put(field, bounds);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("range", fieldMap);
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code multi_match} query for searching across multiple fields.
     *
     * <p>Wire representation:
     * {@code {"multi_match": {"query": "<query>", "fields": [<fields>]}}}
     *
     * @param query  the text to search for
     * @param fields the fields to search in
     * @return an {@link OpenSearchQuery} representing the {@code multi_match} clause
     */
    public static OpenSearchQuery multiMatch(String query, String... fields) {
        Assert.hasLength(query, "query");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("query", query);
        params.put("fields", fields);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("multi_match", params);
        return new OpenSearchQuery(map);
    }

    /**
     * Creates a {@code bool} query builder for compound boolean logic.
     *
     * <p>Wire representation:
     * <pre>{@code
     * {
     *   "bool": {
     *     "must":   [...],
     *     "should": [...],
     *     "filter": [...],
     *     "must_not": [...]
     *   }
     * }
     * }</pre>
     *
     * @return a new {@link BoolQuery} builder
     */
    public static BoolQuery bool() {
        return new BoolQuery();
    }

    /**
     * Creates an {@code OpenSearchQuery} from a raw, pre-built map.
     * Use this escape hatch when the DSL query you need is not covered by the
     * typed factory methods.
     *
     * @param rawQuery a map representing the full OpenSearch query object
     * @return an {@link OpenSearchQuery} wrapping the provided map
     */
    public static OpenSearchQuery ofMap(Map<String, Object> rawQuery) {
        Assert.notNull(rawQuery, "rawQuery");
        return new OpenSearchQuery(rawQuery);
    }

    // ------------------------------------------------------------------
    //  Compound: bool builder
    // ------------------------------------------------------------------

    /**
     * Fluent builder for OpenSearch {@code bool} queries.
     *
     * <p>Call {@link #build()} to obtain the final {@link OpenSearchQuery}.
     */
    public static class BoolQuery {

        private final List<Map<String, Object>> must    = new ArrayList<>();
        private final List<Map<String, Object>> should  = new ArrayList<>();
        private final List<Map<String, Object>> filter  = new ArrayList<>();
        private final List<Map<String, Object>> mustNot = new ArrayList<>();

        /** Private constructor — obtain via {@link OpenSearchQuery#bool()}. */
        private BoolQuery() {}

        /**
         * Adds a clause to the {@code must} array (equivalent to logical AND).
         *
         * @param query the sub-query that must match
         * @return this builder
         */
        public BoolQuery must(OpenSearchQuery query) {
            must.add(query.toMap());
            return this;
        }

        /**
         * Adds a clause to the {@code should} array (equivalent to logical OR).
         *
         * @param query the sub-query that should match
         * @return this builder
         */
        public BoolQuery should(OpenSearchQuery query) {
            should.add(query.toMap());
            return this;
        }

        /**
         * Adds a clause to the {@code filter} array (must match, no scoring).
         *
         * @param query the sub-query used as a filter
         * @return this builder
         */
        public BoolQuery filter(OpenSearchQuery query) {
            filter.add(query.toMap());
            return this;
        }

        /**
         * Adds a clause to the {@code must_not} array (must not match).
         *
         * @param query the sub-query that must not match
         * @return this builder
         */
        public BoolQuery mustNot(OpenSearchQuery query) {
            mustNot.add(query.toMap());
            return this;
        }

        /**
         * Builds the {@link OpenSearchQuery} from the accumulated clauses.
         *
         * @return the composed {@code bool} query
         */
        public OpenSearchQuery build() {
            Map<String, Object> boolMap = new LinkedHashMap<>();
            if (!must.isEmpty())    boolMap.put("must",     must);
            if (!should.isEmpty())  boolMap.put("should",   should);
            if (!filter.isEmpty())  boolMap.put("filter",   filter);
            if (!mustNot.isEmpty()) boolMap.put("must_not", mustNot);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("bool", boolMap);
            return new OpenSearchQuery(map);
        }
    }
}
