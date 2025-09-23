package com.datastax.astra.internal.serdes.tables;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class MapToDataApiPairArraySerializer<K, V> extends JsonSerializer<Map<K, V>> {

    @Override
    public void serialize(Map<K, V> map, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartArray();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            gen.writeStartArray();
            serializers.defaultSerializeValue(entry.getKey(), gen);
            serializers.defaultSerializeValue(entry.getValue(), gen);
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

}
