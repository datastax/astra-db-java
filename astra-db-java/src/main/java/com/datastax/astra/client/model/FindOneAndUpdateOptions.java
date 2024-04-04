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

import com.datastax.astra.internal.utils.Assert;
import com.datastax.astra.internal.utils.OptionsUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


/**
 * List options for a findOneAndUpdate command.
 */
@Getter
@Setter
public class FindOneAndUpdateOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Integer> projection;

    /**
     * Upsert flag.
     */
    private Boolean upsert;

    /**
     * Return document flag.
     */
    private String returnDocument;

    /**
     * Default constructor.
     */
    public FindOneAndUpdateOptions() {
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
    public FindOneAndUpdateOptions sort(Sort... sort) {
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
    public FindOneAndUpdateOptions sort(String vectorize, Sort ... sorts) {
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
    public FindOneAndUpdateOptions sort(float[] vector, Sort... sorts) {
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
    public FindOneAndUpdateOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }

    /**
     * Builder Pattern, update the returnDocument flag
     *
     * @return
     *      self reference
     */
    public FindOneAndUpdateOptions returnDocumentAfter() {
        this.returnDocument = ReturnDocument.AFTER.getKey();
        return this;
    }

    /**
     * Builder Pattern, update the returnDocument flag
     *
     * @return
     *      self reference
     */
    public FindOneAndUpdateOptions returnDocumentBefore() {
        this.returnDocument = ReturnDocument.BEFORE.getKey();
        return this;
    }

    /**
     * Builder Pattern, update the upsert flag.
     *
     * @param upsert
     *      upsert flag
     * @return
     *      self reference
     */
    public FindOneAndUpdateOptions upsert(Boolean upsert) {
        Assert.notNull(upsert, "upsert");
        this.upsert = upsert;
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
         * @param sort The sort criteria to be applied to the findOneAndUpdate operation.
         * @return A new {@link FindOneAndUpdateOptions} instance configured with the provided sort criteria.
         */
        public static FindOneAndUpdateOptions sort(Sort... sort) {
            return new FindOneAndUpdateOptions().sort(sort);
        }

        /**
         * Initializes the building process with projection options.
         *
         * @param projection The projection criteria to be applied to the findOneAndUpdate operation.
         * @return A new {@link FindOneAndUpdateOptions} instance configured with the provided projection criteria.
         */
        public static FindOneAndUpdateOptions projection(Projection... projection) {
            return new FindOneAndUpdateOptions().projection(projection);
        }

        /**
         * Initializes the building process with returnDocument options.
         *
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided returnDocument criteria.
         */
        public static FindOneAndUpdateOptions returnDocumentAfter() {
            return new FindOneAndUpdateOptions().returnDocumentAfter();
        }

        /**
         * Initializes the building process with returnDocument options.
         *
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided returnDocument criteria.
         */
        public static FindOneAndUpdateOptions returnDocumentBefore() {
            return new FindOneAndUpdateOptions().returnDocumentBefore();
        }

        /**
         * Initializes the building process with upsert options.
         *
         * @param upsert The upsert criteria to be applied to the findOneAndUpdate operation.
         * @return A new {@link FindOneAndUpdateOptions} instance configured with the provided upsert criteria.
         */
        public static FindOneAndUpdateOptions upsert(Boolean upsert) {
            return new FindOneAndUpdateOptions().upsert(upsert);
        }

        /**
         * Initializes the building process with vectorize options.
         *
         * @param vectorize The vectorize criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneAndUpdateOptions} instance configured with the provided vectorize criteria.
         */
        public static FindOneAndUpdateOptions sort(String vectorize, Sort... sorts) {
            return new FindOneAndUpdateOptions().sort(vectorize, sorts);
        }

        /**
         * Initializes the building process with vector options.
         *
         * @param vector The vector criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneAndUpdateOptions} instance configured with the provided vector criteria.
         */
        public static FindOneAndUpdateOptions sort(float[] vector, Sort... sorts) {
            return new FindOneAndUpdateOptions().sort(vector, sorts);
        }
    }

}
