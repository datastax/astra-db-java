package com.datastax.astra.internal.serialization;

import com.datastax.astra.internal.types.ObjectId;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Object Id Could be
 * objectId|uuid|uuidv6|uuidv7
 */
public class CustomObjectIdSerializer  extends StdSerializer<ObjectId> {
    /**
     * Default constructor.
     */
    public CustomObjectIdSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomObjectIdSerializer(Class<ObjectId> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(ObjectId objectId, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("$objectId", objectId.toHexString());
        gen.writeEndObject();
    }

}
