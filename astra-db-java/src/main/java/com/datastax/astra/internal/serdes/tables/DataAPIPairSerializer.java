package com.datastax.astra.internal.serdes.tables;

import com.datastax.astra.client.tables.DataAPIPair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serialize a pair as an array of two elements.
 */
public class DataAPIPairSerializer extends JsonSerializer<DataAPIPair<?, ?>> {

    @Override
    public void serialize(DataAPIPair<?, ?> pair, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        gen.writeObject(pair.getKey());
        gen.writeObject(pair.getValue());
        gen.writeEndArray();
    }
}
