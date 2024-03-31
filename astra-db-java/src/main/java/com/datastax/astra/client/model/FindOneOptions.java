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

import com.datastax.astra.internal.utils.OptionsUtils;
import lombok.Data;

import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Data
public class FindOneOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Integer> projection;

    /**
     * Options.
     */
    private Boolean includeSimilarity;

    /**
     * Default constructor.
     */
    public FindOneOptions() {}

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public FindOneOptions sort(Sort... sort) {
        setSort(OptionsUtils.sort(sort));
        return this;
    }

    /**
     * Add a criteria with $vectorize in the sort clause
     *
     * @param vectorize an expression to look for vectorization
     * @param sorts The sort criteria to be applied to the findOne operation.
     * @return current command
     */
    public FindOneOptions vectorize(String vectorize, Sort ... sorts) {
        setSort(Sorts.vectorize(vectorize));
        if (sorts != null) {
            getSort().putAll(OptionsUtils.sort(sorts));
        }
        return this;
    }

    /**
     * Add a criteria with $vector in the sort clause
     *
     * @param vector vector float
     * @param sorts The sort criteria to be applied to the findOne operation.
     * @return current command
     */
    public FindOneOptions vector(float[] vector, Sort... sorts) {
        setSort(Sorts.vector(vector));
        if (sorts != null) {
            getSort().putAll(OptionsUtils.sort(sorts));
        }
        return this;
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param projection
     *      add a filter
     * @return
     *      current command.
     */
    public FindOneOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }

    /**
      * Fluent api.
      *
      * @return
      *      add a filter
      */
    public FindOneOptions includeSimilarity() {
        this.includeSimilarity = true;
        return this;
    }

    /**
     * Builder for creating {@link FindOneAndUpdateOptions} instances with a fluent API.
     */
    public static class Builder {

        /**
         * Hide constructor.
         */
        private Builder() {
        }

        /**
         * Initializes the building process with sorting options.
         *
         * @param sort The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneOptions} instance configured with the provided sort criteria.
         */
        public static FindOneOptions sort(Sort... sort) {
            return new FindOneOptions().sort(sort);
        }

        /**
         * Initializes the building process with projection options.
         *
         * @param projection The projection criteria to be applied to the findOne operation.
         * @return A new {@link FindOneOptions} instance configured with the provided projection criteria.
         */
        public static FindOneOptions projection(Projection... projection) {
            return new FindOneOptions().projection(projection);
        }

        /**
         * Initializes the building process with includeSimilarity options.
         *
         * @return A new {@link FindOneOptions} instance configured with the provided includeSimilarity criteria.
         */
        public static FindOneOptions includeSimilarity() {
            return new FindOneOptions().includeSimilarity();
        }

        /**
         * Initializes the building process with vectorize options.
         *
         * @param vectorize The vectorize criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneOptions} instance configured with the provided vectorize criteria.
         */
        public static FindOneOptions vectorize(String vectorize, Sort... sorts) {
            return new FindOneOptions().vectorize(vectorize, sorts);
        }

        /**
         * Initializes the building process with vector options.
         *
         * @param vector The vector criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneOptions} instance configured with the provided vector criteria.
         */
        public static FindOneOptions vector(float[] vector, Sort... sorts) {
            return new FindOneOptions().vector(vector, sorts);
        }
    }
}
