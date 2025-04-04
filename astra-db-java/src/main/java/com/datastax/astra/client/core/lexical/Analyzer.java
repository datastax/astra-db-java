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

import com.datastax.astra.internal.serdes.core.AnalyzerSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Analyzer used for indexing and searching 'lexical' data.
 */
@Data
@JsonSerialize(using = AnalyzerSerializer.class)
public class Analyzer {

    /** Analyzer definition as a string like 'standard' */
    String strAnalyzer;

    /**
     * Represents the tokenizer used by the analyzer.
     */
    LexicalFilter tokenizer;

    /** Represents the filters for a text analyzer */
    List<LexicalFilter> filters;

    /** Represents the char filters for a text analyzer */
    List<LexicalFilter> charFilters;

    /**
     * Default constructor.
     */
    public Analyzer() {
    }

    /**
     * Constructor with analyzer definition.
     *
     * @param strAnalyzer
     *      the analyzer definition
     */
    public Analyzer(String strAnalyzer) {
        this.strAnalyzer = strAnalyzer;
    }

    /**
     * Constructor with analyzer type.
     *
     * @param strAnalyzer
     *      the analyzer type
     */
    public Analyzer(AnalyzerTypes strAnalyzer) {
       this(strAnalyzer.getValue());
    }

    /**
     * Define a tokenizer by its name.
     *
     * @param name
     *      the analyzer name
     */
    public Analyzer tokenizer(String name) {
        return tokenizer(name, null);
    }

    /**
     * Define a tokenizer by its name and arguments.
     *
     * @param name
     *      the analyzer name
     * @param args
     *      the arguments for the analyzer
     */
    public Analyzer tokenizer(String name, Map<String, String> args) {
        this.tokenizer = new LexicalFilter().name(name).args(args);;
        return this;
    }

    /**
     * Adds a filter to the analyzer.
     *
     * @param name
     *      the name of the filter
     */
    public Analyzer addFilter(String name) {
        return addFilter(name, null);
    }

    /**
     * Adds a filter to the analyzer.
     *
     * @param name
     *      the name of the filter
     * @param args
     *      the arguments for the filter
     */
    public Analyzer addFilter(String name, Map<String, String> args) {
        if (filters == null) {
            filters = new java.util.ArrayList<>();
        }
        filters.add(new LexicalFilter().name(name).args(args));
        return this;
    }

    /**
     * Adds a char filter to the analyzer.
     *
     * @param name
     *      the name of the filter
     */
    public Analyzer addChartFilter(String name) {
        return addChartFilter(name, null);
    }

    /**
     * Adds a char filter to the analyzer.
     *
     * @param name
     *      the name of the filter
     * @param args
     *      the arguments for the filter
     */
    public Analyzer addChartFilter(String name, Map<String, String> args) {
        if (charFilters == null) {
            charFilters = new java.util.ArrayList<>();
        }
        charFilters.add(new LexicalFilter().name(name).args(args));
        return this;
    }

    /**
     * Definition of filters and tokenizers
     */
    @Getter
    @NoArgsConstructor
    public static class LexicalFilter {

        /** Name of the filter */
        String name;

        /** Arguments for the filter */
        Map<String, String> args;

        /**
         * Sets the name of the filter.
         *
         * @param name
         *      the name of the filter
         */
        public LexicalFilter name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the arguments for the filter.
         *
         * @param args
         *      the arguments for the filter
         */
        public LexicalFilter args(Map<String, String> args) {
            this.args = args;
            return this;
        }

        /**
         * Adds an argument to the filter.
         *
         * @param key
         *      the key of the argument
         * @param value
         *      the value of the argument
         */
        public LexicalFilter addArg(String key, String value) {
            if (args == null) {
                args = new java.util.HashMap<>();
            }
            args.put(key, value);
            return this;
        }
    }

}
