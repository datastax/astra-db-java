package com.datastax.astra.internal.utils;

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

import com.datastax.astra.client.model.types.DataAPIKeywords;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.query.Projection;
import com.datastax.astra.client.model.query.Sort;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reuse of treatments for options
 */
public class OptionsUtils {

    /**
     * Hide default constructor.
     */
    private OptionsUtils() {
        // left blank, hiding constructor for utility class
    }

    /**
     * Fluent api.
     *
     * @param pSort
     *      list of sorts
     * @return
     *      Self reference
     */
    public static Document sort(Sort... pSort) {
        Assert.notNull(pSort, "sort");
        Document finalSort = new Document();
        Arrays.stream(pSort).forEach(p -> finalSort.append(p.getField(), p.getOrder().getCode()));
        return finalSort;
    }

    /**
     * Fluent api.
     *
     * @param pProjections
     *      list of projections
     * @return
     *      Self reference
     */
    public static Map<String, Object> projection(Projection... pProjections) {
        Assert.notNull(pProjections, "sort");
        Map<String, Object> finalProjection = new LinkedHashMap<>();
        for (Projection p : pProjections) {
            if (p.getPresent() != null && p.getSliceStart() != null) {
                throw new IllegalArgumentException("A projection cannot be include/exclude and a slide at same time");
            }
            if (p.getPresent() == null && p.getSliceStart() == null) {
                throw new IllegalArgumentException("A projection must be include/exclude or a slide");
            }
            if (p.getPresent() != null) {
                finalProjection.put(p.getField(),  p.getPresent() ? 1 : 0);
            } else {
                // SLICE
                int start = p.getSliceStart();
                Map<String, Object> slice = new LinkedHashMap<>();
                if (p.getSliceEnd() != null) {
                    slice.put(DataAPIKeywords.SLICE.getKeyword(), new Integer[] {start, p.getSliceEnd()});
                } else {
                    slice.put(DataAPIKeywords.SLICE.getKeyword(), start);
                }
                finalProjection.put(p.getField(), slice);
            }
        }
        return finalProjection;
    }

}
