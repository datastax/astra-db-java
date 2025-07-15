package com.datastax.astra.internal.serdes.collections;

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

import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeDefinition;
import com.datastax.astra.client.tables.definition.types.TableUserDefinedTypeFieldDefinition;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Map;

/**
 * This would serialize a UDT definition with the short format, (not showing the type)
 */
public class UdtDefinitionShortSerializer extends StdSerializer<TableUserDefinedTypeDefinition> {

    /**
     * Default constructor.
     */
    public UdtDefinitionShortSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public UdtDefinitionShortSerializer(Class<TableUserDefinedTypeDefinition> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(TableUserDefinedTypeDefinition type, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("fields");
        if (type.getFields() != null) {
            for (Map.Entry<String, TableUserDefinedTypeFieldDefinition> entry : type.getFields().entrySet()) {
                gen.writeStringField(entry.getKey(), entry.getValue().getType().getValue());
            }
        }
        gen.writeEndObject(); // end of "fields"
        gen.writeEndObject(); // end of root object
    }
}
