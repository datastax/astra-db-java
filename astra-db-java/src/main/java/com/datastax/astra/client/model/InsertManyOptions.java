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
import lombok.Getter;

/**
 * Options for InsertMany
 */
@Getter
public class InsertManyOptions {

    /**
     * If the flag is set to true the command is failing on first error
     */
    private final boolean ordered ;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private final int concurrency;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private final int chunkSize;

    /**
     * If the flag is set to true the command is failing on first error
     */
    private final int timeout;

    /**
     * Constructor leveraging the builder.
     *
     * @param builder
     *      builder class
     */
    private InsertManyOptions(Builder builder) {
        this.ordered     = builder.ordered;
        this.concurrency = builder.concurrency;
        this.chunkSize   = builder.chunkSize;
        this.timeout     = builder.timeout;
    }

    /**
     * Create a builder for the options.
     *
     * @return
     *      instance of builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Inner class as a Builder.
     */
    public static class Builder {
        /** builder. */
        private boolean ordered = false;
        /** builder. */
        private int concurrency = 1;
        /** builder. */
        private int chunkSize = DataAPIOptions.getMaxDocumentsInInsert();
        /** builder. */
        private int timeout = DataAPIOptions.DEFAULT_REQUEST_TIMEOUT_SECONDS * 1000;

        /**
         * Default constructor.
         */
        public Builder() {}

        /**
         * Builder pattern, populate field.
         *
         * @param ordered
         *      ordered value
         * @return
         *      self reference
         */
        public Builder ordered(boolean ordered) {
            this.ordered = ordered;
            return this;
        }

        /**
         * Builder pattern, populate field.
         *
         * @param concurrency
         *      concurrency value
         * @return
         *      self reference
         */
        public Builder withConcurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        /**
         * Builder pattern, populate field.
         *
         * @param chunkSize
         *      chunkSize value
         * @return
         *      self reference
         */
        public Builder withChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        /**
         * Builder pattern, populate field.
         *
         * @param timeout
         *      timeout value
         * @return
         *      self reference
         */
        public Builder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Create the instance of the InsertManyOptions.
         *
         * @return
         *      immutable instance of InsertManyOptions
         */
        public InsertManyOptions build() {
            return new InsertManyOptions(this);
        }
    }

}
