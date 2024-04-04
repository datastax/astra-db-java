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
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
@Setter
public class FindOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Projection for return document (select)
     */
    private Map<String, Integer> projection;

    /**
     * Skip a few result in the beginning
     */
    private Integer skip;

    /**
     * Stop processing after a few results
     */
    private Integer limit;

    /**
     * Flag to include similarity in the result when operating a semantic search.
     */
    private Boolean includeSimilarity;

    /**
     * Page state.
     */
    @Setter
    private String pageState;

    /**
     * Default constructor.
     */
    public FindOptions() {
        // left blank as populated by Jackson
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public FindOptions sort(Sort... sort) {
        setSort(OptionsUtils.sort(sort));
        return this;
    }

    /**
     * Add a criteria with $vectorize in the sort clause.
     * <p><i style='color: orange;'><b>Note</b> : This feature is under current development.</i></p>
     *
     * @param vectorize an expression to look for vectorization
     * @param sorts The sort criteria to be applied to the findOne operation.
     * @return current command
     */
    public FindOptions sort(String vectorize, Sort ... sorts) {
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
    public FindOptions sort(float[] vector, Sort... sorts) {
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
    public FindOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }


    /**
     * Fluent api.
     *
     * @return add a filter
     */
    public FindOptions includeSimilarity() {
        this.includeSimilarity = true;
        return this;
    }

    /**
     * Update page state
     *
     * @param pageState new value for page state
     * @return self reference
     */
    public FindOptions pageState(String pageState) {
        this.pageState = pageState;
        return this;
    }

    /**
     * Add a skip clause in the find block
     *
     * @param skip value for skip options
     * @return current command
     */
    public FindOptions skip(int skip) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be positive");
        }
        this.skip = skip;
        return this;
    }

    /**
     * Add a limit clause in the find block
     *
     * @param limit value for limit options
     * @return current command
     */
    public FindOptions limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limit = limit;
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
        public static FindOptions sort(Sort... sort) {
            return new FindOptions().sort(sort);
        }

        /**
         * Initializes the building process with sorting options.
         *
         * @param vector string to be vectorized in the findOne operation.
         * @param sort The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOptions} instance configured with the provided sort criteria.
         */
        public static FindOptions sort(float[] vector, Sort... sort) {
            return new FindOptions().sort(vector, sort);
        }

        /**
         * Initializes the building process with sorting options.
         * <p><i style='color: orange;'><b>Note</b> : This feature is under current development.</i></p>
         *
         * @param vectorize string to be vectorized in the findOne operation.
         * @param sort The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOptions} instance configured with the provided sort criteria.
         */
        public static FindOptions sort(String vectorize, Sort... sort) {
            return new FindOptions().sort(vectorize, sort);
        }

        /**
         * Initializes the building process with projection options.
         *
         * @param projection The projection criteria to be applied to the findOne operation.
         * @return A new {@link FindOptions} instance configured with the provided projection criteria.
         */
        public static FindOptions projection(Projection... projection) {
            return new FindOptions().projection(projection);
        }

        /**
         * Initializes the building process with includeSimilarity options.
         *
         * @return A new {@link FindOneOptions} instance configured with the provided includeSimilarity criteria.
         */
        public static FindOptions includeSimilarity() {
            return new FindOptions().includeSimilarity();
        }

        /**
         * Initializes the building process with skip options.
         *
         * @param skip The skip criteria to be applied to the findOne operation
         * @return A new {@link FindOneOptions} instance configured with the provided skip criteria.
         */
        public static FindOptions skip(int skip) {
            return new FindOptions().skip(skip);
        }

        /**
         * Initializes the building process with limit options.
         *
         * @param limit The limit criteria to be applied to the findOne operation
         * @return A new {@link FindOneOptions} instance configured with the provided limit criteria.
         */
        public static FindOptions limit(int limit) {
            return new FindOptions().limit(limit);
        }

    }
}
