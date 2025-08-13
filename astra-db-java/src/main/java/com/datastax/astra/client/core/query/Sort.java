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

import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.hybrid.Hybrid;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.datastax.astra.internal.utils.EscapeUtils;
import lombok.Builder;
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
    private final String passage;

    /** sort for the field (if vectorize). */
    private final DataAPIVector vector;

    /** sort for the field (if hybrid). */
    private final Hybrid hybrid;

    /**
     * Default Constructor.
     *
     * @param field
     *      field name
     * @param order
     *      field ordering instruction
     * @param passage
     *     vectorize instruction
     * @param vector
     *     vector instruction
     */
    @Builder(builderMethodName = "internalBuilder")
    private Sort(String field, SortOrder order, String passage, DataAPIVector vector, Hybrid hybrid) {
        this.field     = field;
        this.order     = order;
        this.passage   = passage;
        this.vector    = vector;
        this.hybrid    = hybrid;
        if (order == null && passage == null && vector == null && hybrid == null) {
            throw new IllegalArgumentException("Sort must have an order, vectorize, vector or hybrid");
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
            return order.getCode();
        }
        if (passage != null) {
            return passage;
        }
        if (hybrid != null) {
            return hybrid;
        }
        return vector;
    }

    /**
     * Build a sort clause ascending.
     *
     * @param fieldSegments
     *      field Segments
     * @return
     *      sort instance.
     */
    public static Sort ascending(String[] fieldSegments) {
        return ascending(EscapeUtils.escapeFieldNames(fieldSegments));
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
        return internalBuilder()
                .field(field)
                .order(SortOrder.ASCENDING)
                .build();
    }

    /**
     * Build a sort clause DESCENDING.
     *
     * @param fieldSegments
     *      field Segments
     * @return
     *      sort instance.
     */
    public static Sort descending(String[] fieldSegments) {
        return descending(EscapeUtils.escapeFieldNames(fieldSegments));
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
        return internalBuilder()
                .field(field)
                .order(SortOrder.DESCENDING)
                .build();
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
        return vector(DataAPIKeywords.VECTOR.getKeyword(), embeddings);
    }

    /**
     * Build a sort clause with a vector on a table
     *
     * @param fieldName
     *      current field
     * @param embeddings
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Sort vector(String fieldName, float[] embeddings) {
        return internalBuilder()
                .field(fieldName)
                .vector(new DataAPIVector(embeddings))
                .build();
    }

    /**
     * Build a sort clause with a vector on a table
     *
     * @param fieldName
     *      current field
     * @param embeddings
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Sort vector(String fieldName, DataAPIVector embeddings) {
        return internalBuilder()
                .field(fieldName)
                .vector(embeddings)
                .build();
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
        return vectorize(DataAPIKeywords.VECTORIZE.getKeyword(), vectorize);
    }

    /**
     * Build a sort clause with vectorize.
     *
     * @param fieldName
     *      current field
     * @param vectorize
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Sort vectorize(String fieldName, String vectorize) {
        return internalBuilder()
                .field(fieldName)
                .passage(vectorize)
                .build();
    }

    /**
     * Build a sort clause with vectorize.
     *
     * @param hybrid
     *      hybrid sort
     * @return
     *       sort instance.
     */
    public static Sort hybrid(Hybrid hybrid) {
        return internalBuilder()
                .field(DataAPIKeywords.HYBRID.getKeyword())
                .hybrid(hybrid)
                .build();
    }

    /**
     * Build a sort clause with vectorize.
     *
     * @param hybrid
     *      hybrid sort
     * @return
     *       sort instance.
     */
    public static Sort hybrid(String hybrid) {
        return internalBuilder()
                .field(DataAPIKeywords.HYBRID.getKeyword())
                .hybrid(new Hybrid(hybrid))
                .build();
    }

    /**
     * Build a sort clause with vectorize.
     *
     * @param vectorize
     *      vectorize information
     * @param lexical
     *      lexical information
     *
     * @return
     *       sort instance.
     */
    public static Sort hybrid(String vectorize, String lexical) {
        return internalBuilder()
                .field(DataAPIKeywords.HYBRID.getKeyword())
                .hybrid(new Hybrid().vectorize(vectorize).lexical(lexical))
                .build();
    }


    /**
     * Build a sort clause with lexical.
     *
     * @param content
     *      Content to saerh
     * @return
     *       sort instance.
     */
    public static Sort lexical(String content) {
        return lexical(DataAPIKeywords.LEXICAL.getKeyword(), content);
    }

    /**
     * Build a sort clause with lexical.
     *
     * @param fieldName
     *      name of the column to sort against
     * @param content
     *      Content to saerh
     * @return
     *       sort instance.
     */
    public static Sort lexical(String fieldName, String content) {
        return internalBuilder()
                .field(fieldName)
                .passage(content)
                .build();
    }

}
