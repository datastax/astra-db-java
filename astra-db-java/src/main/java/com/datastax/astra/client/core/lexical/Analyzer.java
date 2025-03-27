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
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@JsonSerialize(using = AnalyzerSerializer.class)
public class Analyzer {

    /** In the case of String analyzer */
    String strAnalyzer;

    /** In the case of Document analyzer, free structure */
    LexicalFilter tokenizer;

    List<LexicalFilter> filters;

    List<LexicalFilter> charFilters;

    public Analyzer() {
    }

    public Analyzer(String strAnalyzer) {
        this.strAnalyzer = strAnalyzer;
    }

    public Analyzer tokenizer(String name) {
        return tokenizer(name, null);
    }
    public Analyzer tokenizer(String name, Map<String, String> args) {
        this.tokenizer = new LexicalFilter().name(name).args(args);;
        return this;
    }

    public Analyzer addFilter(String name) {
        return addFilter(name, null);
    }
    public Analyzer addFilter(String name, Map<String, String> args) {
        if (filters == null) {
            filters = new java.util.ArrayList<>();
        }
        filters.add(new LexicalFilter().name(name).args(args));
        return this;
    }

    public Analyzer addChartFilter(String name) {
        return addChartFilter(name, null);
    }
    public Analyzer addChartFilter(String name, Map<String, String> args) {
        if (charFilters == null) {
            charFilters = new java.util.ArrayList<>();
        }
        charFilters.add(new LexicalFilter().name(name).args(args));
        return this;
    }

    public Analyzer(AnalyzerTypes strAnalyzer) {
        this.strAnalyzer = strAnalyzer.getValue();
    }

    @NoArgsConstructor
    public static class LexicalFilter {

        String name;

        Map<String, String> args;

        public LexicalFilter name(String name) {
            this.name = name;
            return this;
        }

        public LexicalFilter args(Map<String, String> args) {
            this.args = args;
            return this;
        }

        public LexicalFilter arg(String key, String value) {
            if (args == null) {
                args = new java.util.HashMap<>();
            }
            args.put(key, value);
            return this;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getArgs() {
            return args;
        }
    }



}
