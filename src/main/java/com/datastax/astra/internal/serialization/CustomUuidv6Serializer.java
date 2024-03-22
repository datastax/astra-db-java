package com.datastax.astra.internal.serialization;

import com.datastax.astra.internal.types.UUIDv6;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Object Id Could be
 * objectId|uuid|uuidv6|uuidv7
 */
public class CustomUuidv6Serializer extends StdSerializer<UUIDv6> {
    /**
     * Default constructor.
     */
    public CustomUuidv6Serializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomUuidv6Serializer(Class<UUIDv6> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(UUIDv6 uuidv6, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("$uuid", uuidv6.toString());
        gen.writeEndObject();
    }
}
