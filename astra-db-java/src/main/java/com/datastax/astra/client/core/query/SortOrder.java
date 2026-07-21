package com.datastax.astra.client.core.query;

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
 * Enumeration representing sort order for query results.
 * <p>
 * This enum defines the two possible sort orders: ascending (1) and descending (-1),
 * which correspond to the Data API's sort order values.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * // Sort by age in ascending order
 * Sort.ascending("age");
 * 
 * // Sort by timestamp in descending order
 * Sort.descending("timestamp");
 * }
 * </pre>
 */
@Getter
public enum SortOrder {

    /** 
     * Ascending sort order (1).
     * <p>Sorts values from lowest to highest (A-Z, 0-9, oldest to newest).</p>
     */
    ASCENDING(1),

    /** 
     * Descending sort order (-1).
     * <p>Sorts values from highest to lowest (Z-A, 9-0, newest to oldest).</p>
     */
    DESCENDING(-1);

    /** 
     * The numeric code representing the sort order.
     * <p>1 for ascending, -1 for descending.</p>
     */
    private final Integer code;

    /**
     * Constructs a SortOrder with the specified numeric code.
     *
     * @param code the numeric value representing the sort order (1 or -1)
     */
    SortOrder(Integer code) {
        this.code = code;
    }

}
