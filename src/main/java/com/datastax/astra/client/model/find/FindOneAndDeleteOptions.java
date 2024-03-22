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
import com.datastax.astra.internal.utils.Assert;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Options to find one and delete.
 */
@Data
public class FindOneAndDeleteOptions {

    /**
     * Order by.
     */
    private Document sort;

    /**
     * Select.
     */
    private Map<String, Integer> projection;

    /**
     * Default constructor.
     */
    public FindOneAndDeleteOptions() {}

    /**
     * Fluent api.
     *
     * @param pProjection
     *      add a project field
     * @return
     *      current command.
     */
    public FindOneAndDeleteOptions projection(Map<String, Integer> pProjection) {
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
    public FindOneAndDeleteOptions sort(Document pSort) {
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
    public FindOneAndDeleteOptions sortByVector(float[] vector) {
        return sort(new Document().append(Document.VECTOR, vector));
    }


}
