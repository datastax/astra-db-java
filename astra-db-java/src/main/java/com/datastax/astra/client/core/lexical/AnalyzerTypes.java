package com.datastax.astra.client.core.lexical;

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

import lombok.Getter;

@Getter
public enum AnalyzerTypes {

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    STANDARD("standard"),

    LETTER("letter"),

    LOWERCASE("lowercase"),

    WHITESPACE("whitespace"),

    N_GRAM("n-gram"),

    EDGE_N_GRAM("edge_n-gram"),

    KEYWORD("keyword");

    final String value;

    AnalyzerTypes(String value) {
        this.value = value;
    }

    /**
     * Build from the key.
     *
     * @param value
     *      string value
     * @return
     *      enum value
     */
    public static LexicalFilters fromValue(String value) {
        for (LexicalFilters filter : LexicalFilters.values()) {
            if (filter.getValue().equalsIgnoreCase(value)) {
                return filter;
            }
        }
        throw new IllegalArgumentException("Unknown LexicalFilters: " + value);
    }

}
