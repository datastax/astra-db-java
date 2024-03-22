/**
 * Provides classes for entities and Data Transfer Objects (DTOs) used to interact with the Data API.
 * <p>
 * This package {@code com.datastax.astra.client.model.api} is designed to serve as a foundation for modeling
 * the structure of JSON responses from the Data API. It includes various classes that represent the data
 * structure of API responses, including both data payloads and error information. These models facilitate
 * the process of serializing and deserializing JSON data when making requests to or receiving responses from
 * the Data API.
 * </p>
 * <p>
 * Entities in this package represent the domain-specific objects that are commonly used within the application
 * and are often mapped directly from the database records. DTOs, on the other hand, are used to encapsulate data
 * and send it from the client to the server or vice versa, focusing on the data transfer aspect rather than the
 * business logic of the application. This distinction helps in maintaining a clean separation between the
 * presentation layer and the underlying database model, enhancing both security and flexibility in data handling.
 * </p>
 * <p>
 * Key components of this package include:
 * <ul>
 *     <li>{@link com.datastax.astra.client.model.api.ApiData} - Represents data segments in API responses, encapsulating returned data from operations.</li>
 *     <li>{@link com.datastax.astra.client.model.api.ApiError} - Defines error structures for API responses, including error messages and codes.</li>
 *     <li>{@link com.datastax.astra.client.model.api.ApiResponse} - Defines wrapper for API responses, including the others classes from the package</li>
 * </ul>
 * These classes are integral to handling the communication between the client and the Data API, ensuring
 * that data can be easily processed and utilized within the application.
 * </p>
 */
package com.datastax.astra.client.model.api;

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
