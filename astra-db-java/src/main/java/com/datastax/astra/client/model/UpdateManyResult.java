package com.datastax.astra.client.model;

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
 * Represents the result returned by command 'updateMany()'
 */
@Data
public class UpdateManyResult {

    /**
     * Number of matched documents
     */
    private Integer matchedCount;

    /**
     * Number of modified documents
     */
    private Integer modifiedCount;

    /**
     * Populated if upserted
     */
    private Object upsertedId;

    /**
     * Not used any more
     */
    private Boolean moreData;

    /**
     * Next page state.
     */
    private String nextPageState;

    /**
     * Default constructor.
     */
    public UpdateManyResult() {}
}
