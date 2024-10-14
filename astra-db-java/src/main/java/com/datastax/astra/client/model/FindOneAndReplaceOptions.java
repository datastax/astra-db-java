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

import com.datastax.astra.client.model.collections.Document;
import com.datastax.astra.client.model.command.CommandOptions;
import com.datastax.astra.client.model.query.Projection;
import com.datastax.astra.client.model.query.Sort;
import com.datastax.astra.client.model.query.Sorts;
import com.datastax.astra.internal.utils.Assert;
import com.datastax.astra.internal.utils.OptionsUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Options used in the `findAndReplace` command.
 */
@Getter
@Setter
public class FindOneAndReplaceOptions extends CommandOptions<FindOneAndReplaceOptions> {

    /**
     * Option to order the result.
     */
    private Document sort;

    /**
     * Options to project (select) the result.
     */
    private Map<String, Object> projection;

    /**
     * Flag to enforce the replacement
     */
    private Boolean upsert;

    /**
     * Tell to return the document before or after the update.
     */
    private String returnDocument = ReturnDocument.AFTER.getKey();

    /**
     * Default constructor.
     */
    public FindOneAndReplaceOptions() {
        // left blank as attributes are populated in static way
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
    public FindOneAndReplaceOptions sort(Sort... sort) {
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
    public FindOneAndReplaceOptions sort(Map<String, Object> rawSort) {
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
    public FindOneAndReplaceOptions sort(Document sorClause) {
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
    public FindOneAndReplaceOptions sort(String vectorize, Sort ... sorts) {
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
    public FindOneAndReplaceOptions sort(float[] vector, Sort... sorts) {
        Document doc = Sorts.vector(vector);
        if (sorts != null) {
            doc.putAll(OptionsUtils.sort(sorts));
        }
        return sort(doc);
    }

    /**
     * Syntax sugar as delete option is only a sort
     *
     * @param projection
     *      add a filter
     * @return
     *      current command.
     */
    public FindOneAndReplaceOptions projection(Projection... projection) {
        setProjection(OptionsUtils.projection(projection));
        return this;
    }

    /**
     * Builder Pattern, update the returnDocument flag
     *
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions returnDocumentAfter() {
        this.returnDocument = ReturnDocument.AFTER.getKey();
        return this;
    }

    /**
     * Builder Pattern, update the returnDocument flag
     *
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions returnDocumentBefore() {
        this.returnDocument = ReturnDocument.BEFORE.getKey();
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
    public FindOneAndReplaceOptions upsert(Boolean upsert) {
        Assert.notNull(upsert, "upsert");
        this.upsert = upsert;
        return this;
    }

}
