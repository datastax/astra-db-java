package com.datastax.astra.client.collections.mapping;

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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field as a vector in a collection document.
 * <p>
 * This annotation indicates that the field should be serialized with the special
 * property name "$vector" for use with DataStax Astra DB's vector search features.
 * The field must be of type {@code float[]} or {@code DataAPIVector}.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @DataApiCollection
 * public class MyDocument {
 *     @DocumentId
 *     private String id;
 *     
 *     @Vector
 *     private float[] embedding;
 *     
 *     // or
 *     
 *     @Vector
 *     private DataAPIVector embedding;
 *     
 *     // getters and setters
 * }
 * }
 * </pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JsonProperty("$vector")
public @interface Vector {
}
