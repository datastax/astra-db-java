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

import static com.datastax.astra.client.exceptions.ClientErrorCodes.INVALID_FIELD_EXPRESSION;

/**
 * Error raised during escaping or unescaping a field path.
 */
public class InvalidFieldExpressionException extends DataAPIException {

    /**
     * Constructor with code and message
     * @param code
     *      error code
     * @param message
     *      error message
     */
    public InvalidFieldExpressionException(ClientErrorCodes code, String message) {
        super(code, message);
    }

    /**
     * Format error message.
     *
     * @param path
     *      current field expression
     */
    public static void throwInvalidField(String path, String cause) {
        throw new InvalidFieldExpressionException(INVALID_FIELD_EXPRESSION,
                String.format(INVALID_FIELD_EXPRESSION.getMessage() + ":" + cause, path));
    }
}
