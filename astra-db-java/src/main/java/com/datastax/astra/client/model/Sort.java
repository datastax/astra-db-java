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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class to help building a projection.
 */
@Data
@AllArgsConstructor
public class Sort {

    /** name of the Projection. */
    String field;

    /** sort for the field. */
    SortOrder order;

    /** Default constructor. */
    public Sort() {}

    /**
     * Builder for a projection.
     *
     * @param field
     *      name of the field to sort on
     * @param sort
     *      sort clause
     * @return
     *      self reference
     */
    public static Sort of(String field, SortOrder sort) {
        Sort p = new Sort();
        p.field = field;
        p.order = sort;
        return p;
    }

}
