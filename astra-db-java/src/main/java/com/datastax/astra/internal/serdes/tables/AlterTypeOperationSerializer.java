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


import com.datastax.astra.client.tables.commands.AlterTypeOperation;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Serializer for {@link AlterTypeOperation}.
 * <p>
 * This class is responsible for serializing an {@link AlterTypeOperation} instance into JSON format.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * AlterTypeOperation operation = new AlterTypeOperation();
 * String json = new ObjectMapper().writeValueAsString(operation);
 * }
 * </pre>
 */
public class AlterTypeOperationSerializer extends StdSerializer<AlterTypeOperation<?,?>> {

    /**
     * Default constructor.
     */
    public AlterTypeOperationSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public AlterTypeOperationSerializer(Class<AlterTypeOperation<?,?>> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(AlterTypeOperation def, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (def == null) {
            gen.writeNull();
            return;
        }
        // {"def.getOperationName":{ "fields": {"name": "type"}}}
        gen.writeStartObject();
        gen.writeFieldName(def.getOperationName());
        gen.writeObjectField("fields", def.getFields());
        gen.writeEndObject();
    }

}
