package com.datastax.astra.internal.serializer.tables;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class CustomDoubleDeserializer extends StdDeserializer<Double> {

    public CustomDoubleDeserializer() {
        super(Double.class);
    }

    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        return switch (text) {
            case "Infinity" -> Double.POSITIVE_INFINITY;
            case "-Infinity" -> Double.NEGATIVE_INFINITY;
            case "NaN" -> Double.NaN;
            default -> Double.parseDouble(text);
        };
    }
}
