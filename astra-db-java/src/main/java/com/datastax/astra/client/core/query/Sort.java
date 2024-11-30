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

import com.datastax.astra.client.core.types.DataAPIKeywords;
import com.datastax.astra.client.core.vector.DataAPIVector;
import lombok.Getter;
import lombok.Setter;

/**
 * Class to help building a projection.
 */
@Getter @Setter
public class Sort {

    /** name of the Projection. */
    private final String field;

    /** sort for the field (if not vector). */
    private final SortOrder order;

    /** sort for the field (if vectorize). */
    private final String vectorize;

    /** sort for the field (if vectorize). */
    private final DataAPIVector vector;

    /**
     * Default Constructor.
     *
     * @param field
     *      field name
     * @param order
     *      field ordering instruction
     */
    public Sort(String field, SortOrder order, String vectorize, DataAPIVector vector) {
        this.field = field;
        this.order = order;
        this.vectorize = vectorize;
        this.vector = vector;
        if (order == null && vectorize == null && vector == null) {
            throw new IllegalArgumentException("Sort must have an order, vectorize or vector");
        }
    }

    /**
     * Get the value of the sort.
     *
     * @return
     *      sor value
     */
    public Object getValue() {
        if (order != null) {
            return order;
        }
        if (vectorize != null) {
            return vectorize;
        }
        return vector;
    }

    /**
     * Build a sort clause ascending.
     *
     * @param field
     *      current field
     * @return
     *      sort instance.
     */
    public static Sort ascending(String field) {
        return new Sort(field, SortOrder.ASCENDING, null, null);
    }

    /**
     * Build a sort clause DESCENDING.
     *
     * @param field
     *      current field
     * @return
     *      sort instance.
     */
    public static Sort descending(String field) {
        return new Sort(field, SortOrder.DESCENDING, null, null);
    }

    /**
     * Build a sort clause with a vector.
     *
     * @param embeddings
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Sort vector(float[] embeddings) {
        return new Sort(DataAPIKeywords.VECTOR.getKeyword(), null, null, new DataAPIVector(embeddings));
    }

    /**
     * Build a sort clause with vectorize.
     *
     * @param vectorize
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Sort vectorize(String vectorize) {
        return new Sort(DataAPIKeywords.VECTORIZE.getKeyword(), null, vectorize, null);
    }

}
