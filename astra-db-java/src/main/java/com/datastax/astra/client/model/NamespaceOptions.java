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

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static com.datastax.astra.client.model.NamespaceInformation.ReplicationStrategy.NETWORK_TOPOLOGY_STRATEGY;
import static com.datastax.astra.client.model.NamespaceInformation.ReplicationStrategy.SIMPLE_STRATEGY;

/**
 * Options to create a Namespace.
 *
 * @deprecated use {@link KeyspaceOptions} instead
 */
@Deprecated
@Getter
public class NamespaceOptions {

    /**
     * The replication of the namespace.
     */
    private final Map<String, Object> replication;

    /**
     * Default Constructor.
     */
    private NamespaceOptions() {
        replication = new HashMap<>();
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
    public static NamespaceOptions simpleStrategy(int replicationFactor) {
        NamespaceOptions options = new NamespaceOptions();
        options.replication.put("class", SIMPLE_STRATEGY.getValue());
        options.replication.put("replication_factor", replicationFactor);
        return options;
    }

    /**
     * Enforce the creation of a namespace with NetworkTopology strategy.
     *
     * @param datacenters
     *      list of datacenters with replication factors
     * @return
     *      instance of the options populated
     */
    public static NamespaceOptions networkTopologyStrategy(Map<String, Integer> datacenters) {
        NamespaceOptions options = new NamespaceOptions();
        options.replication.put("class", NETWORK_TOPOLOGY_STRATEGY.getValue());
        options.replication.putAll(datacenters);
        return options;
    }

}
