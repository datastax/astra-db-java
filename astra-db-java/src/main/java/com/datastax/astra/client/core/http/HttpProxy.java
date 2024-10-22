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

import lombok.Getter;
import lombok.Setter;

/**
 * Subclass to represent an http proxy.
 */
@Getter
@Setter
public class HttpProxy {

    /** hostname of the proxy. */
    String hostname;

    /** port of the proxy. */
    int port;

    /**
     * Default constructor.
     *
     * @param hostname
     *    host name
     * @param port
     *      roxy port
     */
    public HttpProxy(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }
}
