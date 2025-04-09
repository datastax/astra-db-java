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

import com.datastax.astra.client.tables.definition.indexes.TableIndexColumnDefinition;
import com.datastax.astra.client.tables.definition.indexes.TableIndexMapTypes;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializer for TableIndexColumnDefinition.
 *
 * {"column": "name"}
 *
 */
public class TableIndexColumnDefinitionSerializer extends JsonSerializer<TableIndexColumnDefinition> {

    /**
     * Default constructor
     */
    public TableIndexColumnDefinitionSerializer() {
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(TableIndexColumnDefinition def, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (def == null) {
            gen.writeNull();
            return;
        }
        // {"name" : "type" }}
        if (def.getType() != null) {
            if (TableIndexMapTypes.ENTRIES.equals(def.getType())) {
                gen.writeString(def.getName());
                return;
            }
            gen.writeStartObject();
            gen.writeStringField(def.getName(), def.getType().getValue());
            gen.writeEndObject();
            return;
        } else {
            // "name"
            gen.writeString(def.getName());
        }

    }


}
