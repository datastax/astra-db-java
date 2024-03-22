package com.datastax.astra.internal.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.UUID;

/**
 * Object Id Could be
 * objectId|uuid|uuidv6|uuidv7
 */
public class CustomUuidSerializer extends StdSerializer<UUID> {
    /**
     * Default constructor.
     */
    public CustomUuidSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomUuidSerializer(Class<UUID> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(UUID uuid, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("$uuid", uuid.toString());
        gen.writeEndObject();
    }
}
