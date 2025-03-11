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

import com.datastax.astra.client.core.DataAPIKeywords;
import com.datastax.astra.client.core.vectorize.Vectorize;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 * Serializer for Vectorize
 */
public class VectorizeSerializer extends JsonSerializer<Vectorize> {

    @Override
    public void serialize(Vectorize value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.getSetPassage() == null || value.getSetPassage().isEmpty()) {
            gen.writeString(value.getPassage());
        } else {
            gen.writeStartObject();
            gen.writeStringField(DataAPIKeywords.PASSAGE.getKeyword(), value.getPassage());
            gen.writeStringField(DataAPIKeywords.SET_PASSAGE.getKeyword(), value.getSetPassage());
            gen.writeEndObject();
        }
    }
}
