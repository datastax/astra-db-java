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

package com.dtsx.astra.sdk.utils;

import java.util.regex.Pattern;

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
     * Input string should not be empty.
     *
     * @param s
     *      string value
     * @param name
     *      param name
     */
    public static void hasLength(String s, String name) {
        if (s == null || "".equals(s)) {
            throw new IllegalArgumentException("Parameter '" + name + "' should be null nor empty");
        }
    }
    
    /**
     * Input object should not be null
     * @param o
     *      object value
     * @param name
     *      param name
     */
    public static void notNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Parameter '" + name + "' should be null nor empty");
        }
    }
    
    /**
     * Check condition at start.
     *
     * @param b
     *      predicate should be true
     * @param msg
     *      error message
     */
    public static void isTrue(Boolean b, String msg) {
        if (!b) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static final String UUID_PATTERN_STR = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private static final Pattern UUID_PATTERN = Pattern.compile("^" + UUID_PATTERN_STR + "$");
    private static final Pattern DATACENTER_ID_PATTERN = Pattern.compile("^" + UUID_PATTERN_STR + "-\\d+$");

    public static void isUUID(String id, String name) {
        hasLength(id, name);

        if (!UUID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Parameter '" + name + "' should be a valid UUID");
        }
    }

    public static void isDatacenterID(String id, String name) {
        hasLength(id, name);

        if (!DATACENTER_ID_PATTERN.matcher(id).matches()) {
            var addendum = "";

            if (UUID_PATTERN.matcher(id).matches()) {
                addendum = " (missing '-<number>' suffix; did you accidentally pass a database id instead?)";
            }

            throw new IllegalArgumentException("Parameter '" + name + "' should be a valid datacenter id of format <uuid>-<number>" + addendum);
        }
    }
}
