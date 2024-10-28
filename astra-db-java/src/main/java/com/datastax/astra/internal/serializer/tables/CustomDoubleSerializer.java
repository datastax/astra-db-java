package com.datastax.astra.internal.serializer.tables;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomDoubleSerializer extends StdSerializer<Double> {

    public CustomDoubleSerializer() {
        super(Double.class);
    }

    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.equals(Double.POSITIVE_INFINITY) ||
            value.equals(Double.MAX_VALUE)) {
            gen.writeString("Infinity");
        } else if (value.equals(Double.NEGATIVE_INFINITY) ||
            value.equals(-Double.MAX_VALUE)) {
            gen.writeString("-Infinity");
        } else if (value.isNaN()) {
            gen.writeString("NaN");
        } else {
            gen.writeNumber(value);
        }
    }
}
