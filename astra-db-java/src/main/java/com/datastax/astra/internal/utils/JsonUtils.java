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

package com.datastax.astra.internal.utils;

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

import com.datastax.astra.client.model.ObjectId;
import com.datastax.astra.client.model.UUIDv6;
import com.datastax.astra.client.model.UUIDv7;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Objects;
import java.util.UUID;

/**
 * Custom implementation of serialization : faster + no jackson dependency
 * 
 * @author Cedrick Lunven (@clunven)
 */
@SuppressWarnings("deprecation")
public class JsonUtils {

    /** Object mapper with customization fo data API. */
    private static ObjectMapper dataApiObjectMapper;

    /**
     * Building the data api specific object mapper.
     *
     * @return
     *      object mapper.
     */
    public static synchronized ObjectMapper getDataApiObjectMapper() {
        if (dataApiObjectMapper == null) {
            dataApiObjectMapper = new ObjectMapper()
                    .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                    .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
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
            dataApiObjectMapper.registerModule(module);
        }
        return dataApiObjectMapper;
    }

    /**
     * Default constructor
     */
    private JsonUtils() {
    }

    /**
     * Transform object as a String.
     *
     * @param o
     *      object to be serialized.
     * @return
     *      body as String
     */
    public static String marshall(Object o) {
        Objects.requireNonNull(o);
        try {
            if (o instanceof String) {
                return (String) o;
            }
            return getDataApiObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("Cannot marshall object " + o, e);
        }
    }

    /**
     * Jackson deserialization.
     * @param bean
     *      current beam
     * @param clazz
     *      target class
     * @return
     *      serialized
     * @param <T>
     *     current type
     */
    public static <T> T convertValue(Object bean, Class<T> clazz) {
        return  getDataApiObjectMapper().convertValue(bean, clazz);
    }

    /**
     * Load body as expected object.
     *
     * @param <T>
     *      parameter
     * @param body
     *      response body as String
     * @param ref
     *      type Reference to map the result
     * @return
     *       expected objects
     */
    public static <T> T unMarshallBean(String body, Class<T> ref) {
        try {
            return getDataApiObjectMapper().readValue(body, ref);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot unmarshall object " + body, e);
        }
    }
}
