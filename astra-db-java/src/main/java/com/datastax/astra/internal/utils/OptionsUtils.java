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

import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Projection;
import com.datastax.astra.client.model.Sort;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reuse of treatments for options
 */
public class OptionsUtils {

    /**
     * Hide default constructor.
     */
    private OptionsUtils() {}

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
    public static LinkedHashMap<String, Integer> projection(Projection... pProjections) {
        Assert.notNull(pProjections, "sort");
        return Arrays.stream(pProjections).collect(Collectors.toMap(
                  Projection::getField, // keyMapper
                  p -> p.isPresent() ? 1 : 0, // valueMapper
                  (oldValue, newValue) -> oldValue, // mergeFunction, in case of key collision
                  LinkedHashMap::new // Map supplier, to maintain insertion order
               ));
    }


}
