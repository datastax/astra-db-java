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
import lombok.Setter;

/**
 * Encode the presence of a field in the result.
 */
@Getter @Setter
public class Projection {

    /** Name of the field. */
    private final String field;

    /** Is the field present in the result. */
    private final Boolean present;

    /** Mutually exclusive with 'present', provide a slice */
    private final Integer sliceStart;

    /** Mutually exclusive with 'present', provide a slice end */
    private final Integer sliceEnd;

    /**
     * Default constructor.
     *
     * @param field
     *      field value
     * @param present
     *      tell if field is present
     */
    public Projection(String field, boolean present) {
        this(field, present, null, null);
    }

    /**
     * Default constructor.
     *
     * @param field
     *      field value
     * @param present
     *      tell if field is present
     * @param sliceStart
     *     start of slice (mutually exclusive with 'present')
     * @param sliceEnd
     *     end of slice (mutually exclusive with 'present'), optional
     */
    public Projection(String field, Boolean present, Integer sliceStart, Integer sliceEnd) {
        this.field      = field;
        this.present    = present;
        this.sliceStart = sliceStart;
        this.sliceEnd   = sliceEnd;
    }
}
