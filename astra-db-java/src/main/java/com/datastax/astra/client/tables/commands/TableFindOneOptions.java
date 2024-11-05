package com.datastax.astra.client.tables.commands;

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

import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.client.core.commands.CommandOptions;
import com.datastax.astra.client.core.query.Projection;
import com.datastax.astra.client.core.query.Sort;
import com.datastax.astra.client.core.query.Sorts;
import com.datastax.astra.internal.utils.OptionsUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * List Options for a FindOne command.
 */
@Getter
@Setter
public class TableFindOneOptions extends CommandOptions<TableFindOneOptions> {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Object> projection;

    /**
     * Options.
     */
    private Boolean includeSimilarity;

    /**
     * Flag to include sortVector in the result when operating a semantic search.
     */
    private Boolean includeSortVector;

    /**
     * Default constructor.
     */
    public TableFindOneOptions() {
        // left blank, jackson serialization
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
    public TableFindOneOptions sort(Sort... sort) {
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
    public TableFindOneOptions sort(Map<String, Object> rawSort) {
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
    public TableFindOneOptions sort(Document sorClause) {
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
    public TableFindOneOptions sort(String vectorize, Sort ... sorts) {
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
    public TableFindOneOptions sort(float[] vector, Sort... sorts) {
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
    public TableFindOneOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }

    /**
      * Fluent api.
      *
      * @return
      *      add a filter
      */
    public TableFindOneOptions includeSimilarity() {
        this.includeSimilarity = true;
        return this;
    }

    /**
     * Fluent api.
     *
     * @return add a filter
     */
    public TableFindOneOptions includeSortVector() {
        this.includeSortVector = true;
        return this;
    }
}
