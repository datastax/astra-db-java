package com.datastax.astra.client.keyspaces;

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

import com.datastax.astra.internal.serializer.tables.RowSerializer;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the keyspace definition with its name and metadata.
 */
@Getter
@Setter
public class KeyspaceInformation {

    /**
     * The name of the keyspace.
     */
    private String name;

    /**
     * The options of the namespace.
     keyspace
    private KeyspaceOptions options;

    /**
     * Default Constructor.
     */
    public KeyspaceInformation() {
        // Left Blank for Jackson
    }

    /**
     * Default Constructor.
     *
     * @param name
     *      create keyspace information with name
     */
    public KeyspaceInformation(String name) {
        this.name = name;
    }

    /**
     * Replication strategies
     */
    @Getter
    public enum ReplicationStrategy {

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
        ReplicationStrategy(String value) {
            this.value = value;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }

}
