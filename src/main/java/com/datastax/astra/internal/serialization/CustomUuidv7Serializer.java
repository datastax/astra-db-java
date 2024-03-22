package com.datastax.astra.internal.serialization;

import com.datastax.astra.internal.types.UUIDv7;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Object Id Could be
 * objectId|uuid|uuidv6|uuidv7
 */
public class CustomUuidv7Serializer extends StdSerializer<UUIDv7> {
    /**
     * Default constructor.
     */
    public CustomUuidv7Serializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomUuidv7Serializer(Class<UUIDv7> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(UUIDv7 uuidv7, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("$uuidv7", uuidv7.toString());
        gen.writeEndObject();
    }
}
