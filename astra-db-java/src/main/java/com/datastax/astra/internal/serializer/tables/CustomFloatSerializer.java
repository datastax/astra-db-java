package com.datastax.astra.internal.serializer.tables;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomFloatSerializer extends StdSerializer<Float> {

    public CustomFloatSerializer() {
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
