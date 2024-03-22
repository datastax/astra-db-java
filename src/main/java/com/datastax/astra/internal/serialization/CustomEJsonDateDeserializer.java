package com.datastax.astra.internal.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Date;

/**
 * Custom deserializer for EJson Date type.
 */
public class CustomEJsonDateDeserializer extends JsonDeserializer<Date> {

    /**
     * Default constructor.
     */
    public CustomEJsonDateDeserializer() {
    }

    /** {@inheritDoc} */
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        long timestamp = node.get("$date").asLong();
        return new Date(timestamp);
    }

}