package com.datastax.astra.client.model.find;

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

import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.internal.utils.Assert;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
public class FindOptions {

    /**
     * Default constructor.
     */
    public FindOptions() {
    }

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
     * Fluent api.
     *
     * @return
     *      add a filter
     */
    public FindOptions includeSimilarity() {
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
    public FindOptions withPageState(String pageState) {
        this.pageState = pageState;
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
    public FindOptions projection(Map<String, Integer> pProjection) {
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
     *      add a filter
     * @return
     *      current command.
     */
    public FindOptions sortingBy(Document pSort) {
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
    public FindOptions sortingBy(String fieldName, SortOrder ordering) {
        return sortingBy(new Document().append(fieldName, ordering.getOrder()));
    }

    /**
     * Help to chain filters.
     *
     * @return
     *      reference to current object
     */
    public FindOptions and() {
        return this;
    }

    /**
     * Add vector in the sort block.
     *
     * @param vector
     *      vector float
     * @return
     *      current command
     */
    public FindOptions sortingByVector(float[] vector) {
        return sortingBy(new Document().append(Document.VECTOR, vector));
    }

    /**
     * Add a skip clause in the find block
     *
     * @param skip
     *      value for skip options
     * @return
     *      current command
     */
    public FindOptions skip(int skip) {
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
    public FindOptions limit(int limit) {
        this.limit = limit;
        return this;
    }

}
