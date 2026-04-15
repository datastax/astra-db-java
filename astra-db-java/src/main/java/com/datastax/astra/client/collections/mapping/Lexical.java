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
 * Annotation to mark a field for lexical search in a collection document.
 * <p>
 * This annotation indicates that the field should be serialized with the special
 * property name "$lexical" for use with DataStax Astra DB's lexical search features.
 * The field must be of type String.
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
 *     @Lexical
 *     private String searchableText;
 *     
 *     // getters and setters
 * }
 * }
 * </pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JsonProperty("$lexical")
public @interface Lexical {
}
