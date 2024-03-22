package com.datastax.astra.internal.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Calendar;

/**
 * Custom Serializer for EJson Date type.
 */
public class CustomEJsonCalendarSerializer extends StdSerializer<Calendar> {

    /**
     * Default constructor.
     */
    public CustomEJsonCalendarSerializer() {
        this(null);
    }

    /**
     * Constructor with type
     * @param t
     *      type
     */
    public CustomEJsonCalendarSerializer(Class<Calendar> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Calendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("$date", value.getTime().getTime());
        gen.writeEndObject();
    }
}


