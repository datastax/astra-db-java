package com.datastax.astra.internal.serializer;

import com.datastax.astra.client.exception.DataApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import static com.datastax.astra.client.exception.DataApiException.ERROR_CODE_SERIALIZATION;

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
            throw new DataApiException(ERROR_CODE_SERIALIZATION, "Cannot marshall object " + o, e);
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
            throw new DataApiException(ERROR_CODE_SERIALIZATION, "Cannot unmarshall object " + body, e);
        }
    }


}
