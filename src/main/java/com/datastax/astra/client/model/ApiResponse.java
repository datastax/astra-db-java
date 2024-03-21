package com.datastax.astra.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.stargate.sdk.utils.JsonUtils;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents the Api response.
 */
@Data
public class ApiResponse {

    /**
     * Return by all operations except find*()
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Document status;

    /**
     * List of errors, could be one per inserted items with reason.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ApiError> errors;

    /**
     * Data retrieve with operations find
     */
    private ApiData data;

    /**
     * Default constructor.
     */
    public ApiResponse() {
    }

    /**
     * Read a value as a stream from the key/value 'status' map.
     *
     * @param key
     *      key to be retrieved
     * @return
     *      list of values
     */
    @SuppressWarnings("unchecked")
    public Stream<String> getStatusKeyAsStringStream(@NonNull String key) {
        if (status.containsKey(key)) {
            return ((ArrayList<String>) status.get(key)).stream();
        }
        return Stream.empty();
    }

    /**
     * Read a value as a List from the key/value 'status' map.
     *
     * @param key
     *      target get
     * @param targetClass
     *      target class
     * @return
     *      object
     * @param <T>
     *      type in used
     */
    public <T> T getStatusKeyAsObject(@NonNull String key, @NonNull Class<T> targetClass) {
        if (status.containsKey(key)) {
            return JsonUtils.convertValue(status.get(key), targetClass);
        }
        return null;
    }

    /**
     * Read a value as a Specialized class from the key/value 'status' map.
     *
     * @param key
     *      target get
     * @param targetClass
     *      target class
     * @return
     *      object
     * @param <T>
     *      type in used
     */
    public <T> List<T> getStatusKeyAsList(@NonNull String key, Class<T> targetClass) {
        if (status.containsKey(key)) {
            return JsonUtils.getObjectMapper().convertValue(status.get(key),
                   JsonUtils.getObjectMapper().getTypeFactory()
                           .constructCollectionType(List.class, targetClass));
        }
        return null;
    }

    /**
     * Read a value as an Integer from the key/value 'status' map.
     *
     * @param key
     *      key to be retrieved
     * @return
     *      list of values
     */
    public Integer getStatusKeyAsInt(@NonNull String key) {
        if (status.containsKey(key)) {
            return (Integer) status.get(key);
        }
        throw new IllegalArgumentException("Key '" + key + "' does not exist in status");
    }

    /**
     * Read a value as an Integer from the key/value 'status' map.
     *
     * @param key
     *      key to be retrieved
     * @return
     *      list of values
     */
    public Boolean getStatusKeyAsBoolean(@NonNull String key) {
        if (status.containsKey(key)) {
            return (Boolean) status.get(key);
        }
        throw new IllegalArgumentException("Key '" + key + "' does not exist in status");
    }

}
