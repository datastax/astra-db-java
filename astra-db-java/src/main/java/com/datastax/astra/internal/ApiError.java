package com.datastax.astra.internal;

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

import lombok.Data;

/**
 * Encapsulates error details from a JSON API response. This class is used to represent error information
 * such as messages, codes, and exception classes associated with an API request failure. It allows for
 * a structured way to convey error details from the server to the client.
 */
@Data
public class ApiError {

    /**
     * The detailed message describing the error. This message is intended to provide developers and
     * end-users with understandable information about what went wrong during the API call.
     */
    String message;

    /**
     * A specific code associated with the error, allowing for easier identification and handling of different
     * types of errors. Error codes are typically defined by the API and can be used for programmatic responses
     * to specific conditions.
     */
    String errorCode;

    /**
     * The name of the exception class that was thrown, providing insight into the type of error that occurred.
     * This can be useful for debugging purposes, as it gives developers a hint about the underlying cause of the error.
     */
    String exceptionClass;

    /**
     * Default constructor for {@link ApiError}. Initializes a new instance of the class without setting any properties.
     * Properties should be set via their setters or directly, depending on the usage context and the framework's conventions.
     */
    public ApiError() {}

    /**
     * Constructs a comprehensive error message by combining the exception class name, error code, and the detailed message.
     * This method is useful for logging or displaying a complete error message to the end-user or developer.
     *
     * @return A concatenated string representing the full error message, which may include the exception class name,
     * the error code in parentheses, and the detailed error message. Each element is included only if it is not {@code null}.
     */
    public String getErrorMessage() {
        StringBuilder sb = new StringBuilder();
        if (exceptionClass != null) {
            sb.append(exceptionClass).append(": ");
        }
        if (errorCode != null) {
            sb.append("(").append(errorCode).append(") ");
        }
        if (message != null) {
            sb.append(message);
        }
        return sb.toString().trim();
    }
}
