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

import com.datastax.astra.client.DataAPIDestination;

import static com.datastax.astra.client.exception.ClientErrorCodes.ENV_RESTRICTED_ASTRA;

/**
 * Exception thrown when the environment is invalid.
 */
public class InvalidEnvironmentException extends DataAPIException {

    public InvalidEnvironmentException(ClientErrorCodes code, String message) {
        super(code, message);
    }

    /**
     * Format error message.
     *
     * @param operation
     *      operation returning the error
     * @param currentEnv
     *      current environment
     */
    public static void throwErrorRestrictedAstra(String operation, DataAPIDestination currentEnv) {
        throw new InvalidEnvironmentException(ENV_RESTRICTED_ASTRA,
                String.format(ENV_RESTRICTED_ASTRA.getMessage(), operation, currentEnv.name()));
    }

}
