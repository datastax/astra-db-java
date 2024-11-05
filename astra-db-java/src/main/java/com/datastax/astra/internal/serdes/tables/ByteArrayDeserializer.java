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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Base64;

public class ByteArrayDeserializer extends StdDeserializer<byte[]> {

    public ByteArrayDeserializer() {
        super(byte[].class);
    }

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            String fieldName = p.nextFieldName();
            if ("$binary".equals(fieldName)) {
                p.nextToken(); // Move to the value of $binary
                String base64Value = p.getText();
                p.nextToken(); // Move past the value
                p.nextToken(); // Move past END_OBJECT
                return Base64.getDecoder().decode(base64Value);
            } else {
                throw new IOException("Expected field '$binary' but got '" + fieldName + "'");
            }
        } else {
            throw new IOException("Expected START_OBJECT token");
        }
    }

}
