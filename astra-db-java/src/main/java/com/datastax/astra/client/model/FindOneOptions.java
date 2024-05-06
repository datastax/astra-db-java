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
public class FindOneOptions extends CommandOptions<FindOneOptions> {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Object> projection;

    /**
     * Options.
     */
    private Boolean includeSimilarity;

    /**
     * Default constructor.
     */
    public FindOneOptions() {
        // left blank, jackson serialization
    }

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
     * <p><i style='color: orange;'><b>Note</b> : This feature is under current development.</i></p>
     *
     * @param vectorize an expression to look for vectorization
     * @param sorts The sort criteria to be applied to the findOne operation.
     * @return current command
     */
    public FindOneOptions sort(String vectorize, Sort ... sorts) {
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
    public FindOneOptions sort(float[] vector, Sort... sorts) {
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
    @Deprecated
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
         * Initializes the building process with sorting options.
         *
         * @param vector string to be vectorized in the findOne operation.
         * @param sort The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneOptions} instance configured with the provided sort criteria.
         */
        public static FindOneOptions sort(float[] vector, Sort... sort) {
            return new FindOneOptions().sort(vector, sort);
        }

        /**
         * Initializes the building process with sorting options.
         * <p><i style='color: orange;'><b>Note</b> : This feature is under current development.</i></p>
         *
         * @param vectorize string to be vectorized in the findOne operation.
         * @param sort The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneOptions} instance configured with the provided sort criteria.
         */
        public static FindOneOptions sort(String vectorize, Sort... sort) {
            return new FindOneOptions().sort(vectorize, sort);
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
         * Setter for Api Key
         *
         * @param apiKey
         *      embedding service Api keu
         * @return
         *      insert many options
         */
        public static InsertManyOptions embeddingServiceApiKey(String apiKey) {
            return new InsertManyOptions().embeddingAPIKey(apiKey);
        }

    }
}
