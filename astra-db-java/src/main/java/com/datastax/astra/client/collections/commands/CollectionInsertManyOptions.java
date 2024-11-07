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

import com.datastax.astra.client.core.options.DataAPIOptions;
import com.datastax.astra.client.core.commands.CommandOptions;
import com.datastax.astra.client.core.http.HttpClientOptions;
import lombok.Getter;

/**
 * Options for InsertMany
 */
@Getter
public class CollectionInsertManyOptions extends CommandOptions<CollectionInsertManyOptions> {

    /**
     * If the flag is set to true the command is failing on first error
     */
    private boolean ordered = false;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int concurrency = 1;

    /**
     * When `true`, response will contain an additional field: 'documentResponses'"
     * with is an array of Document Response Objects. Each Document Response Object"
     * contains the `_id` of the document and the `status` of the operation (one of"
     * `OK`, `ERROR` or `SKIPPED`). Additional `errorsIdx` field is present when the"
     * " status is `ERROR` and contains the index of the error in the main `errors` array.",
     * defaultValue = "false").
     */
    private boolean returnDocumentResponses = false;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private int chunkSize = DataAPIOptions.DEFAULT_MAX_CHUNK_SIZE;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private long timeout;

    /**
     * Populate insertMany options
     */
    public CollectionInsertManyOptions() {
        // left blank, jackson serialization
        this.httpClientOptions = new HttpClientOptions();
        this.timeout = httpClientOptions.getRequestTimeout().getSeconds();
    }

    /**
     * Setter for ordered.
     *
     * @param ordered
     *      ordered value
     * @return
     *      insert many options
     */
    public CollectionInsertManyOptions ordered(boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    /**
     * Setter for ordered.
     *
     * @param returnDocumentResponses
     *      enabled return document
     * @return
     *      insert many options
     */
    public CollectionInsertManyOptions returnDocumentResponses(boolean returnDocumentResponses) {
        this.returnDocumentResponses = returnDocumentResponses;
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
    public CollectionInsertManyOptions concurrency(int concurrency) {
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
    public CollectionInsertManyOptions chunkSize(int chunkSize) {
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
    public CollectionInsertManyOptions timeout(int timeout) {
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
         * @return A new {@link CollectionInsertManyOptions} instance configured with the provided ordered criteria.
         */
        public static CollectionInsertManyOptions ordered(boolean ordered) {
            return new CollectionInsertManyOptions().ordered(ordered);
        }

        /**
         * Initializes the building process with concurrency options.
         *
         * @param concurrency The concurrency criteria to be applied to the insertMany operation.
         * @return A new {@link CollectionInsertManyOptions} instance configured with the provided concurrency criteria.
         */
        public static CollectionInsertManyOptions concurrency(int concurrency) {
            return new CollectionInsertManyOptions().concurrency(concurrency);
        }

        /**
         * Initializes the building process with chunkSize options.
         *
         * @param chunkSize The chunkSize criteria to be applied to the insertMany operation.
         * @return A new {@link CollectionInsertManyOptions} instance configured with the provided chunkSize criteria.
         */
        public static CollectionInsertManyOptions chunkSize(int chunkSize) {
            return new CollectionInsertManyOptions().chunkSize(chunkSize);
        }

        /**
         * Initializes the building process with timeout options.
         *
         * @param timeout The timeout criteria to be applied to the insertMany operation.
         * @return A new {@link CollectionInsertManyOptions} instance configured with the provided timeout criteria.
         */
        public static CollectionInsertManyOptions timeout(int timeout) {
            return new CollectionInsertManyOptions().timeout(timeout);
        }

    }

}
