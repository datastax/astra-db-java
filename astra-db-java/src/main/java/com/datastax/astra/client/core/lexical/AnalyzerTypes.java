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

/**
 * Enum representing different types of analyzers.
 * Each enum constant corresponds to a specific analyzer type.
 */
@Getter
public enum AnalyzerTypes {

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    STANDARD("standard"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    LETTER("letter"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    LOWERCASE("lowercase"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    WHITESPACE("whitespace"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    N_GRAM("n-gram"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    EDGE_N_GRAM("edge_n-gram"),

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    KEYWORD("keyword");

    /**
     * The string value of the analyzer type.
     */
    final String value;

    /**
     * Constructor for the enum.
     *
     * @param value
     *      string value
     */
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
