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

import java.util.List;

/**
 * Options for the updateOne operation

 */
@Data
public class UpdateOneOptions {

    /**
     * if upsert is selected
     */
    private Boolean upsert;

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Create a builder for those options.
     *
     * @return
     *      instance of the builder.
     */
    public static UpdateOneOptionsBuilder builder() {
        return new UpdateOneOptionsBuilder();
    }

    /**
     * Default constructor.
     *
     * @param builder
     *    builder to help creating the immutable object.
     */
    public UpdateOneOptions(UpdateOneOptionsBuilder builder) {
        this.sort   = builder.sort;
        this.upsert = builder.upsert;
    }

    /**
     * Find is an operation with multiple options to filter, sort, project, skip, limit, and more.
     * This builder will help to chain options.
     */
    public static class UpdateOneOptionsBuilder {

        /**
         * if upsert is selected
         */
        private Boolean upsert;

        /**
         * Order by.
         */
        private Document sort;

        /**
         * Builder Pattern, update the upsert flag
         *
         * @param upsert
         *     upsert flag
         * @return
         *      self reference
         */
        public UpdateOneOptionsBuilder upsert(Boolean upsert) {
            Assert.notNull(upsert, "upsert");
            this.upsert = upsert;
            return this;
        }

        /**
         * Fluent api.
         *
         * @param pSort
         *      list of sorts
         * @return
         *      Self reference
         */
        public UpdateOneOptionsBuilder sort(Sort pSort) {
            Assert.notNull(pSort, "sort");
            if (this.sort == null) {
                this.sort = new Document();
            }
            this.sort.put(pSort.getField(), pSort.getOrder().getCode());
            return this;
        }

        /**
         * Fluent api.
         *
         * @param sorts
         *      list of sorts
         * @return
         *      Self reference
         */
        public UpdateOneOptionsBuilder sort(List<Sort> sorts) {
            Assert.notNull(sorts, "sort");
            if (this.sort == null) {
                sort = new Document();
            }
            for (Sort s : sorts) {
                this.sort.put(s.getField(), s.getOrder().getCode());
            }
            return this;
        }

        /**
         * Fluent api.
         *
         * @param pSort
         *      add a filter
         * @return
         *      current command.
         */
        public UpdateOneOptionsBuilder sort(Document pSort) {
            Assert.notNull(pSort, "sort");
            if (this.sort == null) {
                sort = new Document();
            }
            this.sort.putAll(pSort);
            return this;
        }

        /**
         * Builder for the Options.
         *
         * @return
         *      the find options object
         */
        public UpdateOneOptions build() {
            return new UpdateOneOptions(this);
        }

    }



}
