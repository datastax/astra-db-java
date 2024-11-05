package com.datastax.astra.internal.api;

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

import com.datastax.astra.client.collections.documents.Document;
import com.datastax.astra.internal.serdes.DataAPISerializer;
import com.datastax.astra.internal.utils.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents the generic response structure for API calls. This class encapsulates different segments of the response,
 * such as status information, error details, and data returned by 'find' operations. It provides flexibility to handle
 * various types of responses within a unified framework.
 */
@Getter
@Setter
public class ApiResponse implements Serializable {

    /**
     * The {@link DataAPISerializer} instance used to serialize and deserialize data objects.
     */
    @JsonIgnore
    private DataAPISerializer serializer;

    /**
     * Holds status information returned by the API for all operations except those prefixed with 'find'.
     * This can include status codes, messages, or any other relevant status details in a {@link Document} format.
     * The inclusion of this field in the response is conditional and based on the presence of status information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Document status;

    /**
     * Contains a list of {@link ApiError} objects representing errors that occurred during the API call.
     * This field is especially relevant for batch operations where multiple items are processed, and individual errors
     * may occur for each item. The inclusion of this field is conditional and based on the presence of errors.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private transient List<ApiError> errors;

    /**
     * Encapsulates the data retrieved by operations prefixed with 'find'. This field is populated with the results
     * from such queries, packaging the returned data within an {@link ApiData} object.
     */
    private transient ApiData data;

    /**
     * Default constructor for {@link ApiResponse}. Initializes a new instance of the class without setting any properties.
     */
    public ApiResponse() {
        // left blank, will be populated by jackson
    }

    /**
     * Retrieves a stream of {@link String} values from the 'status' map based on the provided key.
     * This method is useful for processing multiple values associated with a single key in the status information.
     *
     * @param key The key for which to retrieve the values.
     * @return A {@link Stream} of {@link String} values associated with the specified key; an empty stream if the key does not exist.
     */
    @SuppressWarnings("unchecked")
    public Stream<String> getStatusKeyAsStringStream(@NonNull String key) {
        Assert.isTrue(status.containsKey(key), "Key not found in status map");
        return ((ArrayList<String>) status.get(key)).stream();
    }

    /**
     * Retrieves a list of objects from the 'status' map based on the provided key, casting them to the specified class.
     * This method is suitable for cases where the status information contains lists of objects under a single key.
     *
     * @param key The key for which to retrieve the list.
     * @param targetClass The class to which the objects in the list should be cast.
     * @param <T> The type of the objects in the list to be returned.
     * @return The list of objects associated with the specified key, cast to the specified class; {@code null} if the key does not exist.
     */
    public <T> List<T> getStatusKeyAsList(@NonNull String key, Class<T> targetClass) {
        Assert.isTrue(status.containsKey(key), "Key not found in status map");
        return serializer.getMapper().convertValue(status.get(key),
               serializer.getMapper().getTypeFactory()
                        .constructCollectionType(List.class, targetClass));
    }

    /**
     * Retrieves a list of objects from the 'status' map based on the provided key, casting them to the specified class.
     * This method is suitable for cases where the status information contains lists of objects under a single key.
     *
     * @param key The key for which to retrieve the list.
     * @param targetClass The class to which the objects in the list should be cast.
     * @param <T> The type of the objects in the list to be returned.
     * @return The list of objects associated with the specified key, cast to the specified class; {@code null} if the key does not exist.
     */
    public <T> Map<String, T> getStatusKeyAsMap(@NonNull String key, Class<T> targetClass) {
        Assert.isTrue(status.containsKey(key), "Key not found in status map");
        return serializer.getMapper().convertValue(status.get(key),
               serializer.getMapper().getTypeFactory()
                        .constructMapType(Map.class, String.class, targetClass));
    }

    public <T> T getStatus(Class<T> targetClass) {
        return serializer.getMapper().convertValue(status, targetClass);
    }

}