package com.datastax.astra.client.exceptions;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2026 DataStax
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

import java.util.Optional;
import java.util.UUID;

/**
 * Exception thrown when an error occurs during Astra DevOps API operations.
 * <p>
 * This runtime exception is used to signal failures in interactions with the
 * Astra DevOps API, such as database creation, deletion, or configuration errors.
 * It provides multiple constructors to accommodate different error scenarios,
 * including wrapping underlying exceptions and providing custom error messages.
 * </p>
 *
 * @see RuntimeException
 */
@Getter
public class AstraDevOpsAPIException extends RuntimeException {

    /**
     * Default error message used when no specific message is provided.
     * This message indicates an unexpected error occurred during Astra DevOps API operations.
     */
    public static final String DEFAULT_ERROR_MESSAGE = "Unexpected error occurred for Astra Devops API";

    /**
     * Constructs a new AstraDevOpsAPIException with the specified detail message and cause.
     * <p>
     * This constructor is useful when you want to provide both a descriptive error message
     * and wrap an underlying exception that caused the failure.
     * </p>
     *
     * @param message the detail message explaining the reason for the exception
     * @param parent  the underlying cause of this exception (can be null)
     */
    public AstraDevOpsAPIException(String message, Throwable parent) {
        super(message, parent);
    }

    /**
     * Constructs a new AstraDevOpsAPIException with the specified detail message.
     * <p>
     * Use this constructor when you have a specific error message to communicate
     * but no underlying exception to wrap.
     * </p>
     *
     * @param message the detail message explaining the reason for the exception
     */
    public AstraDevOpsAPIException(String message) {
        super(message);
    }

    /**
     * Constructs a new AstraDevOpsAPIException wrapping an underlying throwable.
     * <p>
     * This constructor is useful when you want to propagate an exception from
     * a lower layer while converting it to an AstraDevOpsAPIException. The message
     * will be derived from the throwable's message.
     * </p>
     *
     * @param throwable the underlying cause of this exception
     */
    public AstraDevOpsAPIException(Throwable throwable) {
        super(throwable);
    }


}
