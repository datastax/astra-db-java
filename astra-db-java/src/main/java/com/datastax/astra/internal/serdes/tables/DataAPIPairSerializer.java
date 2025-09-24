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

import com.datastax.astra.client.tables.DataAPIPair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serialize a pair as an array of two elements.
 */
public class DataAPIPairSerializer extends JsonSerializer<DataAPIPair<?, ?>> {

    /**
     * Default constructor.
     */
    public DataAPIPairSerializer() {}

    /**
     * Serialize a pair as an array of two elements.
     *
     * @param pair        the pair to serialize
     * @param gen         the JSON generator
     * @param serializers the serializer provider
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void serialize(DataAPIPair<?, ?> pair, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        gen.writeObject(pair.getKey());
        gen.writeObject(pair.getValue());
        gen.writeEndArray();
    }
}
