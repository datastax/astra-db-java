package com.datastax.astra.client.collections.exceptions;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2026 DataStax
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
import  com.datastax.astra.client.exceptions.DataAPIException;

import java.util.List;

public class CollectionInsertManyException extends DataAPIException {

    /**
     * List of successfully inserted document IDs before the error occurred.
     */
    private final List<Object> insertedIds;

    /**
     * Default constructor.
     */
    public CollectionInsertManyException() {
        super(ERROR_CODE_PARTIAL_INSERTION, "Some documents were not inserted, check insertedIds property.");
        this.insertedIds = List.of();
    }

    /**
     * Constructor with inserted IDs.
     *
     * @param insertedIds List of successfully inserted document IDs
     */
    public CollectionInsertManyException(List<Object> insertedIds) {
        super(ERROR_CODE_PARTIAL_INSERTION, 
              String.format("Partial insertion: %d documents were inserted before error occurred.", 
                           insertedIds != null ? insertedIds.size() : 0));
        this.insertedIds = insertedIds != null ? List.copyOf(insertedIds) : List.of();
    }

    /**
     * Constructor with inserted IDs and custom message.
     *
     * @param insertedIds List of successfully inserted document IDs
     * @param message Custom error message
     */
    public CollectionInsertManyException(List<Object> insertedIds, String message) {
        super(ERROR_CODE_PARTIAL_INSERTION, message);
        this.insertedIds = insertedIds != null ? List.copyOf(insertedIds) : List.of();
    }

    /**
     * Get the list of successfully inserted document IDs.
     *
     * @return Unmodifiable list of inserted IDs
     */
    public List<Object> getInsertedIds() {
        return insertedIds;
    }

}
