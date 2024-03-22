package com.datastax.astra.internal.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom deserializer for EJson Date type.
 */
public class CustomEJsonInstantDeserializer extends JsonDeserializer<Instant> {

    /**
     * Default constructor.
     */
    public CustomEJsonInstantDeserializer() {
    }

    /** {@inheritDoc} */
    @Override
    public Instant deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        long timestamp = node.get("$date").asLong();
        return Instant.ofEpochMilli(timestamp);
    }

}