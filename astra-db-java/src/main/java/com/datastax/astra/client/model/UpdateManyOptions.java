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
import lombok.Data;

/**
 * Options for the updateOne operation

 */
@Data
public class UpdateManyOptions {

    /**
     * if upsert is selected
     */
    private Boolean upsert;

    /**
     * Create a builder for those options.
     *
     * @return
     *      instance of the builder.
     */
    public static UpdateManyOptionsBuilder builder() {
        return new UpdateManyOptionsBuilder();
    }

    /**
     * Create an instance of the options with upsert flag.
     * @param upsert
     *      upsert value
     * @return
     *      instance of the options
     */
    public static UpdateManyOptions upsert(Boolean upsert) {
        return new UpdateManyOptionsBuilder().upsert(upsert).build();
    }

    /**
     * Default constructor.
     *
     * @param builder
     *    builder to help creating the immutable object.
     */
    public UpdateManyOptions(UpdateManyOptionsBuilder builder) {
        this.upsert = builder.upsert;
    }

    /**
     * Find is an operation with multiple options to filter, sort, project, skip, limit, and more.
     * This builder will help to chain options.
     */
    public static class UpdateManyOptionsBuilder {

        /**
         * if upsert is selected
         */
        private Boolean upsert;

        /**
         * Builder Pattern, update the upsert flag
         *
         * @param upsert
         *     upsert flag
         * @return
         *      self reference
         */
        public UpdateManyOptionsBuilder upsert(Boolean upsert) {
            Assert.notNull(upsert, "upsert");
            this.upsert = upsert;
            return this;
        }

        /**
         * Builder for the Options.
         *
         * @return
         *      the find options object
         */
        public UpdateManyOptions build() {
            return new UpdateManyOptions(this);
        }

    }



}
