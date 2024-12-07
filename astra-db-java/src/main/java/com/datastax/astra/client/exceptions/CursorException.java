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
 * Represents an exception that occurs when there is an error related to a database cursor.
 * This exception extends {@link DataAPIException} and provides additional context about
 * the state of the cursor when the error occurred.
 */
@Getter
public class CursorException extends DataAPIException {

    /**
     * The state of the cursor when the exception was thrown.
     */
    private final String state;

    /**
     * Constructs a new {@code CursorException} with the specified error message and cursor state.
     *
     * @param errorMessage a descriptive error message
     * @param state        the state of the cursor at the time of the error
     */
    public CursorException(String errorMessage, String state) {
        super(ClientErrorCodes.ERROR, errorMessage);
        this.state = state;
    }
}