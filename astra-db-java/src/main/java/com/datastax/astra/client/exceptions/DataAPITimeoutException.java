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

/**
 * A request to the Data API resulted in an HTTP 4xx or 5xx response.
 * In most cases this comes with additional information: the purpose
 * of this class is to present such information in a structured way,
 * asking to what happens for the DataAPIResponseException, while
 * still raising`.
 */
public class DataAPITimeoutException extends DataAPIHttpException {

    /**
     * Constructors providing all arguments and a parent exception.
     *
     * @param errorMessage
     *      error message
     */
    public DataAPITimeoutException(String errorMessage) {
        super(ERROR_CODE_TIMEOUT, errorMessage);
    }

}
