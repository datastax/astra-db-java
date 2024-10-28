package com.datastax.astra.internal.serializer.tables;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Base64;

public class CustomByteArrayDeserializer extends StdDeserializer<byte[]> {

    public CustomByteArrayDeserializer() {
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
