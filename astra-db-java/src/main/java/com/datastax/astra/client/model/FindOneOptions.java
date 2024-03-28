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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
public class FindOneOptions {

    /**
     * Order by.
     */
    private final Document sort;

    /**
     * Select.
     */
    private final Map<String, Integer> projection;

    /**
     * Options.
     */
    private final Boolean includeSimilarity;

    /**
     * Create a builder for those options.
     *
     * @return
     *      instance of the builder.
     */
    public static FindOneOptionsBuilder builder() {
        return new FindOneOptionsBuilder();
    }

    /**
     * Default constructor.
     *
     * @param builder
     *    builder to help creating the immutable object.
     */
    public FindOneOptions(FindOneOptionsBuilder builder) {
        this.sort = builder.sort;
        this.projection = builder.projection;
        this.includeSimilarity = builder.includeSimilarity;
    }

    /**
     * Find is an operation with multiple options to filter, sort, project, skip, limit, and more.
     * This builder will help to chain options.
     */
    public static class FindOneOptionsBuilder {

        /**
         * Order by.
         */
        private Document sort;

        /**
         * Projection for return document (select)
         */
        private Map<String, Integer> projection;

        /**
         * Flag to include similarity in the result when operating a semantic search.
         */
        private Boolean includeSimilarity;

        /**
         * Default Builder.
         */
        public FindOneOptionsBuilder() {}

        /**
         * Fluent api.
         *
         * @return
         *      add a filter
         */
        public FindOneOptionsBuilder withIncludeSimilarity() {
            this.includeSimilarity = true;
            return this;
        }

        /**
         * Builder for the projection.
         *
         * @param fields
         *     list of fields to include
         * @return
         *     self reference
         */
        public FindOneOptionsBuilder projections(String... fields) {
            if (fields != null) {
                for (String field : fields) {
                    projection(field);
                }
            }
            return this;
        }

        /**
         * Builder for the projection.
         *
         * @param field
         *      field to be present
         * @return
         *     self reference
         */
        public FindOneOptionsBuilder projection(String field) {
            Assert.hasLength(field, "field");
            if (this.projection == null) {
                this.projection = new LinkedHashMap<>();
            }
            this.projection.put(field, 1);
            return this;
        }

        /**
         * Provide a way to enter projections elements
         *
         * @param projections
         *      projections as a list.
         * @return
         *     self reference
         */
        public FindOneOptionsBuilder projections(List<Projection> projections) {
            Assert.notNull(projections, "projection");
            if (this.projection == null) {
                this.projection = new LinkedHashMap<>();
            }
            for (Projection p : projections) {
                this.projection.put(p.getField(), p.isPresent() ? 1 : 0);
            }
            return this;
        }

        /**
         * Fluent api.
         *
         * @param pProjection
         *      add a project field
         * @return
         *      current command.
         */
        public FindOneOptionsBuilder projections(Map<String, Integer> pProjection) {
            Assert.notNull(pProjection, "projection");
            if (this.projection == null) {
                this.projection = new LinkedHashMap<>();
            }
            this.projection.putAll(pProjection);
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
        public FindOneOptionsBuilder sort(Sort pSort) {
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
        public FindOneOptionsBuilder sort(List<Sort> sorts) {
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
        public FindOneOptionsBuilder sort(Document pSort) {
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
        public FindOneOptions build() {
            return new FindOneOptions(this);
        }

    }

}
