package com.datastax.astra.internal.serialization;

import com.datastax.astra.internal.types.UUIDv6;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Custom deserializer for EJson Date type.
 */
public class CustomUuidv6Deserializer extends JsonDeserializer<UUIDv6> {

    /**
     * Default constructor.
     */
    public CustomUuidv6Deserializer() {
    }

    /** {@inheritDoc} */
    @Override
    public UUIDv6 deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String hexString = node.get("$uuidv6").asText();
        return UUIDv6.fromString(hexString);
    }

}