package com.datastax.astra.internal.serdes;

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

import com.datastax.astra.client.core.options.DataAPIOptions;
import com.datastax.astra.client.core.vector.DataAPIVector;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Serializer for DataAPIVector
 */
public class DataAPIVectorSerializer extends StdSerializer<DataAPIVector> {

    public DataAPIVectorSerializer() {
        super(DataAPIVector.class);
    }

    @Override
    public void serialize(DataAPIVector dataApiVector, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (dataApiVector == null) {
            gen.writeNull();
        } else if (DataAPIOptions.encodeDataApiVectorsAsBase64) {
            // Binary ENCODING
            final int vectorLen = dataApiVector.getEmbeddings().length;
            final byte[] b = new byte[vectorLen << 2];
            for (int i = 0, out = 0; i < vectorLen; i++) {
                final int floatBits = Float.floatToIntBits(dataApiVector.getEmbeddings()[i]);
                b[out++] = (byte) (floatBits >> 24);
                b[out++] = (byte) (floatBits >> 16);
                b[out++] = (byte) (floatBits >> 8);
                b[out++] = (byte) (floatBits);
            }
            // Second: write packed bytes (for JSON, Base64 encoded)
            gen.writeBinary(b);
        } else {
            // DEFAULT FLOAT ARRAY
            gen.writeStartArray();
            for (float f : dataApiVector.getEmbeddings()) {
                gen.writeNumber(f);
            }
            gen.writeEndArray();
        }
    }
}
