/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.astra.internal.serializer.collections;

/*-
 * #%L
 * Data API Java Client
 * --
 * Copyright (C) 2024 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.types.ObjectId;
import com.datastax.astra.client.core.types.UUIDv6;
import com.datastax.astra.client.core.types.UUIDv7;
import com.datastax.astra.internal.serializer.DataAPISerializer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Custom implementation of serialization : faster + no jackson dependency
 * 
 * @author Cedrick Lunven (@clunven)
 */
@SuppressWarnings("deprecation")
public class DocumentSerializer implements DataAPISerializer {

    /** Object mapper with customization fo data API. */
    private ObjectMapper objectMapper;

    /**
     * Default constructor
     */
    public DocumentSerializer() {
        // left blank, hiding constructor for utility class
    }

    @Override
    public ObjectMapper getMapper() {
        if (objectMapper == null) {
            JsonFactory jsonFactory = JsonFactory.builder()
                    .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                    .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(StreamReadFeature.USE_FAST_BIG_NUMBER_PARSER)
                    .enable(StreamReadFeature.USE_FAST_DOUBLE_PARSER)
                    .enable(StreamWriteFeature.USE_FAST_DOUBLE_WRITER)
                    .build();
            objectMapper = new ObjectMapper(jsonFactory)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .registerModule(new JavaTimeModule())
                    .setDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
                    .setSerializationInclusion(Include.NON_NULL)
                    .setAnnotationIntrospector(new JacksonAnnotationIntrospector());

            SimpleModule module = new SimpleModule();
            // Date
            module.addSerializer(Date.class, new CustomEJsonDateSerializer());
            module.addDeserializer(Date.class, new CustomEJsonDateDeserializer());
            // Calendar
            module.addSerializer(Calendar.class, new CustomEJsonCalendarSerializer());
            module.addDeserializer(Calendar.class, new CustomEJsonCalendarDeserializer());
            // Instant
            module.addSerializer(Instant.class, new CustomEJsonInstantSerializer());
            module.addDeserializer(Instant.class, new CustomEJsonInstantDeserializer());
            // UUID
            module.addSerializer(UUID.class, new CustomUuidSerializer());
            module.addDeserializer(UUID.class, new CustomUuidDeserializer());
            // UUIDv6
            module.addSerializer(UUIDv6.class, new CustomUuidv6Serializer());
            // UUIDv7
            module.addSerializer(UUIDv7.class, new CustomUuidv7Serializer());
            // ObjectId
            module.addSerializer(ObjectId.class, new CustomObjectIdSerializer());
            module.addDeserializer(ObjectId.class, new CustomObjectIdDeserializer());
            objectMapper.registerModule(module);
        }
        return objectMapper;
    }
}
