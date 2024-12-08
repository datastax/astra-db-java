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

import com.datastax.astra.internal.serdes.tables.RowSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the response of a Data API operation involving documents.
 * <p>
 * This class provides a structured representation of a document response,
 * including its unique identifier(s) and status. The class is serializable
 * and integrates with JSON serialization frameworks like Jackson.
 * </p>
 */
@Getter
@Setter
public class DataAPIDocumentResponse implements Serializable {

    /**
     * A list of objects representing the unique identifier(s) of the document.
     * <p>
     * This field is annotated with {@link JsonProperty} to map it to the {@code "_id"}
     * key in JSON.
     * </p>
     */
    @JsonProperty("_id")
    private List<Object> id;

    /**
     * The status of the document operation, such as "SUCCESS" or "FAILED".
     */
    private String status;

    /**
     * Default constructor for serialization frameworks.
     */
    public DataAPIDocumentResponse() {}

    /**
     * Converts the {@code DataAPIDocumentResponse} object into a string representation.
     * This implementation uses {@link RowSerializer#marshall(Object)} for serialization.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return new RowSerializer().marshall(this);
    }
}