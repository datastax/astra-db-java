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

import com.datastax.astra.client.DataAPIOptions;
import com.datastax.astra.client.collections.commands.FindOneAndUpdateOptions;
import com.datastax.astra.client.core.commands.CommandOptions;
import com.datastax.astra.client.core.http.HttpClientOptions;
import lombok.Getter;

/**
 * Options for InsertMany
 */
@Getter
public class TableInsertManyOptions extends CommandOptions<TableInsertManyOptions> {

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
    private int chunkSize = DataAPIOptions.DEFAULT_MAX_CHUNK_SIZE;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private long timeout = HttpClientOptions.DEFAULT_REQUEST_TIMEOUT_MILLISECONDS;

    /**
     * Populate insertMany options
     */
    public TableInsertManyOptions() {
        // left blank, jackson serialization
    }

    /**
     * Setter for ordered.
     *
     * @param ordered
     *      ordered value
     * @return
     *      insert many options
     */
    public TableInsertManyOptions ordered(boolean ordered) {
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
    public TableInsertManyOptions concurrency(int concurrency) {
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
    public TableInsertManyOptions chunkSize(int chunkSize) {
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
    public TableInsertManyOptions timeout(int timeout) {
        this.timeout = timeout;
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
            // builder pattern
        }

        /**
         * Initializes the building process with ordered options.
         *
         * @param ordered The ordered criteria to be applied to the insertMany operation.
         * @return A new {@link TableInsertManyOptions} instance configured with the provided ordered criteria.
         */
        public static TableInsertManyOptions ordered(boolean ordered) {
            return new TableInsertManyOptions().ordered(ordered);
        }

        /**
         * Initializes the building process with concurrency options.
         *
         * @param concurrency The concurrency criteria to be applied to the insertMany operation.
         * @return A new {@link TableInsertManyOptions} instance configured with the provided concurrency criteria.
         */
        public static TableInsertManyOptions concurrency(int concurrency) {
            return new TableInsertManyOptions().concurrency(concurrency);
        }

        /**
         * Initializes the building process with chunkSize options.
         *
         * @param chunkSize The chunkSize criteria to be applied to the insertMany operation.
         * @return A new {@link TableInsertManyOptions} instance configured with the provided chunkSize criteria.
         */
        public static TableInsertManyOptions chunkSize(int chunkSize) {
            return new TableInsertManyOptions().chunkSize(chunkSize);
        }

        /**
         * Initializes the building process with timeout options.
         *
         * @param timeout The timeout criteria to be applied to the insertMany operation.
         * @return A new {@link TableInsertManyOptions} instance configured with the provided timeout criteria.
         */
        public static TableInsertManyOptions timeout(int timeout) {
            return new TableInsertManyOptions().timeout(timeout);
        }

    }

}
