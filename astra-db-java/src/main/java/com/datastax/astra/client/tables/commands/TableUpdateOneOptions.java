package com.datastax.astra.client.tables.commands;

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
 * Options for the updateOne operation

 */
@Getter
@Setter
public class TableUpdateOneOptions extends CommandOptions<TableUpdateOneOptions> {

    /**
     * if upsert is selected
     */
    private Boolean upsert;

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Default constructor.
     */
    public TableUpdateOneOptions() {
        // left blank as fields are populated by builder
    }

    /**
     * Upsert flag.
     *
     * @param upsert upsert flag
     * @return current command.
     */
    public TableUpdateOneOptions upsert(Boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public TableUpdateOneOptions sort(Sort... sort) {
        setSort(OptionsUtils.sort(sort));
        return this;
    }

    /**
     * Add a criteria with $vectorize in the sort clause
     *
     * @param vectorize an expression to look for vectorization
     * @param sorts The sort criteria to be applied to the delete operation.
     * @return current command
     */
    public TableUpdateOneOptions vectorize(String vectorize, Sort ... sorts) {
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
     * @param sorts The sort criteria to be applied to the delete operation.
     * @return current command
     */
    public TableUpdateOneOptions vector(float[] vector, Sort... sorts) {
        setSort(Sorts.vector(vector));
        if (sorts != null) {
            getSort().putAll(OptionsUtils.sort(sorts));
        }
        return this;
    }


    /**
     * Builder for creating {@link TableUpdateOneOptions} instances with a fluent API.
     */
    @Deprecated
    public static class Builder {

        /**
         * Hide constructor.
         */
        private Builder() {
        }

        /**
         * Create a new instance of {@link TableUpdateOneOptions}.
         *
         * @param upsert upsert flag
         * @return new instance of {@link TableUpdateOneOptions}.
         */
        public static TableUpdateOneOptions upsert(boolean upsert) {
            return new TableUpdateOneOptions().upsert(upsert);
        }

        /**
         * Initializes the building process with sorting options.
         *
         * @param sort The sort criteria to be applied to the delete operation.
         * @return A new {@link TableUpdateOneOptions} instance configured with the provided sort criteria.
         */
        public static TableUpdateOneOptions sort(Sort... sort) {
            return new TableUpdateOneOptions().sort(sort);
        }

        /**
         * Initializes the building process with sorting options.
         *
         * @param vectorize The sort criteria to be applied to the delete operation.
         * @param sort The sort criteria to be applied to the delete operation.
         * @return A new {@link TableUpdateOneOptions} instance configured with the provided sort criteria.
         */
        public static TableUpdateOneOptions vectorize(String vectorize, Sort... sort) {
            return new TableUpdateOneOptions().vectorize(vectorize, sort);
        }

        /**
         * Initializes the building process with sorting options.
         *
         * @param vector The sort criteria to be applied to the delete operation.
         * @param sort The sort criteria to be applied to the delete operation.
         * @return A new {@link TableUpdateOneOptions} instance configured with the provided sort criteria.
         */
        public static TableUpdateOneOptions vector(float[] vector, Sort... sort) {
            return new TableUpdateOneOptions().vector(vector, sort);
        }
    }

}
