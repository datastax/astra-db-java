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
 * Lexical filters used for indexing and searching 'lexical' data.
 */
@Getter
public enum LexicalFilters {

    /**
     * Filters StandardTokenizer output that divides text
     * into terms on word boundaries and then uses the LowerCaseFilter.
     */
    LOWERCASE("lowercase"),

    /**
     * Filter with stop words
     */
    STOP("stop"),

    /**
     * Filter with synonyms
     */
    SYNONYM("synonym"),

    /**
     * Filter with synonyms and graph
     */
    SYNONYM_GRAPH("synonym_graph");

    /**
     * The value of the filter.
     */
    final String value;

    /**
     * Constructor.
     *
     * @param value
     *      the value of the filter.
     */
    LexicalFilters(String value) {
        this.value = value;
    }

}
