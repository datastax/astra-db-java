package com.datastax.astra.internal.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.UUID;

/**
 * Custom deserializer for EJson Date type.
 */
public class CustomUuidDeserializer extends JsonDeserializer<UUID> {

    /**
     * Default constructor.
     */
    public CustomUuidDeserializer() {
    }

    /** {@inheritDoc} */
    @Override
    public UUID deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String hexString = node.get("$uuid").asText();
        return UUID.fromString(hexString);
    }

}