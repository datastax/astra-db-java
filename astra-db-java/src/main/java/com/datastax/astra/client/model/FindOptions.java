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
    @Setter
    private String pageState;

    /**
     * Default constructor.
     */
    public FindOptions() {
    }

    /**
     * Default constructor.
     *
     * @param builder
     *      builder to initialize the options
     */
    public FindOptions(FindOptionsBuilder builder) {
        if (builder != null) {
            this.includeSimilarity = builder.includeSimilarity;
            this.projection = builder.projection;
            this.pageState  = builder.pageState;
            this.sort       = builder.sort;
            this.skip       = builder.skip;
            this.limit      = builder.limit;
        }
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
     * Builder options with a vectorize.
     *
     * @param vectorize
     *     vectorize expression
     * @return
     *      self references
     */
    public static FindOptions vectorize(String vectorize) {
        Assert.hasLength(vectorize, "vectorize");
        return builder().withVectorize(vectorize).build();
    }

    /**
     * Builder options with 'vectorize'.
     *
     * @param vector
     *     build a find query on vector
     * @return
     *      self references
     */
    public static FindOptions vector(float[] vector) {
        Assert.notNull(vector, "vector");
        return builder().withVector(vector).build();
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
        public FindOptionsBuilder withIncludeSimilarity() {
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
        public FindOptionsBuilder withPageState(String pageState) {
            this.pageState = pageState;
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
        public FindOptionsBuilder withProjection(List<Projection> projections) {
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
        public FindOptionsBuilder withProjection(Map<String, Integer> pProjection) {
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
        public FindOptionsBuilder withVectorize(String vectorize) {
            Assert.hasLength(vectorize, "vectorize");
            return sortBy(new Document().append(Document.VECTORIZE, vectorize));
        }

        /**
         * Add a criteria with $vector in the sort clause
         *
         * @param vector
         *      vector float
         * @return
         *      current command
         */
        public FindOptionsBuilder withVector(float[] vector) {
            Assert.notNull(vector, "vector");
            return sortBy(new Document().append(Document.VECTOR, vector));
        }

        /**
         * Fluent api.
         *
         * @param field
         *      field name
         * @param order
         *      orders
         * @return
         *      Self reference
         */
        public FindOptionsBuilder sortBy(String field, SortOrder order) {
            Assert.notNull(order, "order");
            Assert.hasLength(field, "field");
            if (this.sort == null) {
                sort = new Document();
            }
            this.sort.put(field, order.getOrder());
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
        public FindOptionsBuilder sortBy(List<Sort> sorts) {
            Assert.notNull(sorts, "sort");
            if (this.sort == null) {
                sort = new Document();
            }
            for (Sort s : sorts) {
                this.sort.put(s.getField(), s.getSort().getOrder());
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
        public FindOptionsBuilder sortBy(Document pSort) {
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