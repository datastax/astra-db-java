package com.datastax.astra.client.exceptions;

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
 * Error codes for server-side errors.
 */
@Getter
public enum ErrorCodesServer {

    /**
     * Indicates an error in client-side serialization.
     */
    DESERIALIZATION_ERROR("Cannot deserialize String %s, cause: %s");

    /**
     * The descriptive message associated with the error.
     */
    private final String message;

    /**
     * Constructs a new {@code ClientErrorCodes} instance with the specified code and message.
     *
     * @param message the descriptive message associated with the error
     */
    ErrorCodesServer(String message) {
        this.message = message;
    }
}
