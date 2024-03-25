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

/**
 * Represents a runtime exception that occurs within the Data API client. This exception
 * is thrown to indicate a problem encountered while performing operations such as querying,
 * updating, or deleting data through the Data API. It acts as a wrapper for lower-level exceptions
 * to provide a more general fault indication to the client code.
 * <p>
 * This exception should be caught and handled by the client application to gracefully
 * manage API-related errors, such as connectivity issues, data format mismatches, or unauthorized
 * access attempts. It is recommended to log the details of this exception and present a user-friendly
 * message to the end-user, if applicable.
 * </p>
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * try {
 *     collection.insertOne(document);
 * } catch (DataApiException e) {
 *     // Handle the exception, e.g., log it or display an error message
 *     log.error("Error performing Data API operation: " + e.getMessage(), e);
 * }
 * }
 * </pre>
 *
 * @see RuntimeException
 */
@Getter
public class DataApiException extends RuntimeException {

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** Default error message. */
    public static final String DEFAULT_ERROR_MESSAGE = "Unexpected error occurred for Data API";

    /** Default error code. */
    public static final String DEFAULT_ERROR_CODE = "CLIENT_ERROR";

    /** Error. */
    private final String errorCode;

    /**
     *Constructors providing all arguments and a parent exception.
     *
     * @param errorMessage
     *      error message
     * @param errorCode
     *      error code
     */
    public DataApiException(String errorCode, String errorMessage) {
        super("[" + errorCode + "] - " + errorMessage);
        this.errorCode = errorCode;
    }

    /**
     * Constructors providing all arguments and a parent exception.
     *
     * @param errorMessage
     *      error message
     * @param errorCode
     *      error code
     * @param parent
     *      parent exception
     */
    public DataApiException(String errorMessage, String errorCode, Throwable parent) {
        super(errorMessage, parent);
        this.errorCode = errorCode;
    }

}
