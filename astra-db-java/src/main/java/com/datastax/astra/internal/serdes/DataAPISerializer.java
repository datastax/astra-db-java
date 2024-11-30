package com.datastax.astra.internal.serdes;

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

import com.datastax.astra.client.exceptions.DataAPIException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import static com.datastax.astra.client.exceptions.DataAPIException.ERROR_CODE_SERIALIZATION;

/**
 * Definition of a serializer for the Data API
 */
public interface DataAPISerializer {

    /**
     * Access the Jackson Mapper
     * @return
     *      jackson mapper
     */
    ObjectMapper getMapper();

    /**
     * Transform object as a String.
     *
     * @param o
     *      object to be serialized.
     * @return
     *      body as String
     */
    default String marshall(Object o) {
        Objects.requireNonNull(o);
        try {
            if (o instanceof String) {
                return (String) o;
            }
            return getMapper().writeValueAsString(o);
        } catch (Exception e) {
            throw new DataAPIException(ERROR_CODE_SERIALIZATION, "Cannot marshall object " + o, e);
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
    @SuppressWarnings("unchecked")
    default <T> T convertValue(Object bean, Class<T> clazz) {
        if (bean == null) {
            return null;
        }
        if (bean.getClass() == clazz) {
            return (T) bean;
        }
        return getMapper().convertValue(bean, clazz);
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
    default <T> T unMarshallBean(String body, Class<T> ref) {
        try {
            return getMapper().readValue(body, ref);
        } catch (JsonProcessingException e) {
            throw new DataAPIException(ERROR_CODE_SERIALIZATION, "Cannot unmarshall object " + body, e);
        }
    }


}
