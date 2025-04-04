package com.datastax.astra.internal.serdes.core;

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

import com.datastax.astra.client.core.hybrid.HybridLimits;
import com.datastax.astra.client.core.lexical.Analyzer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * Serializer for TableIndexColumnDefinition.
 *
 * {"column": "name"}
 *
 */
public class AnalyzerSerializer extends JsonSerializer<Analyzer> {

    @Override
    public void serialize(Analyzer analyzer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (analyzer == null) {
            gen.writeNull();
            return;
        }
        if (analyzer.getStrAnalyzer() != null) {
            gen.writeString(analyzer.getStrAnalyzer());
        } else {
            gen.writeStartObject();
            if (analyzer.getTokenizer() != null) {
                gen.writeObjectField("tokenizer", analyzer.getTokenizer());
            }
            if (analyzer.getFilters() != null) {
                gen.writeObjectField("filters", analyzer.getFilters());
            }
            if (analyzer.getCharFilters() != null) {
                gen.writeObjectField("charFilters", analyzer.getCharFilters());
            }
            gen.writeEndObject();
        }
    }


}
