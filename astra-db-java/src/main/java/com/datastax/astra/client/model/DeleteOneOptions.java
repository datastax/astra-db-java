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
import lombok.Getter;

import java.util.List;

/**
 * Options to delete One document.
 */
@Getter
public class DeleteOneOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Default constructor.
     */
    public DeleteOneOptions(DeleteOneOptionsBuilder builder) {
        this.sort = builder.sort;
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public static DeleteOneOptions sort(Sort sort) {
        return new DeleteOneOptionsBuilder().sort(sort).build();
    }

    /**
     * Create a builder for those options.
     *
     * @return
     *      instance of the builder.
     */
    public static DeleteOneOptionsBuilder builder() {
        return new DeleteOneOptionsBuilder();
    }

    /**
     * Find is an operation with multiple options to filter, sort, project, skip, limit, and more.
     * This builder will help to chain options.
     */
    public static class DeleteOneOptionsBuilder {

        /**
         * Order by.
         */
        private Document sort;

        /**
         * Default Builder.
         */
        public DeleteOneOptionsBuilder() {}

        /**
         * Fluent api.
         *
         * @param pSort
         *      list of sorts
         * @return
         *      Self reference
         */
        public DeleteOneOptionsBuilder sort(Sort pSort) {
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
        public DeleteOneOptionsBuilder sort(List<Sort> sorts) {
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
        public DeleteOneOptionsBuilder sort(Document pSort) {
            Assert.notNull(pSort, "sort");
            if (this.sort == null) {
                sort = new Document();
            }
            this.sort.putAll(pSort);
            return this;
        }

        /**
         * Builder for the find Options.
         *
         * @return
         *      the find options object
         */
        public DeleteOneOptions build() {
            return new DeleteOneOptions(this);
        }

    }

    /**
     * Fluent api.
     *
     * @param pSort
     *      add a filter
     * @return
     *      current command.
     */
    public DeleteOneOptions sortingBy(Document pSort) {
        Assert.notNull(pSort, "sort");
        if (this.sort == null) {
            sort = new Document();
        }
        this.sort.putAll(pSort);
        return this;
    }

    /**
     * Add a sort clause to the current field.
     *
     * @param fieldName
     *      field name
     * @param ordering
     *      field ordering
     * @return
     *      current reference  find
     */
    public DeleteOneOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getCode()));
    }
}
