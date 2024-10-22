package com.datastax.astra.client.collections.commands;

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

import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.commands.CommandOptions;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.Sorts;
import com.datastax.astra.internal.utils.OptionsUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Options to delete One document.
 */
@Getter
@Setter
public class DeleteOneOptions extends CommandOptions<DeleteOneOptions> {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Default constructor.
     */
    public DeleteOneOptions() {
        // Left blank as sort is populated in static way
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public DeleteOneOptions sort(Sort... sort) {
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
    public DeleteOneOptions sort(String vectorize, Sort ... sorts) {
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
    public DeleteOneOptions sort(float[] vector, Sort... sorts) {
        setSort(Sorts.vector(vector));
        if (sorts != null) {
            getSort().putAll(OptionsUtils.sort(sorts));
        }
        return this;
    }

    /**
     * Builder for creating {@link DeleteOneOptions} instances with a fluent API.
     */
    @Deprecated
    public static class Builder {

        /**
         * Hide constructor.
         */
        private Builder() {}

        /**
         * Initializes the building process with sorting options.
         *
         * @param sort The sort criteria to be applied to the delete operation.
         * @return A new {@link DeleteOneOptions} instance configured with the provided sort criteria.
         */
        public static DeleteOneOptions sort(Sort... sort) {
            return new DeleteOneOptions().sort(sort);
        }

        /**
         * Initializes the building process with vectorize options.
         *
         * @param vectorize The vectorize criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link DeleteOneOptions} instance configured with the provided vectorize criteria.
         */
        public static DeleteOneOptions vectorize(String vectorize, Sort... sorts) {
            return new DeleteOneOptions().sort(vectorize, sorts);
        }

        /**
         * Initializes the building process with vector options.
         *
         * @param vector The vector criteria to be applied to the findOne operation
         * @param sorts The sort criteria to be applied to the findOne operation.
         * @return A new {@link DeleteOneOptions} instance configured with the provided vector criteria.
         */
        public static DeleteOneOptions vector(float[] vector, Sort... sorts) {
            return new DeleteOneOptions().sort(vector, sorts);
        }
    }

}
