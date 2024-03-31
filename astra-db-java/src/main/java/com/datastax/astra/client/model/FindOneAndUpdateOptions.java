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
 * List options for a findOneAndUpdate command.
 */
@Getter
public class FindOneAndUpdateOptions {

    /**
     * Order by.
     */
    private final Document sort;

    /**
     * Select.
     */
    private final Map<String, Integer> projection;

    /**
     * Upsert flag.
     */
    private final Boolean upsert;

    /**
     * Return document flag.
     */
    private final FindOneAndReplaceOptions.ReturnDocument returnDocument;

    /**
     * Default constructor.
     *
     * @param builder
     *      tht builder to help creating the immutable object.
     */
    private FindOneAndUpdateOptions(FindOneAndUpdateOptionsBuider builder) {
        this.projection     = builder.projection;
        this.sort           = builder.sort;
        this.upsert         = builder.upsert;
        this.returnDocument = builder.returnDocument;
    }

    /**
     * Create a builder to help creating the options object.
     *
     * @return
     *      an instance of builder
     */
    public static FindOneAndUpdateOptionsBuider builder() {
        return new FindOneAndUpdateOptionsBuider();
    }

    /**
     * Find is an operation with multiple options to filter, sort, project, skip, limit, and more.
     * This builder will help to chain options.
     */
    public static class FindOneAndUpdateOptionsBuider {
        /**
         * Order by.
         */
        private Document sort;

        /**
         * Select.
         */
        private Map<String, Integer> projection;

        /**
         * Upsert flag.
         */
        private Boolean upsert;

        /**
         * Return document flag.
         */
        private FindOneAndReplaceOptions.ReturnDocument returnDocument = FindOneAndReplaceOptions.ReturnDocument.after;

        /**
         * Builder for the projection.
         *
         * @param fields
         *     list of fields to include
         * @return
         *     self reference
         */
        public FindOneAndUpdateOptionsBuider projections(String... fields) {
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
        public FindOneAndUpdateOptionsBuider  projection(String field) {
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
        public FindOneAndUpdateOptionsBuider projections(List<Projection> projections) {
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
        public FindOneAndUpdateOptionsBuider projections(Map<String, Integer> pProjection) {
            Assert.notNull(pProjection, "projection");
            if (this.projection == null) {
                this.projection = new LinkedHashMap<>();
            }
            this.projection.putAll(pProjection);
            return this;
        }

        /**
         * Builder Pattern, update the returnDocument flag
         *
         * @return
         *      self reference
         */
        public FindOneAndUpdateOptionsBuider returnDocumentAfter() {
            Assert.notNull(returnDocument, "returnDocument");
            this.returnDocument = FindOneAndReplaceOptions.ReturnDocument.after;
            return this;
        }

        /**
         * Builder Pattern, update the returnDocument flag
         *
         * @return
         *      self reference
         */
        public FindOneAndUpdateOptionsBuider returnDocumentBefore() {
            Assert.notNull(returnDocument, "returnDocument");
            this.returnDocument = FindOneAndReplaceOptions.ReturnDocument.before;
            return this;
        }

        /**
         * Builder Pattern, update the upsert flag.
         *
         * @param upsert
         *      upsert flag
         * @return
         *      self reference
         */
        public FindOneAndUpdateOptionsBuider upsert(Boolean upsert) {
            Assert.notNull(upsert, "upsert");
            this.upsert = upsert;
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
        public FindOneAndUpdateOptionsBuider vectorize(String vectorize) {
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
        public FindOneAndUpdateOptionsBuider vector(float[] vector) {
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
        public FindOneAndUpdateOptionsBuider  sort(Sort pSort) {
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
        public FindOneAndUpdateOptionsBuider sort(List<Sort> sorts) {
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
        public FindOneAndUpdateOptionsBuider sort(Document pSort) {
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
        public FindOneAndUpdateOptions build() {
            return new FindOneAndUpdateOptions(this);
        }

    }

}
