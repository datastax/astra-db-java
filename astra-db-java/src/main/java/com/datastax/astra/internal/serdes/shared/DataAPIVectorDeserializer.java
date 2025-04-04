package com.datastax.astra.internal.serdes.shared;

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

import com.datastax.astra.client.core.vector.DataAPIVector;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

/**
 * Deserializer for DataAPIVector.
 */
public class DataAPIVectorDeserializer extends StdScalarDeserializer<DataAPIVector>  {

    /**
     * Default constructor.
     */
    public DataAPIVectorDeserializer() {
        super(DataAPIVector.class);
    }

    /** {@inheritDoc} */
    @Override
    public DataAPIVector deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            final JsonToken t = p.currentToken();

            // Expecting {"$binary":"PszMzb8ZmZo+TMzN"}
            if (t == JsonToken.START_OBJECT) {
                String fieldName = p.nextFieldName();
                if ("$binary".equals(fieldName)) {
                    p.nextToken(); // Move to the value of $binary
                    byte[] base64Value = p.getBinaryValue();
                    p.nextToken(); // Move past the value
                    p.nextToken(); // Move past END_OBJECT
                    return new DataAPIVector(unpack(ctxt, base64Value));
                }
            // Understands [0.4, -0.6, 0.2]
            } else if (t == JsonToken.START_ARRAY) {
                float[] floats = ctxt.readValue(p, float[].class);
                return new DataAPIVector(floats);
            } else if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
                Object emb = p.getEmbeddedObject();
                if (emb instanceof byte[]) {
                    return new DataAPIVector(unpack(ctxt, (byte[]) emb));
                } else if (emb instanceof float[]) {
                    return new DataAPIVector( (float[]) emb);
                }
            } else if (t == JsonToken.VALUE_STRING) {
                return new DataAPIVector(unpack(ctxt, p.getBinaryValue()));
            }

            return new DataAPIVector((float[]) ctxt.handleUnexpectedToken(_valueClass, p));
    }

    /**
     * Unpacks a byte array into an array of floats.
     *
     * @param ctxt the {@link DeserializationContext} in which the deserializer is operating
     * @param bytes the byte array to unpack
     * @return an array of floats
     * @throws IOException if the byte array is not a multiple of 4 bytes
     */
    private final float[] unpack(DeserializationContext ctxt, byte[] bytes)
    throws IOException {
        final int bytesLen = bytes.length;
        if ((bytesLen & 3) != 0) {
            return (float[]) ctxt.reportInputMismatch(_valueClass,
                    "Vector length (%d) not a multiple of 4 bytes", bytesLen);
        }
        final int vectorLen = bytesLen >> 2;
        final float[] floats = new float[vectorLen];
        for (int in = 0, out = 0; in < bytesLen; ) {
            int packed = (bytes[in++] << 24)
                    | ((bytes[in++] & 0xFF) << 16)
                    | ((bytes[in++] & 0xFF) << 8)
                    | (bytes[in++] & 0xFF);
            floats[out++] = Float.intBitsToFloat(packed);
        }
        return floats;
    }
}
