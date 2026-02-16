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
public enum ErrorCodesClient {

    /**
     * Indicates that the operation is restricted to Astra environments.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The operation name.</li>
     *   <li>{@code '%s'}: The current environment.</li>
     * </ul>
     */
    ASTRA_RESTRICTED_OPERATION("Operation '%s' available only for Astra environments (current is '%s')"),

    /**
     * Indicates that the database endpoint URL does not match the client's configured environment.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The environment detected from the URL.</li>
     *   <li>{@code '%s'}: The environment configured on the client.</li>
     * </ul>
     */
    ENVIRONMENT_MISMATCH("The database endpoint corresponds to Astra '%s' but the client is configured for '%s'. "
            + "Use DataAPIClientOptions.destination(%s) when creating the DataAPIClient."),

    /**
     * Indicates that a required configuration parameter is missing.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The name of the missing configuration parameter.</li>
     *   <li>{@code '%s'}: The operation requiring the parameter.</li>
     * </ul>
     */
    MISSING_CONFIGURATION( "Configuration parameter is missing : '%s' for operation '%s'"),

    /**
     * Indicates that a required annotation is missing from a bean.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The name of the missing annotation.</li>
     *   <li>{@code '%s'}: The name of the bean.</li>
     *   <li>{@code '%s'}: The operation requiring the annotation.</li>
     * </ul>
     */
    MISSING_ANNOTATION("Annotation '%s' is missing on bean '%s' for operation '%s'"),

    /**
     * Indicates that an annotation on a bean is invalid.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The name of the invalid annotation.</li>
     *   <li>{@code '%s'}: The name of the bean.</li>
     *   <li>{@code '%s'}: The cause of the invalid annotation.</li>
     * </ul>
     */
    INVALID_ANNOTATION("Annotation '%s' on bean '%s' is invalid : cause '%s'"),

    /**
     * Indicates that a field value is invalid.
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The invalid value.</li>
     *   <li>{@code '%s'}: The name of the field.</li>
     * </ul>
     */
    INVALID_VALUE("Invalid value '%s' for field '%s', Cause: %s"),

    /**
     * Indicates a field name was invalid and cannot be escaped
     * Dynamic placeholders:
     * <ul>
     *   <li>{@code '%s'}: The value for the field to escape</li>
     * </ul>
     */
    INVALID_EXPRESSION("Invalid field expression: '%s'"),

    /**
     * Indicates an error in client-side serialization.
     */
    SERIALIZATION_ERROR("Cannot serialize object %s, cause: %s"),

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
    ErrorCodesClient(String message) {
        this.message = message;
    }
}
