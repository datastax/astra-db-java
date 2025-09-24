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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserialize a list of pairs (arrays of two elements) into a Map.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class DataAPIPairArrayDeserializerToMap<K, V>
        extends JsonDeserializer<Map<K, V>>
        implements ContextualDeserializer {

    /** Key type. */
    private JavaType keyType;

    /** Value type. */
    private JavaType valueType;

    /**
     * Default constructor.
     */
    public DataAPIPairArrayDeserializerToMap() {
        // default constructor
    }

    /**
     * Constructor with key/value types.
     *
     * @param keyType key type
     * @param valueType value type
     */
    public DataAPIPairArrayDeserializerToMap(JavaType keyType, JavaType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<K, V> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        Map<K, V> map = new LinkedHashMap<>();

        if (p.currentToken() == null) p.nextToken();
        if (p.currentToken() != JsonToken.START_ARRAY) {
            throw JsonMappingException.from(p, "Expected START_ARRAY for list of pairs");
        }

        while (p.nextToken() != JsonToken.END_ARRAY) {
            if (p.currentToken() != JsonToken.START_ARRAY) {
                throw JsonMappingException.from(p, "Expected START_ARRAY for pair");
            }

            p.nextToken();
            K key = mapper.readValue(p, keyType);

            p.nextToken();
            V value = mapper.readValue(p, valueType);

            map.put(key, value);

            if (p.nextToken() != JsonToken.END_ARRAY) {
                throw JsonMappingException.from(p, "Expected END_ARRAY after pair");
            }
        }

        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            JavaType type = property.getType(); // Map<K,V>
            MapType mapType = (MapType) type;
            return new DataAPIPairArrayDeserializerToMap<>(mapType.getKeyType(), mapType.getContentType());
        }
        return this;
    }
}
