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
 * Options used in the `findAndReplace` command.
 */
@Getter
@Setter
public class FindOneAndReplaceOptions {

    /**
     * Option to order the result.
     */
    private Document sort;

    /**
     * Options to project (select) the result.
     */
    private Map<String, Object> projection;

    /**
     * Flag to enforce the replacement
     */
    private Boolean upsert;

    /**
     * Tell to return the document before or after the update.
     */
    private String returnDocument = ReturnDocument.AFTER.getKey();

    /**
     * Default constructor.
     */
    public FindOneAndReplaceOptions() {
        // left blank as attributes are populated in static way
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public FindOneAndReplaceOptions sort(Sort... sort) {
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
    public FindOneAndReplaceOptions sort(String vectorize, Sort ... sorts) {
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
    public FindOneAndReplaceOptions sort(float[] vector, Sort... sorts) {
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
    public FindOneAndReplaceOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }

    /**
     * Builder Pattern, update the returnDocument flag
     *
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions returnDocumentAfter() {
        this.returnDocument = ReturnDocument.AFTER.getKey();
        return this;
    }

    /**
     * Builder Pattern, update the returnDocument flag
     *
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions returnDocumentBefore() {
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
    public FindOneAndReplaceOptions upsert(Boolean upsert) {
        Assert.notNull(upsert, "upsert");
        this.upsert = upsert;
        return this;
    }

    /**
     * Builder for creating {@link FindOneAndReplaceOptions} instances with a fluent API.
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
         * @param sort The sort criteria to be applied to the findAndReplace operation.
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided sort criteria.
         */
        public static FindOneAndReplaceOptions sort(Sort... sort) {
            return new FindOneAndReplaceOptions().sort(sort);
        }

        /**
         * Initializes the building process with projection options.
         *
         * @param projection The projection criteria to be applied to the findAndReplace operation.
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided projection criteria.
         */
        public static FindOneAndReplaceOptions projection(Projection... projection) {
            return new FindOneAndReplaceOptions().projection(projection);
        }

        /**
         * Initializes the building process with returnDocument options.
         *
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided returnDocument criteria.
         */
        public static FindOneAndReplaceOptions returnDocumentAfter() {
            return new FindOneAndReplaceOptions().returnDocumentAfter();
        }

        /**
         * Initializes the building process with returnDocument options.
         *
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided returnDocument criteria.
         */
        public static FindOneAndReplaceOptions returnDocumentBefore() {
            return new FindOneAndReplaceOptions().returnDocumentBefore();
        }

        /**
         * Initializes the building process with upsert options.
         *
         * @param upsert The upsert criteria to be applied to the findAndReplace operation.
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided upsert criteria.
         */
        public static FindOneAndReplaceOptions upsert(Boolean upsert) {
            return new FindOneAndReplaceOptions().upsert(upsert);
        }

        /**
         * Initializes the building process with vectorize options.
         * <p><i style='color: orange;'><b>Note</b> : This feature is under current development.</i></p>
         *
         * @param vectorize The vectorize criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided vectorize criteria.
         */
        public static FindOneAndReplaceOptions sort(String vectorize, Sort... sorts) {
            return new FindOneAndReplaceOptions().sort(vectorize, sorts);
        }

        /**
         * Initializes the building process with vector options.
         *
         * @param vector The vector criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link FindOneAndReplaceOptions} instance configured with the provided vector criteria.
         */
        public static FindOneAndReplaceOptions sort(float[] vector, Sort... sorts) {
            return new FindOneAndReplaceOptions().sort(vector, sorts);
        }
    }


}
