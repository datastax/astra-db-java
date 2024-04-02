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

import lombok.Getter;

/**
 *
 * Options used in the `bulkWrite` command.
 */
@Getter
public final class BulkWriteOptions {

    /**
     * Flag to enforcer the ordering of the operations. If set to false the operations will be executed in parallel and put in an Execution Queue.
     */
    private boolean ordered = true;

    /**
     * When executed in parallel (ordered = false) the number of operations that can be executed at the same time.
     */
    private Integer concurrency = 5;

    /**
     * Default constructor.
     */
    public BulkWriteOptions() {
        // left blank attributes have default values
    }

    /**
     * Setter for ordered.
     *
     * @param ordered
     *      ordered value
     * @return
     *      insert many options
     */
    public BulkWriteOptions ordered(boolean ordered) {
        this.ordered = ordered;
        return this;
    }


    /**
     * Setter for concurrency.
     *
     * @param concurrency
     *      concurrency value
     * @return
     *      insert many options
     */
    public BulkWriteOptions concurrency(int concurrency) {
        this.concurrency = concurrency;
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
            // builder pattern
        }

        /**
         * Initializes the building process with ordered options.
         *
         * @param ordered The ordered criteria to be applied to the insertMany operation.
         * @return A new {@link BulkWriteOptions} instance configured with the provided ordered criteria.
         */
        public static BulkWriteOptions ordered(boolean ordered) {
            return new BulkWriteOptions().ordered(ordered);
        }

        /**
         * Initializes the building process with concurrency options.
         *
         * @param concurrency The concurrency criteria to be applied to the insertMany operation.
         * @return A new {@link BulkWriteOptions} instance configured with the provided concurrency criteria.
         */
        public static BulkWriteOptions concurrency(int concurrency) {
            return new BulkWriteOptions().concurrency(concurrency);
        }


    }

}
