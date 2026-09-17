package com.datastax.astra.client.collections.definition;

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

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Options for configuring HCD OpenSearch replication on a collection.
 * <p>
 * When {@code enabled} is {@code true}, the Data API creates a Cassandra custom index
 * ({@code CREATE CUSTOM INDEX … USING 'OpenSearchIndex'}) alongside the normal collection
 * table so every document write is automatically mirrored to an OpenSearch index by HCD.
 * <p>
 * The {@code mappings} field is <strong>required</strong> when {@code enabled} is
 * {@code true}.  It must list every top-level document field that should be replicated to
 * OpenSearch (excluding {@code _id}, which is reserved and injected automatically).
 */
@Setter
@Accessors(fluent = true, chain = true)
public class OpenSearchOptions {

    /**
     * Whether OpenSearch replication is enabled for this collection.
     * Must be {@code true} to activate the feature; {@code false} (or omitting the
     * {@code openSearch} block entirely) is a no-op.
     */
    private boolean enabled = true;

    /**
     * The OpenSearch index name ({@code 'indexName'} in the CQL {@code WITH OPTIONS} block).
     * When omitted the Data API defaults to {@code hcd_<keyspace>_<collectionName>}.
     * Note: the <em>SAI index name</em> (the Cassandra custom-index identifier used in
     * {@code expr(...)}) is always auto-generated as {@code hcd_<keyspace>_<collectionName>}
     * and cannot be configured here.
     */
    private String indexName;

    /**
     * Number of primary shards for the OpenSearch index.
     * When omitted, HCD uses its own default (currently {@code 1}).
     */
    private Integer numShards;

    /**
     * Number of replicas for the OpenSearch index.
     * When omitted, HCD uses its own default.
     */
    private Integer numReplicas;

    /**
     * Map of top-level document field name → OpenSearch field definition.
     * <p>
     * The Data API derives {@code propertiesFromJsonFields} (the key set) and
     * {@code customMappingsJson} from this single field.
     * <strong>{@code _id} must never appear as a key</strong> — it is a reserved
     * OpenSearch metadata field injected automatically.
     * <p>
     * Example entry: {@code "title" → {"type":"text","analyzer":"english"}}
     */
    private Map<String, Object> mappings;

    /**
     * Default constructor.
     */
    public OpenSearchOptions() {}

    /** @return whether OpenSearch replication is enabled */
    public boolean isEnabled() { return enabled; }

    /** @return the explicit OS index name, or {@code null} if using the default */
    public String getIndexName() { return indexName; }

    /** @return the number of primary shards, or {@code null} to use the HCD default */
    public Integer getNumShards() { return numShards; }

    /** @return the number of replicas, or {@code null} to use the HCD default */
    public Integer getNumReplicas() { return numReplicas; }

    /** @return the field-name → OpenSearch field-definition mappings */
    public Map<String, Object> getMappings() { return mappings; }
}
