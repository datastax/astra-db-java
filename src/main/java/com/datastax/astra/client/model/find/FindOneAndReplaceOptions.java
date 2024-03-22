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
 * Options used in the `findAndReplace` command.
 */
@Getter
public class FindOneAndReplaceOptions {

    /**
     * Option to order the result.
     */
    private Document sort;

    /**
     * Options to project (select) the result.
     */
    private Map<String, Integer> projection;

    /**
     * Flag to enforce the replacement
     */
    Boolean upsert;

    /**
     * Tell to return the document before or after the update.
     */
    ReturnDocument returnDocument = ReturnDocument.after;

    /**
     * Default constructor.
     */
    public FindOneAndReplaceOptions() {}

    /**
     * Options of the Return Document flag
     */
    public  enum ReturnDocument {
        /**
         * Return the document before the update.
         */
        before,

        /**
         * Return the document after the update.
         */
        after;
    }

    /**
     * Builder Pattern, update the projection
     *
     * @param pProjection
     *      add a project field
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions projection(Map<String, Integer> pProjection) {
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
     * @param returnDocument
     *      decide to return document before or after the update
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions returnDocument(ReturnDocument returnDocument) {
        Assert.notNull(returnDocument, "returnDocument");
        this.returnDocument = returnDocument;
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

    /**
     * Builder Pattern, update the sort clause
     *
     * @param pSort
     *      sort clause of the command
     * @return
     *      self reference
     */
    public FindOneAndReplaceOptions sort(Document pSort) {
        Assert.notNull(pSort, "projection");
        if (this.sort == null) {
            sort = new Document();
        }
        this.sort.putAll(pSort);
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
    public FindOneAndReplaceOptions sortByVector(float[] vector) {
        return sort(new Document().append(Document.VECTOR, vector));
    }

}
