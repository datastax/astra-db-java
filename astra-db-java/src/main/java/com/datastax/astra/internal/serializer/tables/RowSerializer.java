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

package com.datastax.astra.internal.serializer.tables;

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

import com.datastax.astra.client.tables.columns.ColumnTypes;
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

/**
 * Custom implementation of serialization : faster + no jackson dependency
 * 
 * @author Cedrick Lunven (@clunven)
 */
@SuppressWarnings("deprecation")
public class RowSerializer implements DataAPISerializer {

    /** Object mapper with customization fo data API. */
    private ObjectMapper objectMapper;

    /**
     * Default constructor
     */
    public RowSerializer() {
        // left blank, hiding constructor for utility class
    }

    /**
     * Building the data api specific object mapper.
     *
     * @return
     *      object mapper.
     */
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
                    .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                    .setSerializationInclusion(Include.NON_NULL)
                    .setAnnotationIntrospector(new JacksonAnnotationIntrospector());

            SimpleModule module = new SimpleModule();

            // Serialization
            module.addSerializer(ColumnTypes.class, new CustomColumnTypeSerializer());
            module.addSerializer(Double.class, new CustomDoubleSerializer());
            module.addSerializer(double.class, new CustomDoubleSerializer());
            module.addSerializer(Float.class, new CustomFloatSerializer());
            module.addSerializer(float.class, new CustomFloatSerializer());
            module.addSerializer(byte[].class, new CustomByteArraySerializer());

            // De-Serialization
            module.addDeserializer(ColumnTypes.class, new CustomColumnTypeDeserializer());
            module.addDeserializer(Float.class, new CustomFloatDeserializer());
            module.addDeserializer(float.class, new CustomFloatDeserializer());
            module.addDeserializer(Double.class, new CustomDoubleDeserializer());
            module.addDeserializer(double.class, new CustomDoubleDeserializer());
            module.addDeserializer(byte[].class, new CustomByteArrayDeserializer());
            objectMapper.registerModule(module);
        }
        return objectMapper;
    }



}
