package com.datastax.astra.client.databases.definition.keyspaces;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class KeyspaceDefinition {

    /**
     * The name of the keyspace.
     */
    private String name = DataAPIClientOptions.DEFAULT_KEYSPACE;

    /**
     * The replication of the namespace.
     */
    private Map<String, Object> replication;

    /**
     * Name for the keyspace.
     *
     * @param name
     *     keyspace name
     * @return
     *      instance of the options populated
     *
     */
    public KeyspaceDefinition name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Enforce the creation of a namespace with SimpleStrategy.
     *
     * @param replicationFactor
     *      replication factor
     * @return
     *      instance of the options populated
     *
     */
    public KeyspaceDefinition simpleStrategy(int replicationFactor) {
        if (replication == null) {
            replication = new HashMap<>();
        }
        replication.put("class", KeyspaceReplicationStrategy.SIMPLE_STRATEGY.getValue());
        replication.put("replication_factor", replicationFactor);
        return this;
    }

    /**
     * Enforce the creation of a namespace with NetworkTopology strategy.
     *
     * @param datacenters
     *      list of datacenters with replication factors
     * @return
     *      instance of the options populated
     */
    public KeyspaceDefinition networkTopologyStrategy(Map<String, Integer> datacenters) {
        if (replication == null) {
            replication = new HashMap<>();
        }
        replication.put("class", KeyspaceReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY.getValue());
        replication.putAll(datacenters);
        return this;
    }

}
