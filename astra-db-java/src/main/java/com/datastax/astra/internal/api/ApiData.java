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

import com.datastax.astra.client.model.Document;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * Represents a segment of the response payload for a JSON API, encapsulating the data returned from a query or operation.
 * This class is designed to package the data section of a response, including any relevant documents, a potential single document,
 * and information for paginated results, such as the state of the next page.
 */
@Getter
@Setter
public class ApiData {

    /**
     * A list of {@link Document} objects representing a collection of documents returned by the API. This could be the result of
     * a query that fetches multiple documents at once. Each {@link Document} in the list should be considered a separate item
     * in the dataset returned.
     */
    List<Document> documents;

    /**
     * A single {@link Document} object, typically used in responses where the API operation or query is expected to return
     * a single document. This field may be {@code null} if the operation does not result in a single document return,
     * or if the {@code documents} list is used instead.
     */
    Document document;

    /**
     * A {@code String} that represents the state of the next page for pagination purposes. This is typically used in
     * conjunction with APIs that limit the amount of data returned in a single response for performance reasons.
     * The {@code nextPageState} can be passed in subsequent API calls to retrieve the next set of data. A {@code null}
     * or empty value indicates that there are no more pages to retrieve.
     */
    String nextPageState;

    /**
     * Default constructor for {@link ApiData}. Initializes a new instance of the class without setting any properties.
     * Fields should be set via their setters or directly, depending on the usage context and the framework's conventions.
     */
    public ApiData() {
        // left blank, will be populated by jackson
    }
}