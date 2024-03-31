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

import com.datastax.astra.client.DataAPIOptions;
import lombok.Data;
import lombok.Getter;

/**
 * Options for InsertMany
 */
@Getter
public class InsertManyOptions {

    /**
     * If the flag is set to true the command is failing on first error
     */
    private boolean ordered = false;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int concurrency = 1;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int chunkSize = DataAPIOptions.getMaxDocumentsInInsert();

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int timeout = DataAPIOptions.DEFAULT_REQUEST_TIMEOUT_SECONDS * 1000;

    /**
     * Populate inserMany options
     */
    public InsertManyOptions() {
    }

    /**
     * Setter for ordered.
     *
     * @param ordered
     *      ordered value
     * @return
     *      insert many options
     */
    public InsertManyOptions ordered(boolean ordered) {
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
    public InsertManyOptions concurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    /**
     * Setter for chunkSize.
     *
     * @param chunkSize
     *      chunkSize value
     * @return
     *      insert many options
     */
    public InsertManyOptions chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Setter for timeout.
     *
     * @param timeout
     *      timeout value
     * @return
     *      insert many options
     */
    public InsertManyOptions timeout(int timeout) {
        this.timeout = timeout;
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
         * Initializes the building process with ordered options.
         *
         * @param ordered The ordered criteria to be applied to the insertMany operation.
         * @return A new {@link InsertManyOptions} instance configured with the provided ordered criteria.
         */
        public static InsertManyOptions ordered(boolean ordered) {
            return new InsertManyOptions().ordered(ordered);
        }

        /**
         * Initializes the building process with concurrency options.
         *
         * @param concurrency The concurrency criteria to be applied to the insertMany operation.
         * @return A new {@link InsertManyOptions} instance configured with the provided concurrency criteria.
         */
        public static InsertManyOptions concurrency(int concurrency) {
            return new InsertManyOptions().concurrency(concurrency);
        }

        /**
         * Initializes the building process with chunkSize options.
         *
         * @param chunkSize The chunkSize criteria to be applied to the insertMany operation.
         * @return A new {@link InsertManyOptions} instance configured with the provided chunkSize criteria.
         */
        public static InsertManyOptions chunkSize(int chunkSize) {
            return new InsertManyOptions().chunkSize(chunkSize);
        }

        /**
         * Initializes the building process with timeout options.
         *
         * @param timeout The timeout criteria to be applied to the insertMany operation.
         * @return A new {@link InsertManyOptions} instance configured with the provided timeout criteria.
         */
        public static InsertManyOptions timeout(int timeout) {
            return new InsertManyOptions().timeout(timeout);
        }

    }

}
