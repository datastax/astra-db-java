package com.datastax.astra.internal.serdes.collections;

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

import com.datastax.astra.client.collections.mapping.Lexical;
import com.datastax.astra.client.collections.mapping.Vector;
import com.datastax.astra.client.collections.mapping.Vectorize;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Custom Jackson annotation introspector for Data API specific annotations.
 * <p>
 * This introspector handles special field name mappings for Data API annotations:
 * <ul>
 *   <li>{@link Vectorize} - serialized as "$vectorize"</li>
 *   <li>{@link Lexical} - serialized as "$lexical"</li>
 *   <li>{@link Vector} - serialized as "$vector"</li>
 * </ul>
 * </p>
 */
public class DataAPIAnnotationIntrospector extends JacksonAnnotationIntrospector {

    /**
     * Default constructor.
     */
    public DataAPIAnnotationIntrospector() {
        super();
    }

    /**
     * Overrides the default field name resolution to handle Data API specific annotations.
     * <p>
     * Fields annotated with {@link Vectorize}, {@link Lexical}, or {@link Vector} will be
     * serialized with the appropriate "$" prefix.
     * </p>
     *
     * @param a the annotated field
     * @param name the default property name
     * @return the property name to use for serialization, with "$" prefix if applicable
     */
    @Override
    public PropertyName findNameForSerialization(Annotated a) {
        // Check for @Vectorize annotation
        if (a.hasAnnotation(Vectorize.class)) {
            return PropertyName.construct("$vectorize");
        }
        
        // Check for @Lexical annotation
        if (a.hasAnnotation(Lexical.class)) {
            return PropertyName.construct("$lexical");
        }
        
        // Check for @Vector annotation
        if (a.hasAnnotation(Vector.class)) {
            return PropertyName.construct("$vector");
        }
        
        // Fall back to default behavior
        return super.findNameForSerialization(a);
    }
}
