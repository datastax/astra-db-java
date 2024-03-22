package com.datastax.astra.internal.serialization;

import com.datastax.astra.internal.types.ObjectId;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Custom deserializer for EJson Date type.
 */
public class CustomObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    /**
     * Default constructor.
     */
    public CustomObjectIdDeserializer() {
    }

    /** {@inheritDoc} */
    @Override
    public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String hexString = node.get("$objectId").asText();
        return new ObjectId(hexString);
    }

}