package com.datastax.astra.internal.serialization;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Date;

/**
 * Custom Serializer for EJson Date type.
 */
public class CustomEJsonDateSerializer extends StdSerializer<Date> {

    /**
     * Default constructor.
     */
    public CustomEJsonDateSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomEJsonDateSerializer(Class<Date> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("$date", value.getTime());
        gen.writeEndObject();
    }
}


