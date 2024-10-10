package com.datastax.astra.client.model.query;

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
import lombok.Setter;

/**
 * Class to help building a projection.
 */
@Getter @Setter
public class Sort {

    /** name of the Projection. */
    private final String field;

    /** sort for the field. */
    private final SortOrder order;

    /**
     * Default Constructor.
     *
     * @param field
     *      field name
     * @param order
     *      field ordering instruction
     */
    public Sort(String field, SortOrder order) {
        this.field = field;
        this.order = order;
    }
}
