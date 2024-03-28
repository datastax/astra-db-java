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

/**
 * Utility class to hold the sort options for a query.
 */
public class Sorts {

    /**
     * Hide constructor
     */
    private Sorts() {
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
        return new Sort(field, SortOrder.ASCENDING);
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
        return new Sort(field, SortOrder.DESCENDING);
    }

    /**
     * Build a sort clause with a vector.
     *
     * @param embeddings
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Document vector(float[] embeddings) {
        return new Document().append(Document.VECTOR, embeddings);
    }

    /**
     * Build a sort clause with vectorize.
     *
     * @param vectorize
     *      vector of embeddings
     * @return
     *       sort instance.
     */
    public static Document vectorize(String vectorize) {
        return new Document().append(Document.VECTORIZE, vectorize);
    }

}
