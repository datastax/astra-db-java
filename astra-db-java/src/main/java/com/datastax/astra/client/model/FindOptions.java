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
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
public class FindOptions {

    /**
     * Order by.
     */
    private final Document sort;

    /**
     * Projection for return document (select)
     */
    private final Map<String, Integer> projection;

    /**
     * Skip a few result in the beginning
     */
    private final Integer skip;

    /**
     * Stop processing after a few results
     */
    private final Integer limit;

    /**
     * Flag to include similarity in the result when operating a semantic search.
     */
    private final Boolean includeSimilarity;

    /**
     * Page state.
     */
    @Setter
    private String pageState;

    /**
     * Syntax sugar to create a FindOptions with a vectorize sort.
     *
     * @param vectorize
     *      vectorize expression
     * @return
     *      find options
     */
    public static FindOptions vectorize(String vectorize) {
        return FindOptions.builder().vectorize(vectorize).build();
    }

    /**
     * Default constructor.
     *
     * @param builder
     *      builder to initialize the options
     */
    public FindOptions(FindOptionsBuilder builder) {
        this.includeSimilarity = builder.includeSimilarity;
        this.projection = builder.projection;
        this.pageState  = builder.pageState;
        this.sort       = builder.sort;
        this.skip       = builder.skip;
        this.limit      = builder.limit;
    }

    /**
     * Create a builder to help creating the options object.
     *
     * @return
     *      an instance of builder
     */
    public static FindOptionsBuilder builder() {
        return new FindOptionsBuilder();
    }

    /**
     * Find is an operation with multiple options to filter, sort, project, skip, limit, and more.
     * This builder will help to chain options.
     */
    public static class FindOptionsBuilder {

        /**
         * Order by.
         */
        private Document sort;

        /**
         * Projection for return document (select)
         */
        private Map<String, Integer> projection;

        /**
         * Skip a few result in the beginning
         */
        private Integer skip;

        /**
         * Stop processing after a few results
         */
        private Integer limit;

        /**
         * Flag to include similarity in the result when operating a semantic search.
         */
        private Boolean includeSimilarity;

        /**
         * Page state.
         */
        private String pageState;

        /**
         * Default Builder.
         */
        public FindOptionsBuilder() {}

        /**
         * Fluent api.
         *
         * @return
         *      add a filter
         */
        public FindOptionsBuilder includeSimilarity() {
            this.includeSimilarity = true;
            return this;
        }

        /**
         * Update page state
         *
         * @param pageState
         *      new value for page state
         * @return
         *      self reference
         */
        public FindOptionsBuilder pageState(String pageState) {
            this.pageState = pageState;
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
        public FindOptionsBuilder projections(String... fields) {
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
        public FindOptionsBuilder projection(String field) {
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
        public FindOptionsBuilder projections(List<Projection> projections) {
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
        public FindOptionsBuilder projections(Map<String, Integer> pProjection) {
            Assert.notNull(pProjection, "projection");
            if (this.projection == null) {
                this.projection = new LinkedHashMap<>();
            }
            this.projection.putAll(pProjection);
            return this;
        }

        /**
         * Add a skip clause in the find block
         *
         * @param skip
         *      value for skip options
         * @return
         *      current command
         */
        public FindOptionsBuilder skip(int skip) {
            if (skip < 0) {
                throw new IllegalArgumentException("Skip must be positive");
            }
            this.skip = skip;
            return this;
        }

        /**
         * Add a limit clause in the find block
         *
         * @param limit
         *      value for limit options
         * @return
         *      current command
         */
        public FindOptionsBuilder limit(int limit) {
            if (limit<0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            this.limit = limit;
            return this;
        }

        /**
         * Add a criteria with $vectorize in the sort clause
         *
         * @param vectorize
         *      an expression to look for vectorization
         * @return
         *      current command
         */
        public FindOptionsBuilder vectorize(String vectorize) {
            return sort(Sorts.vectorize(vectorize));
        }

        /**
         * Add a criteria with $vector in the sort clause
         *
         * @param vector
         *      vector float
         * @return
         *      current command
         */
        public FindOptionsBuilder vector(float[] vector) {
            return sort(Sorts.vector(vector));
        }

        /**
         * Fluent api.
         *
         * @param pSort
         *      list of sorts
         * @return
         *      Self reference
         */
        public FindOptionsBuilder sort(Sort pSort) {
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
        public FindOptionsBuilder sort(List<Sort> sorts) {
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
        public FindOptionsBuilder sort(Document pSort) {
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
        public FindOptions build() {
            return new FindOptions(this);
        }


    }






}
