/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datastax.astra.internal.utils;

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

/**
 * Syntaxic sugar for common validations.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class Assert {
    
    /**
     * Hide default.
     */
    private Assert() {}
    
    /**
     * Validates that the input string is not null and not empty.
     * <p>
     * This method is used to enforce preconditions on string parameters,
     * ensuring they contain meaningful content before processing.
     * </p>
     *
     * @param s the string value to validate
     * @param name the parameter name to include in the error message
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void hasLength(String s, String name) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Parameter '" + name + "' should be null nor empty");
        }
    }
    
    /**
     * Validates that the input object is not null.
     * <p>
     * This method is used to enforce preconditions on object parameters,
     * ensuring they are properly initialized before use.
     * </p>
     *
     * @param o the object value to validate
     * @param name the parameter name to include in the error message
     * @throws IllegalArgumentException if the object is null
     */
    public static void notNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Parameter '" + name + "' should be null nor empty");
        }
    }
    
    /**
     * Check condition at start.
     *
     * @param condition
     *      predicate should be true
     * @param msg
     *      error message
     */
    public static void isTrue(boolean condition, String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

}
