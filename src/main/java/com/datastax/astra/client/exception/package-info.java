/**
 * Provides the set of exceptions specifically related to interactions with the Data API.
 * This package encompasses a variety of exception classes designed to signal different
 * types of errors that can occur while using the Data API client provided by DataStax Astra.
 * Exceptions in this package are thrown to indicate issues such as:
 * <ul>
 *     <li>General Data API client errors ({@link com.datastax.astra.exception.DataApiException})</li>
 *     <li>Errors related to receiving faulty or unexpected responses from the Data API
 *         ({@link com.datastax.astra.exception.DataApiFaultyResponseException})</li>
 *     <li>Errors specific to the response processing phase, especially when a command
 *         execution involves multiple sub-operations ({@link com.datastax.astra.exception.DataApiResponseException})</li>
 *     <li>Other specialized exceptions that may occur during the operation of the Data API client</li>
 * </ul>
 * These exceptions are crucial for robust error handling, allowing developers to catch and
 * manage specific issues gracefully. Each exception class is documented with details on when
 * and how it should be used, including example scenarios that illustrate typical use cases.
 * <p>
 * Utilizing these exceptions effectively enables developers to write more reliable and
 * maintainable applications by ensuring that errors are appropriately caught and handled,
 * thus improving the overall resilience of applications using the Data API client.
 * </p>
 */
package com.datastax.astra.exception;
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
