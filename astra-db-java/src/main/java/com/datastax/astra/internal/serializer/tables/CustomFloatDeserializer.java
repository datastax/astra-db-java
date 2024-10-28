package com.datastax.astra.internal.serializer.tables;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CustomFloatDeserializer extends StdDeserializer<Float> {

    public CustomFloatDeserializer() {
        super(Float.class);
    }

    @Override
    public Float deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        return switch (text) {
            case "Infinity" -> Float.POSITIVE_INFINITY;
            case "-Infinity" -> Float.NEGATIVE_INFINITY;
            case "NaN" -> Float.NaN;
            default -> Float.parseFloat(text);
        };
    }
}
