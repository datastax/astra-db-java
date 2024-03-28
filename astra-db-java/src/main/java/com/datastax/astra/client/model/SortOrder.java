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

import lombok.Getter;

/**
 * Encode the sort order in results.
 */
@Getter
public enum SortOrder {

    /** Value for ascending order. */
    ASCENDING(1),

    /** Value for descending order. */
    DESCENDING(-1);

    /** Order value. */
    private final Integer code;

    /**
     * Constructor for the enum.
     *
     * @param code
     *      value for the order
     */
    SortOrder(Integer code) {
        this.code = code;
    }

}
