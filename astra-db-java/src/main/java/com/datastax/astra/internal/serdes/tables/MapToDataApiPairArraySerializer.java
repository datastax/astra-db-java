package com.datastax.astra.internal.serdes.tables;

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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * Serialize a Map as an array of pairs (arrays of two elements).
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MapToDataApiPairArraySerializer<K, V> extends JsonSerializer<Map<K, V>> {

    /**
     * Default constructor.
     */
    public MapToDataApiPairArraySerializer() {}

    /**
     * Default constructor.
     */
    @Override
    public void serialize(Map<K, V> map, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartArray();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            gen.writeStartArray();
            serializers.defaultSerializeValue(entry.getKey(), gen);
            serializers.defaultSerializeValue(entry.getValue(), gen);
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

}
