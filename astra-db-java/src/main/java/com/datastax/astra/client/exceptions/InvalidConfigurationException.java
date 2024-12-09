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

import static com.datastax.astra.client.exceptions.ClientErrorCodes.CONFIG_MISSING;
import static com.datastax.astra.client.exceptions.ClientErrorCodes.MISSING_ANNOTATION;

/**
 * Exception thrown when the configuration is invalid.
 */
public class InvalidConfigurationException extends DataAPIException {

    /**
     * Constructor with code and message
     * @param code
     *      error code
     * @param message
     *      error message
     */
    public InvalidConfigurationException(ClientErrorCodes code, String message) {
        super(code, message);
    }

    /**
     * Format error message.
     *
     * @param operation
     *      operation returning the error
     * @param configParameter parameter
     *      current environment
     */
    public static void throwErrorMissingConfiguration(String operation, String configParameter) {
        throw new InvalidConfigurationException(CONFIG_MISSING,
                String.format(CONFIG_MISSING.getMessage(), configParameter, operation));
    }

    /**
     * Format error message.
     *
     * @param annotation
     *      annotation
     * @param bean
     *      bean
     * @param operation
     *      operation
     */
    public static void throwErrorMissingAnnotation(String annotation, String bean, String operation) {
        throw new InvalidConfigurationException(MISSING_ANNOTATION,
                String.format(MISSING_ANNOTATION.getMessage(), annotation, bean, operation));
    }
}
