package com.datastax.astra.client.exceptions;

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

/**
 * Represents client error codes used to standardize error reporting in the application.
 * Each error code is associated with a unique identifier (`code`) and a descriptive message (`message`).
 * <p>
 * This enum is designed to facilitate consistent error handling and logging across various components.
 * Some codes are pre-configured with detailed messages that accept placeholders for dynamic values,
 * while others are placeholders for future implementation.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * String errorCode = ClientErrorCodes.CONFIG_MISSING.getCode();
 * String errorMessage = String.format(ClientErrorCodes.CONFIG_MISSING.getMessage(), "paramName", "operationName");
 * }
 * </pre>
 */
@Getter
public enum ClientErrorCodes {

    /**
     * Indicates that the operation is restricted to Astra environments.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The operation name.</li>
     *   <li>{@code '%s'}: The current environment.</li>
     * </ul>
     */
    ENV_RESTRICTED_ASTRA("ASTRA_RESTRICTED", "Operation '%s' available only for Astra environments (current is '%s')"),

    /**
     * Indicates that a required configuration parameter is missing.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The name of the missing configuration parameter.</li>
     *   <li>{@code '%s'}: The operation requiring the parameter.</li>
     * </ul>
     */
    CONFIG_MISSING("CLIENT_CONFIG_MISSING", "Configuration parameter is missing : '%s' for operation '%s'"),

    /**
     * Indicates that a required annotation is missing from a bean.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The name of the missing annotation.</li>
     *   <li>{@code '%s'}: The name of the bean.</li>
     *   <li>{@code '%s'}: The operation requiring the annotation.</li>
     * </ul>
     */
    MISSING_ANNOTATION("CLIENT_MISSING_ANNOTATION", "Annotation '%s' is missing on bean '%s' for operation '%s'"),

    /**
     * Generic client error with no specific message.
     */
    ERROR("CLIENT_ERROR", ""),

    /**
     * Indicates an HTTP error encountered by the client.
     */
    HTTP("CLIENT_HTTP", ""),

    /**
     * Indicates a timeout error encountered by the client.
     */
    TIMEOUT("CLIENT_TIMEOUT", ""),

    /**
     * Indicates an operation was interrupted.
     */
    INTERRUPTED("CLIENT_INTERRUPTED", ""),

    /**
     * Placeholder for random client errors.
     */
    RANDOM("CLIENT_RANDOM", ""),

    /**
     * Indicates an error related to cursor operations.
     */
    CURSOR("CLIENT_CURSOR", ""),

    /**
     * Indicates an error in client-side serialization.
     */
    SERIALIZATION("CLIENT_SERIALIZATION", "CLIENT_SERIALIZATION");

    /**
     * The unique code representing the error.
     */
    private final String code;

    /**
     * The descriptive message associated with the error.
     */
    private final String message;

    /**
     * Constructs a new {@code ClientErrorCodes} instance with the specified code and message.
     *
     * @param code    the unique code representing the error
     * @param message the descriptive message associated with the error
     */
    ClientErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
