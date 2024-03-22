package com.datastax.astra.client.model.namespaces;

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

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Options to create a Namespace.
 */
@Data
public class CreateNamespaceOptions {

    /**
     * The replication of the namespace.
     */
    private Map<String, Object> replication;

    /**
     * Default Constructor.
     */
    private CreateNamespaceOptions() {
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
    public static CreateNamespaceOptions simpleStrategy(int replicationFactor) {
        CreateNamespaceOptions options = new CreateNamespaceOptions();
        options.replication.put("class", NamespaceInformation.ReplicationStrategy.SimpleStrategy.name());
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
    public static CreateNamespaceOptions networkTopologyStrategy(Map<String, Integer> datacenters) {
        CreateNamespaceOptions options = new CreateNamespaceOptions();
        options.replication.put("class", NamespaceInformation.ReplicationStrategy.NetworkTopologyStrategy.name());
        options.replication.putAll(datacenters);
        return options;
    }

}
