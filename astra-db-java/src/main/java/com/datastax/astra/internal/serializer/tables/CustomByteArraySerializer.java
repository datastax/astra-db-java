package com.datastax.astra.internal.serializer.tables;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Base64;

public class CustomByteArraySerializer extends StdSerializer<byte[]> {

    public CustomByteArraySerializer() {
        super(byte[].class);
    }

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String base64Encoded = Base64.getEncoder().encodeToString(value);
        gen.writeStartObject();
        gen.writeStringField("$binary", base64Encoded);
        gen.writeEndObject();
    }
}
