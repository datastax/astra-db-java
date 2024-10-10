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

import com.datastax.astra.client.model.command.CommandOptions;
import com.datastax.astra.client.model.query.Projection;
import com.datastax.astra.client.model.query.Sort;
import com.datastax.astra.client.model.query.Sorts;
import com.datastax.astra.internal.utils.OptionsUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
@Setter
public class FindOptions extends CommandOptions<FindOptions> {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Projection for return document (select)
     */
    private Map<String, Object> projection;

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
     * Flag to include sortVector in the result when operating a semantic search.
     */
    private Boolean includeSortVector;

    /**
     * Page state.
     */
    @Setter
    private String pageState;

    /**
     * Default constructor.
     */
    public FindOptions() {
        // left blank as populated by Jackson
    }

    // ----------------
    // ---- Sort ------
    // ----------------

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param sort
     *      add a filter
     * @return
     *      current command.
     */
    public FindOptions sort(Sort... sort) {
       return sort(OptionsUtils.sort(sort));
    }

    /**
     * Syntax sugar as delete option is only a sort
     * Could be like Map.of("$vectorize", "command, "field1", 1, "field2", -1);
     *
     * @param rawSort
     *      raw sort clause
     * @return
     *      current command.
     */
    public FindOptions sort(Map<String, Object> rawSort) {
        Document doc = new Document();
        doc.putAll(rawSort);
        return sort(doc);
    }

    /**
     * Syntax sugar as delete option is only a sort
     * Could be like Map.of("$vectorize", "command, "field1", 1, "field2", -1);
     *
     * @param sorClause
     *      sort clause as a document
     * @return
     *      current command.
     */
    public FindOptions sort(Document sorClause) {
        setSort(sorClause);
        return this;
    }

    /**
     * Add a criteria with $vectorize in the sort clause.
     *
     * @param vectorize an expression to look for vectorization
     * @param sorts The sort criteria to be applied to the findOne operation.
     * @return current command
     */
    public FindOptions sort(String vectorize, Sort ... sorts) {
        Document doc = Sorts.vectorize(vectorize);
        if (sorts != null) {
            doc.putAll(OptionsUtils.sort(sorts));
        }
        return sort(doc);
    }

    /**
     * Add a criteria with $vector in the sort clause
     *
     * @param vector vector float
     * @param sorts The sort criteria to be applied to the findOne operation.
     * @return current command
     */
    public FindOptions sort(float[] vector, Sort... sorts) {
        Document doc = Sorts.vector(vector);
        if (sorts != null) {
            doc.putAll(OptionsUtils.sort(sorts));
        }
        return sort(doc);
    }

    // ----------------------
    // ---- Projection ------
    // ----------------------

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param projection
     *      add a filter
     * @return
     *      current command.
     */
    public FindOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }

    /**
     * Fluent api.
     *
     * @return add a filter
     */
    public FindOptions includeSimilarity() {
        this.includeSimilarity = true;
        return this;
    }

    /**
     * Fluent api.
     *
     * @return add a filter
     */
    public FindOptions includeSortVector() {
        this.includeSortVector = true;
        return this;
    }

    /**
     * Update page state
     *
     * @param pageState new value for page state
     * @return self reference
     */
    public FindOptions pageState(String pageState) {
        this.pageState = pageState;
        return this;
    }

    /**
     * Add a skip clause in the find block
     *
     * @param skip value for skip options
     * @return current command
     */
    public FindOptions skip(int skip) {
        if (skip < 0) {
            throw new IllegalArgumentException("Skip must be positive");
        }
        this.skip = skip;
        return this;
    }

    /**
     * Add a limit clause in the find block
     *
     * @param limit value for limit options
     * @return current command
     */
    public FindOptions limit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limit = limit;
        return this;
    }

}
