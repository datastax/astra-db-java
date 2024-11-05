package com.datastax.astra.internal.serdes.tables;

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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class FloatSerializer extends StdSerializer<Float> {

    public FloatSerializer() {
        super(Float.class);
    }

    @Override
    public void serialize(Float value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.equals(Float.POSITIVE_INFINITY) ||
            value.equals(Float.MAX_VALUE)) {
            gen.writeString("Infinity");
        } else if (value.equals(Float.NEGATIVE_INFINITY) ||
            value.equals(-Float.MAX_VALUE)) {
            gen.writeString("-Infinity");
        } else if (value.isNaN()) {
            gen.writeString("NaN");
        } else {
            gen.writeNumber(value);
        }
    }
}
