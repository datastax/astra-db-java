package com.datastax.astra.client.collections.exceptions;

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

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.exceptions.DataAPIClientException;
import com.datastax.astra.client.exceptions.ErrorCodesClient;
import com.datastax.astra.client.exceptions.DataAPIException;

/**
 * Error when too many documents in the collection
 */
public class TooManyDocumentsToCountException extends DataAPIClientException {

    /**
     * Default constructor.
     */
    public TooManyDocumentsToCountException() {
        super(ErrorCodesClient.INVALID_VALUE, "Document count exceeds '" + DataAPIClientOptions.MAX_COUNT + ", the maximum allowed by the server");
    }

    /**
     * Default constructor.
     *
     * @param upperLimit
     *      what it the most the count can return
     */
    public TooManyDocumentsToCountException(int upperLimit) {
        super(ErrorCodesClient.INVALID_VALUE, "Document count exceeds upper bound set in method call " + upperLimit);
    }
}
