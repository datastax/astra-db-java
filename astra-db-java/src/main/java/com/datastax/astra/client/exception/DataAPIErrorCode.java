package com.datastax.astra.client.exception;

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

@Getter
public enum DataAPIErrorCode {
    ERROR("CLIENT_ERROR"),
    HTTP("CLIENT_HTTP"),
    ENVIRONMENT("ENVIRONMENT"),
    TIMEOUT("CLIENT_TIMEOUT"),
    INTERRUPTED("CLIENT_INTERRUPTED"),
    RANDOM("CLIENT_RANDOM"),
    CURSOR("CLIENT_CURSOR"),

    SERIALIZATION("CLIENT_SERIALIZATION");

    private final String code;

    DataAPIErrorCode(String code) {
        this.code = code;
    }
}
