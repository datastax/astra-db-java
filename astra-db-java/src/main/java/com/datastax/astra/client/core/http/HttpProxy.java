package com.datastax.astra.client.core.http;

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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Subclass to represent an http proxy.
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
public class HttpProxy implements Cloneable {

    /** hostname of the proxy. */
    String hostname;

    /** port of the proxy. */
    int port;

    @Override
    public HttpProxy clone() {
        return new HttpProxy(this.hostname, this.port);
    }

    /**
     * Gets hostname
     *
     * @return value of hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets port
     *
     * @return value of port
     */
    public int getPort() {
        return port;
    }
}
