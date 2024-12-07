package com.datastax.astra.internal.serdes.collections;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
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

import com.datastax.astra.client.collections.definition.CollectionDefaultIdTypes;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * A custom serializer for {@link CollectionDefaultIdTypes} values, extending {@link StdSerializer}.
 * This serializer handles special cases of collection default id types, converting them
 * to their textual representations in JSON.
 */
public class CollectionDefaultIdTypeSerializer extends StdSerializer<CollectionDefaultIdTypes> {

    /**
     * Default constructor.
     */
    public CollectionDefaultIdTypeSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CollectionDefaultIdTypeSerializer(Class<CollectionDefaultIdTypes> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(CollectionDefaultIdTypes defaultId, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(defaultId.getValue());
    }
}
