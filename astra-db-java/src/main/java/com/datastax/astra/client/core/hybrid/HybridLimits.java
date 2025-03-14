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

@Data
public class HybridLimits {

    Integer limit;

    Map<String, Integer> mapOfLimits;

    public HybridLimits(Integer limit) {
        this.limit = limit;
    }

    public HybridLimits(Map<String, Integer> mapOfLimits) {
        this.mapOfLimits = mapOfLimits;
    }

    public HybridLimits vectorize(Integer limit) {
        if (mapOfLimits == null) {
            mapOfLimits = new HashMap<>();
        }
        mapOfLimits.put(DataAPIKeywords.VECTORIZE.getKeyword(), limit);
        return this;
    }

    public HybridLimits lexical(Integer limit) {
        if (mapOfLimits == null) {
            mapOfLimits = new HashMap<>();
        }
        mapOfLimits.put(DataAPIKeywords.LEXICAL.getKeyword(), limit);
        return this;
    }

}
