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

import com.datastax.astra.client.core.http.HttpClientOptions;
import org.apache.hc.core5.http.HttpException;

/**
 * A request to the Data API resulted in an HTTP 4xx or 5xx response.
 * In most cases this comes with additional information: the purpose
 * of this class is to present such information in a structured way,
 * askin to what happens for the DataAPIResponseException, while
 * still raising`.
 */
public class DataAPIHttpException extends DataAPIException {

    /**
     * Constructors providing all arguments and a parent exception.
     *
     * @param errorMessage
     *      error message
     */
    public DataAPIHttpException(String errorMessage) {
        super(DataAPIErrorCode.HTTP, errorMessage);
    }

}
