package com.datastax.astra.client.core.hybrid;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 - 2025 DataStax
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

import com.datastax.astra.client.core.DataAPIKeywords;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the limits for lexical and vector data.
 */
@Data
public class HybridLimits {

    /**
     * The limit for the hybrid data when vectorize and lexical are used.
     */
    Integer limit;

    /**
     * The limit for the lexical data.
     */
    Map<String, Integer> mapOfLimits;

    /**
     * Constructor.
     *
     * @param limit
     *      the limit for the hybrid data.
     */
    public HybridLimits(Integer limit) {
        this.limit = limit;
    }

    /**
     * Constructor.
     *
     * @param mapOfLimits
     *      the map of limits for the hybrid data.
     */
    public HybridLimits(Map<String, Integer> mapOfLimits) {
        this.mapOfLimits = mapOfLimits;
    }

    /**
     * Add a limit for the lexical data.
     *
     * @param limit
     *      lexical limit.
     * @return
     *      self reference
     */
    public HybridLimits lexical(Integer limit) {
        if (mapOfLimits == null) {
            mapOfLimits = new HashMap<>();
        }
        mapOfLimits.put(DataAPIKeywords.LEXICAL.getKeyword(), limit);
        return this;
    }

    /**
     * Add a limit for the vector data.
     *
     * @param limit
     *      vector limit.
     * @return
     *      self reference
     */
    public HybridLimits vector(Integer limit) {
        if (mapOfLimits == null) {
            mapOfLimits = new HashMap<>();
        }
        mapOfLimits.put(DataAPIKeywords.VECTOR.getKeyword(), limit);
        return this;
    }

}
