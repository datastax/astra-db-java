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
 * Exception thrown when there is an error in the client side.
 * It can be configuration, connectivity or serialization.
 */
@Getter
public class DataAPIClientException extends DataAPIException {

    /**
     * Constructors providing all arguments and a parent exception.
     *
     * @param arguments
     *      arguments for the error message
     * @param errorCode
     *      error code
     */
    public DataAPIClientException(ErrorCodesClient errorCode, Object... arguments) {
        super(errorCode.name(), String.format(errorCode.getMessage(), arguments));
    }

    /**
     *Constructors providing all arguments and a parent exception.
     *
     * @param customMessage
     *      arguments for the error message
     * @param errorCode
     *      error code
     */
    public DataAPIClientException(ErrorCodesClient errorCode, String customMessage) {
        super(errorCode.name(), customMessage);
    }
}
