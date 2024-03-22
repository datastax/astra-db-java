package com.datastax.astra.internal.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom Serializer for EJson Date type.
 */
public class CustomEJsonInstantSerializer extends StdSerializer<Instant> {

    /**
     * Default constructor.
     */
    public CustomEJsonInstantSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomEJsonInstantSerializer(Class<Instant> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("$date", value.toEpochMilli());
        gen.writeEndObject();
    }
}


