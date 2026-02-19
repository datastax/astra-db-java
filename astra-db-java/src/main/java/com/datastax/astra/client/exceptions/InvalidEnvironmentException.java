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

import com.datastax.astra.client.DataAPIDestination;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.datastax.astra.client.exceptions.ErrorCodesClient.ASTRA_RESTRICTED_OPERATION;
import static com.datastax.astra.client.exceptions.ErrorCodesClient.ENVIRONMENT_MISMATCH;

/**
 * Exception thrown when the environment is invalid.
 */
public class InvalidEnvironmentException extends DataAPIClientException {

    private static final Logger log = LoggerFactory.getLogger(InvalidEnvironmentException.class);

    /**
     * Constructor with code and message
     * @param code
     *      error code
     * @param message
     *      error message
     */
    public InvalidEnvironmentException(ErrorCodesClient code, String message) {
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
        throw new InvalidEnvironmentException(ASTRA_RESTRICTED_OPERATION,
                String.format(ASTRA_RESTRICTED_OPERATION.getMessage(), operation, currentEnv.name()));
    }

    /**
     * Throw when the database URL environment does not match the client's configured destination.
     *
     * @param urlEnv
     *      environment detected from the URL
     * @param clientDestination
     *      destination configured on the client
     */
    public static void throwErrorEnvironmentMismatch(AstraEnvironment urlEnv, DataAPIDestination clientDestination) {
        // Suggest the correct DataAPIDestination for the URL environment
        String suggestion = switch (urlEnv) {
            case PROD -> "DataAPIDestination.ASTRA";
            case DEV  -> "DataAPIDestination.ASTRA_DEV";
            case TEST -> "DataAPIDestination.ASTRA_TEST";
        };
        // As of 2.1 we are not enforcing environment matching, so we will log a warning instead of throwing an exception.
        log.warn("Environment mismatch: URL environment is {}, but client destination is {}. " +
                "Suggested destination for this URL environment is {}. This warning will become an error in a " +
                "future release, please update your client configuration to match the URL environment.",
                urlEnv.name(), clientDestination.name(), suggestion);
        //throw new InvalidEnvironmentException(ENVIRONMENT_MISMATCH,
        //        String.format(ENVIRONMENT_MISMATCH.getMessage(),
        //                urlEnv.name(), clientDestination.name(), suggestion));
    }

}
