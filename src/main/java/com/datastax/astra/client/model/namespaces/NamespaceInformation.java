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

import com.datastax.astra.internal.utils.JsonUtils;
import lombok.Data;

/**
 * Represents the Namespace (keyspac) definition with its name and metadata.
 */
@Data
public class NamespaceInformation {

    /**
     * Replication strategies
     */
    public enum ReplicationStrategy {

        /**
         * dev
         */
        SimpleStrategy,

        /**
         * prod
         */
        NetworkTopologyStrategy
    }

    /**
     * The name of the namespace.
     */
    private String name;

    /**
     * The options of the namespace.
     */
    private CreateNamespaceOptions options;

    /**
     * Default Constructor.
     */
    public NamespaceInformation() {
    }

    /**
     * Default Constructor.
     *
     * @param name
     *      create namespace information with name
     */
    public NamespaceInformation(String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JsonUtils.marshallForDataApi(this);
    }

}
