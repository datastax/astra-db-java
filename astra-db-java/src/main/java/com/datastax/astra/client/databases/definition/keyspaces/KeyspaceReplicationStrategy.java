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

import lombok.Getter;

/**
 * Available Keyspace replication strategy.
 */
@Getter
public enum KeyspaceReplicationStrategy {

    /**
     * The simple strategy, for development environments.
     */
    SIMPLE_STRATEGY("SimpleStrategy"),

    /**
     * The network topology strategy, for production environments.
     */
    NETWORK_TOPOLOGY_STRATEGY("NetworkTopologyStrategy");

    /**
     * Enum value
     */
    private final String value;

    /**
     * Constructor.
     *
     * @param value
     *      value for the replication
     */
    KeyspaceReplicationStrategy(String value) {
        this.value = value;
    }
}
